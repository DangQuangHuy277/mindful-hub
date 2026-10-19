package io.github.dangquanghuy277.mindfulhub.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.GoodThing
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.core.PlanItem
import io.github.dangquanghuy277.mindfulhub.core.Precommit
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.SettingRow
import io.github.dangquanghuy277.mindfulhub.core.SettingsCodec
import io.github.dangquanghuy277.mindfulhub.core.SettingsHistory
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// Tables for the D-021 features. Each feature's table is separate, so dropping a feature later is
// a migration that drops its table, nothing else.

/**
 * A setting as calm-you wrote it, with the logical day it starts to count (ARCHITECTURE.md,
 * "Precommitment lives in the data model"). The value for a day is the row with the latest
 * [effectiveFrom] on or before it.
 */
@Entity(tableName = "setting", primaryKeys = ["key", "effectiveFrom"])
data class SettingEntity(val key: String, val value: String, val effectiveFrom: String, val setAt: Long)

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting ORDER BY effectiveFrom")
    suspend fun all(): List<SettingEntity>

    @Query("SELECT * FROM setting ORDER BY effectiveFrom")
    fun observe(): Flow<List<SettingEntity>>

    @Upsert
    suspend fun upsert(row: SettingEntity)

    /** The latest intent wins: a new change replaces any change still waiting for a later day. */
    @Query("DELETE FROM setting WHERE `key` = :key AND effectiveFrom > :from")
    suspend fun dropLater(key: String, from: String)
}

class SettingsStore(private val dao: SettingDao) {
    fun observe(): Flow<List<SettingEntity>> = dao.observe()

    suspend fun on(day: LocalDate): Settings = decode(dao.all(), day)

    /** Changes that loosen a rule and wait for a later day: key to (value, day it starts). */
    suspend fun pending(day: LocalDate): Map<String, Pair<String, LocalDate>> =
        dao.all().filter { LocalDate.parse(it.effectiveFrom).isAfter(day) }
            .associate { it.key to (it.value to LocalDate.parse(it.effectiveFrom)) }

    /** Writes [value] for [key]; returns the logical day it takes effect (today, or tomorrow for a loosening). */
    suspend fun set(key: String, value: String, today: LocalDate, now: Instant = Instant.now()): LocalDate {
        val old = values(dao.all(), today)[key]
        val from = Precommit.effectiveFrom(key, old, value, today)
        dao.dropLater(key, from.toString())
        dao.upsert(SettingEntity(key, value, from.toString(), now.toEpochMilli()))
        return from
    }

    companion object {
        fun values(rows: List<SettingEntity>, day: LocalDate): Map<String, String> =
            SettingsHistory.valuesOn(rows.map { SettingRow(it.key, it.value, LocalDate.parse(it.effectiveFrom)) }, day)

        fun decode(rows: List<SettingEntity>, day: LocalDate): Settings = SettingsCodec.decode(values(rows, day))
    }
}

/** A5: one logged good thing. */
@Entity(tableName = "good_thing", indices = [Index("ts")])
data class GoodThingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val category: String,
    val minutes: Int,
    val note: String = "",
    /** home / gate / plan */
    val source: String = "home",
)

@Dao
interface GoodThingDao {
    @Insert
    suspend fun insert(e: GoodThingEntity): Long

    @Query("DELETE FROM good_thing WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM good_thing WHERE ts >= :from AND ts < :to ORDER BY ts")
    suspend fun between(from: Long, to: Long): List<GoodThingEntity>

    @Query("SELECT * FROM good_thing WHERE ts >= :from ORDER BY ts")
    fun observeSince(from: Long): Flow<List<GoodThingEntity>>
}

fun GoodThingEntity.toCore() = GoodThing(Instant.ofEpochMilli(ts), Category.valueOf(category), minutes, note, source)

/** F27: one planned good thing for a week. */
@Entity(tableName = "plan_item", indices = [Index("weekStart")])
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStart: String,
    /** 1 = Monday … 7 = Sunday */
    val day: Int,
    /** HH:mm */
    val time: String,
    val category: String,
    val minutes: Int,
    val title: String,
    /** null, DONE or SKIPPED */
    val status: String? = null,
)

fun PlanItemEntity.toCore() = PlanItem(
    id, LocalDate.parse(weekStart), DayOfWeek.of(day), LocalTime.parse(time), Category.valueOf(category), minutes, title, status,
)

fun PlanItem.toEntity() = PlanItemEntity(id, weekStart.toString(), day.value, time.toString(), category.name, minutes, title, status)

@Dao
interface PlanDao {
    @Upsert
    suspend fun upsert(e: PlanItemEntity): Long

    @Query("DELETE FROM plan_item WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM plan_item WHERE weekStart = :weekStart ORDER BY day, time")
    suspend fun week(weekStart: String): List<PlanItemEntity>

    @Query("SELECT * FROM plan_item WHERE weekStart >= :from ORDER BY weekStart, day, time")
    fun observeFrom(from: String): Flow<List<PlanItemEntity>>

    @Query("UPDATE plan_item SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String?)

    /** F60: which setup-kit items are ticked this week, as a comma list of indexes. */
    @Upsert
    suspend fun saveKit(e: SetupKitEntity)

    @Query("SELECT * FROM setup_kit WHERE weekStart = :weekStart")
    suspend fun kit(weekStart: String): SetupKitEntity?
}

@Entity(tableName = "setup_kit")
data class SetupKitEntity(@PrimaryKey val weekStart: String, val checked: String)

/** F11 + F12: one saved weekly review. */
@Entity(tableName = "weekly_review")
data class WeeklyReviewEntity(
    @PrimaryKey val weekStart: String,
    /** Five answers 0..5, comma separated, or empty if skipped. */
    val who5: String,
    val missedAnything: String,
    val oneChange: String,
    val savedAt: Long,
)

@Dao
interface ReviewDao {
    @Upsert
    suspend fun save(e: WeeklyReviewEntity)

    @Query("SELECT * FROM weekly_review WHERE weekStart = :weekStart")
    suspend fun get(weekStart: String): WeeklyReviewEntity?

    @Query("SELECT * FROM weekly_review ORDER BY weekStart DESC LIMIT :n")
    suspend fun latest(n: Int): List<WeeklyReviewEntity>
}

/** F31 + F53: one postcard to tomorrow-you, keyed by the logical day of the night it was written. */
@Entity(tableName = "postcard")
data class PostcardEntity(@PrimaryKey val night: String, val text: String, val writtenAt: Long)

@Dao
interface PostcardDao {
    @Upsert
    suspend fun save(e: PostcardEntity)

    @Query("SELECT * FROM postcard WHERE night = :night")
    suspend fun forNight(night: String): PostcardEntity?

    @Query("SELECT * FROM postcard ORDER BY night DESC LIMIT :n")
    suspend fun latest(n: Int): List<PostcardEntity>
}

/** Converts a logical day range to the epoch millis the DAOs use. */
fun LogicalDayClock.millisOf(from: LocalDate, toExclusive: LocalDate): Pair<Long, Long> =
    startOf(from).toEpochMilli() to startOf(toExclusive).toEpochMilli()
