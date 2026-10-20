package io.github.dangquanghuy277.mindfulhub.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import io.github.dangquanghuy277.mindfulhub.core.UsageEvent
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant

data class ImportResult(val scanned: Int, val inserted: Int)

/** Copies Android's usage events into our database (F09). Android only keeps a limited history. */
class UsageStatsImporter(
    private val context: Context,
    private val dao: AppEventDao,
    private val now: () -> Instant = Instant::now,
) {
    // unsafeCheckOpNoThrow is deprecated in API 37, but its replacement doesn't exist on our minSdk 31.
    @Suppress("DEPRECATION")
    fun hasPermission(): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode = ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Imports everything since the last imported event (with 1 min of overlap, deduplicated by the
     * unique index), or the last [firstLookback] on the very first run. Until the first SCREEN_ON
     * arrives it reaches back [backfill] too: F64 added that type (and shutdowns), and the system
     * still holds about that much history of them.
     */
    suspend fun importNew(firstLookback: Duration = Duration.ofDays(60), backfill: Duration = Duration.ofDays(14)): ImportResult = withContext(Dispatchers.IO) {
        val end = now()
        var start = dao.latestTs()?.let { Instant.ofEpochMilli(it).minusSeconds(60) } ?: end.minus(firstLookback)
        if (!dao.hasType(UsageEventType.SCREEN_ON.name)) start = minOf(start, end.minus(backfill))
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val events = usm.queryEvents(start.toEpochMilli(), end.toEpochMilli())
        val e = UsageEvents.Event()
        val batch = ArrayList<AppEventEntity>()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            val type = map(e.eventType) ?: continue
            batch += AppEventEntity(ts = e.timeStamp, pkg = e.packageName, type = type.name, source = "usage")
        }
        val inserted = batch.chunked(500).sumOf { chunk -> dao.insertAll(chunk).count { it != -1L } }
        ImportResult(scanned = batch.size, inserted = inserted)
    }

    private fun map(eventType: Int): UsageEventType? = when (eventType) {
        UsageEvents.Event.ACTIVITY_RESUMED -> UsageEventType.FOREGROUND
        UsageEvents.Event.ACTIVITY_PAUSED -> UsageEventType.BACKGROUND
        UsageEvents.Event.SCREEN_NON_INTERACTIVE -> UsageEventType.SCREEN_OFF
        UsageEvents.Event.KEYGUARD_HIDDEN -> UsageEventType.UNLOCK
        UsageEvents.Event.SCREEN_INTERACTIVE -> UsageEventType.SCREEN_ON
        UsageEvents.Event.DEVICE_SHUTDOWN -> UsageEventType.SHUTDOWN
        UsageEvents.Event.DEVICE_STARTUP -> UsageEventType.STARTUP
        else -> null
    }
}

class UsageRepository(private val dao: AppEventDao) {
    suspend fun events(from: Instant, to: Instant): List<UsageEvent> =
        dao.between(from.toEpochMilli(), to.toEpochMilli()).map {
            UsageEvent(Instant.ofEpochMilli(it.ts), it.pkg, UsageEventType.valueOf(it.type))
        }

    /** How far back our own copy of the history goes (spike item 3 in ARCHITECTURE.md). */
    suspend fun historySince(): Instant? = dao.earliestTs()?.let(Instant::ofEpochMilli)

    /** Packages that were in the foreground in [from, to), per the system's own record. */
    suspend fun foregroundPackages(from: Instant, to: Instant): Set<String> =
        dao.between(from.toEpochMilli(), to.toEpochMilli())
            .filter { it.type == UsageEventType.FOREGROUND.name }
            .map { it.pkg }
            .toSet()
}
