package io.github.dangquanghuy277.mindfulhub

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.data.MindfulDatabase
import io.github.dangquanghuy277.mindfulhub.data.UsageRepository
import io.github.dangquanghuy277.mindfulhub.data.UsageStatsImporter
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Manual DI (STACK-OPTIONS.md S7): one container, created once per process. */
class AppContainer(context: Context) {
    private val db = MindfulDatabase.create(context)
    val importer = UsageStatsImporter(context, db.appEvents())
    val usage = UsageRepository(db.appEvents())
    val clock = LogicalDayClock(ZoneId.systemDefault())
}

class MindfulApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Android keeps only a short event history, so copy it hourly before it ages out.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "usage-import",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UsageImportWorker>(1, TimeUnit.HOURS).build(),
        )
    }
}

class UsageImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val importer = (applicationContext as MindfulApp).container.importer
        if (!importer.hasPermission()) return Result.success()
        importer.importNew()
        return Result.success()
    }
}
