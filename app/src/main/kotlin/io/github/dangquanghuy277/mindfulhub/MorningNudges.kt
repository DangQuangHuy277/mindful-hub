package io.github.dangquanghuy277.mindfulhub

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.MorningNudge
import io.github.dangquanghuy277.mindfulhub.core.MorningWindow
import io.github.dangquanghuy277.mindfulhub.data.MorningNudgeEntity
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * D-030: one quiet notification after the first unlock of the morning, opening the F65 check-in.
 * The rules are in `MorningNudge` (:core). The unlock broadcast is only delivered to a receiver
 * registered at runtime, which lives as long as the process; the gate's accessibility service
 * keeps the process alive. If it was dead anyway, the hourly usage import finds the morning's
 * first unlock in the history and posts then ([checkFromHistory]).
 */
object MorningNudges {
    const val CHANNEL = "morning"
    const val EXTRA_DAY = "morningNudgeDay"
    private const val TAG = "MindfulMorning"
    private const val NOTIFICATION_ID = 30
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()

    fun createChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Morning check-in", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "One quiet note after your first unlock in the morning. Never during work hours."
                setSound(null, null)
                enableVibration(false)
            },
        )
    }

    /** Called once from `MindfulApp.onCreate`. */
    fun listenForUnlock(context: Context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action != Intent.ACTION_USER_PRESENT) return
                Log.d(TAG, "unlock")
                val pending = goAsync()
                scope.launch {
                    try {
                        maybePost(ctx.applicationContext, Instant.now(), firstUnlockAt = Instant.now(), source = "unlock")
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(Intent.ACTION_USER_PRESENT), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    /** The hourly fallback: post only if the phone was already unlocked this morning. */
    suspend fun checkFromHistory(context: Context, now: Instant) {
        val c = (context.applicationContext as MindfulApp).container
        val day = c.clock.dayOf(now)
        val window = MorningWindow.of(day, c.settingsStore.on(day), c.clock) ?: return
        val unlock = c.usage.firstUnlock(window.start, now) ?: return
        maybePost(context, now, unlock, source = "worker")
    }

    private suspend fun maybePost(context: Context, now: Instant, firstUnlockAt: Instant, source: String) = lock.withLock {
        val c = (context.applicationContext as MindfulApp).container
        val day = c.clock.dayOf(now)
        val s = c.settingsStore.on(day)
        val posted = c.morningNudges.get(day.toString()) != null
        val checkInDone = c.checkIns.get(day.minusDays(1).toString())?.answeredAt != null
        if (!MorningNudge.shouldPost(now, s, c.clock, posted, checkInDone)) return@withLock
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return@withLock
        val expires = MorningNudge.expiresAt(now, s, c.clock)
        c.morningNudges.insert(
            MorningNudgeEntity(
                day = day.toString(), firstUnlockAt = firstUnlockAt.toEpochMilli(), postedAt = now.toEpochMilli(),
                expiresAt = expires?.toEpochMilli(), source = source,
            ),
        )
        post(context, day.toString(), expires?.let { Duration.between(now, it) }, withReaction = s.on(Feature.REACTION_CHECK))
    }

    internal fun post(context: Context, day: String, clearsAfter: Duration?, withReaction: Boolean) {
        val open = PendingIntent.getActivity(
            context, NOTIFICATION_ID,
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_DAY, day)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val dismissed = PendingIntent.getBroadcast(
            context, NOTIFICATION_ID,
            Intent(context, NudgeDismissedReceiver::class.java).putExtra(EXTRA_DAY, day),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_day)
            .setContentTitle("☀️ Good morning")
            .setContentText(if (withReaction) "Two taps on how you slept, then the 3-minute reaction check." else "Two taps on how you slept.")
            .setContentIntent(open)
            .setDeleteIntent(dismissed)
            .setAutoCancel(true)
            .setSilent(true)
            .apply { clearsAfter?.let { setTimeoutAfter(it.toMillis()) } }
            .build()
        @Suppress("MissingPermission") // checked by areNotificationsEnabled before posting
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, n)
    }

    /** The check-in was answered or skipped: the nudge has done its job. */
    fun clear(context: Context) = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    /** From `MainActivity`: the notification was tapped. */
    fun tapped(context: Context, intent: Intent?) {
        val day = intent?.getStringExtra(EXTRA_DAY) ?: return
        intent.removeExtra(EXTRA_DAY)
        val c = (context.applicationContext as MindfulApp).container
        scope.launch { c.morningNudges.tapped(day, System.currentTimeMillis()) }
    }
}

/** D-030: the notification was swiped away. Not exported; only our own PendingIntent reaches it. */
class NudgeDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getStringExtra(MorningNudges.EXTRA_DAY) ?: return
        val c = (context.applicationContext as MindfulApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                c.morningNudges.dismissed(day, System.currentTimeMillis())
            } finally {
                pending.finish()
            }
        }
    }
}
