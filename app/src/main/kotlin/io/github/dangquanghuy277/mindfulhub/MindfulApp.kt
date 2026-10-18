package io.github.dangquanghuy277.mindfulhub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.core.GateSettings
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.data.MindfulDatabase
import io.github.dangquanghuy277.mindfulhub.data.UsageRepository
import io.github.dangquanghuy277.mindfulhub.data.UsageStatsImporter
import io.github.dangquanghuy277.mindfulhub.gate.GateDeps
import io.github.dangquanghuy277.mindfulhub.gate.GateHost
import io.github.dangquanghuy277.mindfulhub.gate.GatePauseService
import io.github.dangquanghuy277.mindfulhub.gate.GateStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Manual DI (STACK-OPTIONS.md S7): one container, created once per process. */
class AppContainer(context: Context) {
    private val db = MindfulDatabase.create(context)
    val importer = UsageStatsImporter(context, db.appEvents())
    val usage = UsageRepository(db.appEvents())
    val gateDao = db.gate()
    val clock = LogicalDayClock(ZoneId.systemDefault())
    val gateSettings = GateSettings()
}

class MindfulApp : Application(), GateHost {
    lateinit var container: AppContainer
        private set

    override val gateDeps: GateDeps by lazy { GateDeps(container.gateDao, container.clock, container.gateSettings) }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(HEALTH_CHANNEL, "Gate health", NotificationManager.IMPORTANCE_HIGH),
        )
        // Android keeps only a short event history, so copy it hourly before it ages out.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "usage-import",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UsageImportWorker>(1, TimeUnit.HOURS).build(),
        )
    }

    companion object {
        const val HEALTH_CHANNEL = "gate-health"
    }
}

/**
 * Hourly: copy usage history, then check the gate is alive (F16). Two independent signals: the
 * system says a gated app was in front, but the gate logged nothing, so the gate is dead.
 */
class UsageImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val c = (applicationContext as MindfulApp).container
        if (!c.importer.hasPermission()) return Result.success()
        c.importer.importNew()

        val to = Instant.now()
        val from = to.minus(Duration.ofHours(1))
        val gated = c.usage.foregroundPackages(from, to).any { c.gateSettings.profileOf(it) != AppProfile.NONE }
        val gateSilent = c.gateDao.countBetween(from.toEpochMilli(), to.toEpochMilli()) == 0
        // Off on purpose while a banking app is in front (D-020): not a failure.
        if (GatePauseService.pausedFor != null) return Result.success()
        if (!GateStatus.isEnabled(applicationContext) || (gated && gateSilent)) notifyGateDown()
        return Result.success()
    }

    private fun notifyGateDown() {
        val ctx = applicationContext
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(ctx, MindfulApp.HEALTH_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("The gate is off")
            .setContentText("Samsung or an update switched it off. Tap to turn it back on.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission") // checked by areNotificationsEnabled above
        NotificationManagerCompat.from(ctx).notify(1, n)
    }
}
