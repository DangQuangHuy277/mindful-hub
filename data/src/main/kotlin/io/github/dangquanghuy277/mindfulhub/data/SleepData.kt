package io.github.dangquanghuy277.mindfulhub.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.core.NightFacts
import io.github.dangquanghuy277.mindfulhub.core.NightLog
import io.github.dangquanghuy277.mindfulhub.core.NightRules
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import java.time.Instant
import java.time.LocalDate

// F64 night log and F65 morning check-in, schema v6. Each has its own tables, so dropping either is
// a migration that drops them and nothing else.

/**
 * F64: one night as the phone saw it, keyed by the logical day the night belongs to. Recomputed
 * from `app_event` until [settled]; [rulesVersion] says which [NightRules] made it. The planned
 * times are the ones that were live that night (sleep quest or manual).
 */
@Entity(tableName = "night_log")
data class NightLogEntity(
    @PrimaryKey val night: String,
    /** FOUND / NO_CLEAR_NIGHT / NO_DATA */
    val status: String,
    val plannedBed: Long,
    val plannedWake: Long,
    val downAt: Long?,
    val upAt: Long?,
    val lastPkg: String?,
    val firstPkg: String?,
    val interruptions: Int,
    val interruptionSec: Long,
    /** Comma list, in order, one package per interruption (the app it was spent in longest, or ""). */
    val interruptionPkgs: String,
    val glances: Int,
    /** Other phone-down stretches of a split night, "startMs-endMs" separated by commas. */
    val otherStretches: String,
    val shutdowns: Int,
    /** False = older history without SCREEN_ON events, measured the fallback way. */
    val screenOnSignal: Boolean,
    val settled: Boolean,
    val rulesVersion: Int,
    val computedAt: Long,
)

/** F64 exposure: each time the night card was on screen, so "didn't help" ≠ "never saw it". */
@Entity(tableName = "night_card_shown", indices = [Index("night")])
data class NightCardShownEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val night: String,
    /** Whether the numbers shown could still change. */
    val settled: Boolean,
)

data class NightKey(val night: String, val settled: Boolean, val rulesVersion: Int)

@Dao
interface NightLogDao {
    @Upsert
    suspend fun upsert(e: NightLogEntity)

    @Query("SELECT * FROM night_log WHERE night = :night")
    suspend fun get(night: String): NightLogEntity?

    @Query("SELECT night, settled, rulesVersion FROM night_log")
    suspend fun keys(): List<NightKey>

    @Insert
    suspend fun insertShown(e: NightCardShownEntity)
}

fun NightFacts.toEntity(computedAt: Instant) = NightLogEntity(
    night = night.toString(),
    status = status.name,
    plannedBed = plannedBed.toEpochMilli(),
    plannedWake = plannedWake.toEpochMilli(),
    downAt = downAt?.toEpochMilli(),
    upAt = upAt?.toEpochMilli(),
    lastPkg = lastPkg,
    firstPkg = firstPkg,
    interruptions = interruptions.size,
    interruptionSec = interruptions.sumOf { it.duration.seconds },
    interruptionPkgs = interruptions.joinToString(",") { it.main.orEmpty() },
    glances = glances,
    otherStretches = otherStretches.joinToString(",") { "${it.start.toEpochMilli()}-${it.end.toEpochMilli()}" },
    shutdowns = shutdowns,
    screenOnSignal = screenOnSignal,
    settled = settled,
    rulesVersion = rulesVersion,
    computedAt = computedAt.toEpochMilli(),
)

/** Keeps `night_log` current: every night of our history not yet settled under today's rules. */
class NightLogStore(
    private val dao: NightLogDao,
    private val usage: UsageRepository,
    private val clock: LogicalDayClock,
    private val settings: SettingsStore,
    private val rules: NightRules = NightRules(),
) {
    /**
     * Computes the nights from the start of our copy of the history (at most [maxBackDays]) up to
     * last night. The first run fills the baseline in one go; after that only the last night or two
     * are open.
     */
    suspend fun update(now: Instant, maxBackDays: Long = 60) {
        val since = usage.historySince() ?: return
        val today = clock.dayOf(now)
        val done = dao.keys().filter { it.settled && it.rulesVersion == rules.version }.map { it.night }.toSet()
        val rows = settings.rows()
        var night = maxOf(clock.dayOf(since), today.minusDays(maxBackDays))
        while (night.isBefore(today)) {
            if (night.toString() !in done) dao.upsert(compute(night, now, rows).toEntity(now))
            night = night.plusDays(1)
        }
    }

    suspend fun get(night: LocalDate): NightLogEntity? = dao.get(night.toString())

    /** The full facts for one night, with each check's time (the table keeps only counts). */
    suspend fun facts(night: LocalDate, now: Instant): NightFacts = compute(night, now, settings.rows())

    private suspend fun compute(night: LocalDate, now: Instant, rows: List<SettingEntity>): NightFacts {
        val times = SettingsStore.decode(rows, night).sleepTimes(night)
        val (from, to) = NightLog.fetchRange(night, times, clock, rules)
        return NightLog.night(usage.events(from, minOf(to, now)), night, times, clock, now, rules)
    }

    suspend fun markShown(row: NightLogEntity, now: Instant) =
        dao.insertShown(NightCardShownEntity(ts = now.toEpochMilli(), night = row.night, settled = row.settled))
}

/**
 * F65: one morning check-in per night. [askedAt] is when it was first on screen (exposure); a skip
 * is kept as a row too, since "asked and skipped" is data. [plannedLate] marks a night you stayed
 * up on purpose (a 02:00 match), so it isn't read as a slip.
 */
@Entity(tableName = "sleep_checkin")
data class SleepCheckInEntity(
    @PrimaryKey val night: String,
    val askedAt: Long,
    val answeredAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val skipped: Boolean = false,
    /** 1 (not at all) … 5 (very) */
    val rested: Int? = null,
    /** SleepLatency name */
    val latency: String? = null,
    @ColumnInfo(defaultValue = "0") val plannedLate: Boolean = false,
)

@Dao
interface CheckInDao {
    /** Keeps the first time it was asked. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAsked(e: SleepCheckInEntity)

    @Query("SELECT * FROM sleep_checkin WHERE night = :night")
    suspend fun get(night: String): SleepCheckInEntity?

    @Query("UPDATE sleep_checkin SET answeredAt = :at, rested = :rested, latency = :latency, plannedLate = :plannedLate, skipped = 0 WHERE night = :night")
    suspend fun answer(night: String, at: Long, rested: Int, latency: String, plannedLate: Boolean)

    @Query("UPDATE sleep_checkin SET answeredAt = :at, skipped = 1, plannedLate = :plannedLate WHERE night = :night")
    suspend fun skip(night: String, at: Long, plannedLate: Boolean)
}
