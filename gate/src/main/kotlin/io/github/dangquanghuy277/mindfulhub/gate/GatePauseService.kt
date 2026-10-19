package io.github.dangquanghuy277.mindfulhub.gate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.data.GateStatusEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Keeps the gate off while a banking app is in front (D-020).
 *
 * Vietnamese banking apps refuse to run while a sideloaded accessibility service is on. The gate
 * switches itself off when one comes to the front and starts this service, which watches usage
 * events (the gate can't: it's off) and switches the gate back on once you've left. Turning it
 * back on needs WRITE_SECURE_SETTINGS, granted once over adb; without it the notification asks you
 * to do it in Settings.
 */
class GatePauseService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private lateinit var usage: UsageStatsManager
    private var pkg = ""
    private var top: String? = null
    private var topSince = 0L
    private var lastQuery = 0L
    /** Paused by the bank shortcut: the bank isn't in front yet, and may never come (e.g. a crash). */
    private var waitingSince = 0L
    private var launchers: Set<String> = emptySet()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(UsageStatsManager::class.java)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Gate paused", NotificationManager.IMPORTANCE_LOW),
        )
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        launchers = packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }.toSet()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESUME) {
            resume()
            return START_NOT_STICKY
        }
        pkg = intent?.getStringExtra(EXTRA_PKG) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        pausedFor = pkg
        startForeground(NOTIFICATION_ID, pausedNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        val now = System.currentTimeMillis()
        top = pkg
        topSince = now
        waitingSince = if (intent.getBooleanExtra(EXTRA_LAUNCHING, false)) now else 0L
        // Only events from now on: replaying the launcher's earlier resume would end a shortcut
        // pause before the bank has even started.
        lastQuery = now
        main.removeCallbacks(poll)
        main.postDelayed(poll, POLL_MS)
        return START_NOT_STICKY
    }

    /**
     * The last ACTIVITY_RESUMED is the app in front. Locking the screen resumes nothing, so the
     * pause holds while the phone sleeps inside the bank.
     */
    private val poll = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val events = usage.queryEvents(lastQuery, now)
            val e = UsageEvents.Event()
            while (events.getNextEvent(e)) {
                if (e.eventType != UsageEvents.Event.ACTIVITY_RESUMED) continue
                if (e.packageName == pkg) waitingSince = 0L
                if (e.packageName != top) {
                    top = e.packageName
                    topSince = e.timeStamp
                }
            }
            lastQuery = now - 1_000 // overlap: replaying an event only re-sets the same answer
            val t = top
            when {
                waitingSince != 0L && now - waitingSince >= LAUNCH_TIMEOUT_MS -> return resume()
                t == null || t == pkg || t == packageName -> Unit
                settings().pausesGate(t) -> { pkg = t; pausedFor = t } // bank to bank: stay off
                now - topSince >= graceFor(t) -> return resume()
            }
            main.postDelayed(this, POLL_MS)
        }
    }

    /**
     * How long another app must stay in front before the gate comes back. A gated app or the home
     * screen means you've left; anything else may be the bank's own camera, photo picker or OTP
     * hop, and coming back on during that would trip the bank's check when you return.
     */
    private fun graceFor(other: String): Long = when {
        settings().profileOf(other) != AppProfile.NONE -> 0
        other in launchers -> HOME_GRACE_MS
        else -> OTHER_GRACE_MS
    }

    private fun resume() {
        main.removeCallbacks(poll)
        pausedFor = null
        val on = GateStatus.turnOn(this)
        logStatus(if (on) "RESUMED" else "RESUME_FAILED", pkg)
        if (on) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            // Can't switch it back ourselves: leave a notification that takes you to Settings.
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, offNotification())
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        stopSelf()
    }

    private fun settings() = (application as GateHost).gateDeps.settings

    /** Outlives this service: the row must land even as it stops itself. */
    private fun logStatus(state: String, detail: String) {
        val stamps = (application as GateHost).gateDeps.stamps
        logScope.launch { stamps.insertStatus(GateStatusEntity(ts = System.currentTimeMillis(), state = state, detail = detail)) }
    }

    private fun pausedNotification() = NotificationCompat.Builder(this, CHANNEL)
        .setSmallIcon(android.R.drawable.ic_media_pause)
        .setContentTitle("Gate paused for ${labelOf(pkg)}")
        .setContentText("It turns back on when you leave the app.")
        .setOngoing(true)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // you should see the gate is off
        .addAction(
            0, "Turn on now",
            PendingIntent.getService(
                this, 0, Intent(this, GatePauseService::class.java).setAction(ACTION_RESUME),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    private fun offNotification() = NotificationCompat.Builder(this, CHANNEL)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("The gate is still off")
        .setContentText("Tap to turn it back on. To make this automatic, see the gate card in Mindful Hub.")
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .setAutoCancel(true)
        .build()

    private fun labelOf(p: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(p, 0)).toString()
    }.getOrDefault(p)

    override fun onDestroy() {
        main.removeCallbacks(poll)
        pausedFor = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MindfulGate"
        private const val CHANNEL = "gate-pause"
        private const val NOTIFICATION_ID = 2
        private const val EXTRA_PKG = "pkg"
        private const val EXTRA_LAUNCHING = "launching"
        private const val ACTION_RESUME = "resume"
        private const val POLL_MS = 1_000L
        private const val HOME_GRACE_MS = 2_000L
        private const val OTHER_GRACE_MS = 60_000L
        private const val LAUNCH_TIMEOUT_MS = 15_000L
        private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** The banking app the gate is paused for, or null. Read by the hourly health check. */
        @Volatile
        var pausedFor: String? = null
            private set

        /** Called by the gate just before it switches itself off. False if Android refused. */
        fun start(context: Context, pkg: String, launching: Boolean): Boolean = runCatching {
            context.startForegroundService(
                Intent(context, GatePauseService::class.java).putExtra(EXTRA_PKG, pkg).putExtra(EXTRA_LAUNCHING, launching),
            )
        }.onFailure { Log.w(TAG, "could not start the pause watcher", it) }.isSuccess
    }
}
