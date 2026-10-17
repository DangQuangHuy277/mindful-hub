package io.github.dangquanghuy277.mindfulhub.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * One row per usage event. `source` is "usage" (system UsageStats) or, later, "gate" (our
 * accessibility service). The two are kept apart on purpose: comparing them is the health check
 * (ARCHITECTURE.md, "The gate must fail loudly").
 */
@Entity(
    tableName = "app_event",
    indices = [Index(value = ["ts", "pkg", "type", "source"], unique = true), Index("ts")],
)
data class AppEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val pkg: String,
    val type: String,
    val source: String,
)

@Dao
interface AppEventDao {
    /** Re-imports overlap on purpose; the unique index drops duplicates (returns -1 for those). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<AppEventEntity>): List<Long>

    @Query("SELECT * FROM app_event WHERE source = :source AND ts >= :from AND ts < :to ORDER BY ts")
    suspend fun between(from: Long, to: Long, source: String = "usage"): List<AppEventEntity>

    @Query("SELECT MAX(ts) FROM app_event WHERE source = :source")
    suspend fun latestTs(source: String = "usage"): Long?

    @Query("SELECT MIN(ts) FROM app_event WHERE source = :source")
    suspend fun earliestTs(source: String = "usage"): Long?
}

/** Every gate decision and what the user did with it (G1, F05). `outcome` is filled in later. */
@Entity(tableName = "gate_event", indices = [Index("ts"), Index("pkg")])
data class GateEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val pkg: String,
    val screen: String,
    /** ALLOW / PROMPT / BLOCK */
    val decision: String,
    val delayMs: Long,
    /** PROCEEDED / ABANDONED / BLOCKED, or null while the prompt is still open. */
    val outcome: String? = null,
    val logicalDay: String,
)

/** A session opened through the gate (G1, G3, G4). */
@Entity(tableName = "session", indices = [Index("start")])
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pkg: String,
    val purpose: String,
    val timeBoxMin: Int,
    val start: Long,
    val plannedEnd: Long,
    val end: Long? = null,
    /** TIMEBOX / USER / SCREEN_OFF */
    val endedBy: String? = null,
    /** 1 = 👍, 0 = 😐, -1 = 👎 */
    val rating: Int? = null,
)

@Dao
interface GateDao {
    @Insert
    suspend fun insertEvent(e: GateEventEntity): Long

    @Query("UPDATE gate_event SET outcome = :outcome WHERE id = :id")
    suspend fun setOutcome(id: Long, outcome: String)

    @Query("SELECT COUNT(*) FROM gate_event WHERE pkg = :pkg AND logicalDay = :day AND decision = 'PROMPT'")
    suspend fun promptsOn(pkg: String, day: String): Int

    @Query("SELECT COUNT(*) FROM gate_event WHERE ts >= :from AND ts < :to")
    suspend fun countBetween(from: Long, to: Long): Int

    @Query("SELECT decision, outcome, COUNT(*) AS n FROM gate_event WHERE logicalDay = :day GROUP BY decision, outcome")
    suspend fun summary(day: String): List<GateSummaryRow>

    @Insert
    suspend fun insertSession(s: SessionEntity): Long

    @Query("UPDATE session SET `end` = :end, endedBy = :endedBy WHERE id = :id AND `end` IS NULL")
    suspend fun endSession(id: Long, end: Long, endedBy: String)

    @Query("UPDATE session SET rating = :rating WHERE id = :id")
    suspend fun rate(id: Long, rating: Int)
}

data class GateSummaryRow(val decision: String, val outcome: String?, val n: Int)

@Database(
    entities = [AppEventEntity::class, GateEventEntity::class, SessionEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class MindfulDatabase : RoomDatabase() {
    abstract fun appEvents(): AppEventDao
    abstract fun gate(): GateDao

    companion object {
        fun create(context: Context): MindfulDatabase =
            Room.databaseBuilder(context, MindfulDatabase::class.java, "mindful.db").build()
    }
}
