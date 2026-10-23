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
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.FeedBudget
import io.github.dangquanghuy277.mindfulhub.core.GateSettings
import io.github.dangquanghuy277.mindfulhub.core.Sessionizer
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.data.BuildStampEntity
import io.github.dangquanghuy277.mindfulhub.data.DataExport
import io.github.dangquanghuy277.mindfulhub.data.GateStatusEntity
import io.github.dangquanghuy277.mindfulhub.data.MindfulDatabase
import io.github.dangquanghuy277.mindfulhub.data.NightLogStore
import io.github.dangquanghuy277.mindfulhub.data.stampIfChanged
import io.github.dangquanghuy277.mindfulhub.data.SettingsStore
import io.github.dangquanghuy277.mindfulhub.data.UsageRepository
import io.github.dangquanghuy277.mindfulhub.data.UsageStatsImporter
import io.github.dangquanghuy277.mindfulhub.gate.BankLaunchActivity
import io.github.dangquanghuy277.mindfulhub.gate.GateDeps
import io.github.dangquanghuy277.mindfulhub.gate.GateHost
import io.github.dangquanghuy277.mindfulhub.gate.GatePauseService
import io.github.dangquanghuy277.mindfulhub.gate.GateStatus
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Manual DI (STACK-OPTIONS.md S7): one container, created once per process. */
class AppContainer(context: Context) {
    private val app = context.applicationContext
    private val db = MindfulDatabase.create(context)
    val importer = UsageStatsImporter(context, db.appEvents())
    val usage = UsageRepository(db.appEvents())
    val gateDao = db.gate()
    val clock = LogicalDayClock(ZoneId.systemDefault())
    val gateSettings = GateSettings()

    // D-021 features.
    val settingsStore = SettingsStore(db.settings())
    val goodThings = db.goodThings()
    val plans = db.plans()
    val reviews = db.reviews()
    val postcards = db.postcards()
    val stamps = db.stamps()
    val insteadOffers = db.insteadOffers()
    // F64 + F65 (SleepData.kt).
    val nightLog = NightLogStore(db.nights(), usage, clock, settingsStore)
    val checkIns = db.checkIns()
    // D-030 + F75 (MorningData.kt).
    val morningNudges = db.morningNudges()
    val reactions = db.reactions()

    /** A6: the whole database to Downloads; returns the file name. */
    suspend fun exportData(): String = DataExport.toDownloads(app, db)

    /** Records the build and gate config, if either changed since the last start (v4). */
    suspend fun stampBuild() {
        val info = app.packageManager.getPackageInfo(app.packageName, 0)
        stamps.stampIfChanged(
            BuildStampEntity(
                ts = System.currentTimeMillis(), versionName = info.versionName.orEmpty(), gitSha = BuildConfig.GIT_SHA,
                installedAt = info.lastUpdateTime, gateConfig = gateSettings.toString(),
            ),
        )
    }
    val strictApps: Set<String> = gateSettings.profiles.filterValues { it == AppProfile.STRICT }.keys

    /** F29: minutes in STRICT apps so far today. Imports the newest system events first. */
    suspend fun feedMinutesToday(now: Instant): Double {
        if (importer.hasPermission()) runCatching { importer.importNew() }
        val from = clock.startOf(clock.dayOf(now))
        val sessions = Sessionizer.sessions(usage.events(from.minus(Duration.ofHours(3)), now), now)
        return FeedBudget.usedMinutes(sessions, strictApps, from, now)
    }
}

class MindfulApp : Application(), GateHost {
    lateinit var container: AppContainer
        private set

    override val gateDeps: GateDeps by lazy {
        with(container) { GateDeps(gateDao, clock, gateSettings, settingsStore, goodThings, postcards, ::feedMinutesToday, stamps, insteadOffers) }
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { runCatching { container.stampBuild() } }
        BankLaunchActivity.publishShortcuts(this, container.gateSettings)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(HEALTH_CHANNEL, "Gate health", NotificationManager.IMPORTANCE_HIGH),
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(PlanReminders.CHANNEL, "Planned good things", NotificationManager.IMPORTANCE_DEFAULT),
        )
        MorningNudges.createChannel(this)
        MorningNudges.listenForUnlock(this)
        MorningDebug.register(this)
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
        // F64: nights are logged even on mornings you don't open the app.
        if (c.settingsStore.on(c.clock.dayOf(Instant.now())).on(Feature.NIGHT_LOG)) runCatching { c.nightLog.update(Instant.now()) }
        // D-030: the morning nudge, if the unlock broadcast was missed.
        runCatching { MorningNudges.checkFromHistory(applicationContext, Instant.now()) }

        val to = Instant.now()
        val from = to.minus(Duration.ofHours(1))
        val gated = c.usage.foregroundPackages(from, to).any { c.gateSettings.profileOf(it) != AppProfile.NONE }
        val gateSilent = c.gateDao.countBetween(from.toEpochMilli(), to.toEpochMilli()) == 0
        // Off on purpose while a banking app is in front (D-020): not a failure.
        if (GatePauseService.pausedFor != null) return Result.success()
        val down = when {
            !GateStatus.isEnabled(applicationContext) -> "DISABLED"
            gated && gateSilent -> "SILENT"
            else -> null
        }
        if (down != null) {
            c.stamps.insertStatus(GateStatusEntity(ts = to.toEpochMilli(), state = "HEALTH_ALERT", detail = down))
            notifyGateDown()
        }
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
