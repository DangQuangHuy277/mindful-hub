package io.github.dangquanghuy277.mindfulhub

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.core.PlanItem
import io.github.dangquanghuy277.mindfulhub.core.WeekPlan
import io.github.dangquanghuy277.mindfulhub.core.Weeks
import io.github.dangquanghuy277.mindfulhub.data.toCore
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * F27: a gentle reminder at the time of each planned good thing, so the plan is an appointment
 * (P28). One unique WorkManager job per item; rescheduling replaces it, so calling [schedule] on
 * every refresh is safe. Inexact by design: no exact-alarm permission needed.
 */
object PlanReminders {
    const val CHANNEL = "plan"

    fun schedule(context: Context, items: List<PlanItem>, clock: LogicalDayClock, now: Instant = Instant.now()) {
        val wm = WorkManager.getInstance(context)
        for (item in items) {
            val at = WeekPlan.instantOf(item, clock)
            if (item.status != null || !at.isAfter(now)) {
                cancel(context, item.id)
                continue
            }
            val request = OneTimeWorkRequestBuilder<PlanReminderWorker>()
                .setInitialDelay(Duration.between(now, at).toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("id" to item.id))
                .build()
            wm.enqueueUniqueWork(name(item.id), ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun cancel(context: Context, id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(name(id))
    }

    private fun name(id: Long) = "plan-$id"
}

class PlanReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val c = (applicationContext as MindfulApp).container
        val id = inputData.getLong("id", -1)
        val today = c.clock.dayOf(Instant.now())
        if (!c.settingsStore.on(today).on(Feature.WEEK_PLAN)) return Result.success()
        val item = (c.plans.week(Weeks.startOf(today).toString()) + c.plans.week(Weeks.startOf(today).minusWeeks(1).toString()))
            .map { it.toCore() }.firstOrNull { it.id == id } ?: return Result.success()
        if (item.status != null) return Result.success()
        val ctx = applicationContext
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return Result.success()
        val open = PendingIntent.getActivity(
            ctx, id.toInt(), Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(ctx, PlanReminders.CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("${item.category.emoji} ${item.title}")
            .setContentText("You planned this for ${item.time}, ${item.minutes} min. Mark it done at home when it's done.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission") // checked by areNotificationsEnabled above
        NotificationManagerCompat.from(ctx).notify(1000 + id.toInt(), n)
        return Result.success()
    }
}
