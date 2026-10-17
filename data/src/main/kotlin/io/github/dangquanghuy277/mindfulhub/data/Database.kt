package io.github.dangquanghuy277.mindfulhub.data

import android.content.Context
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

@Database(entities = [AppEventEntity::class], version = 1)
abstract class MindfulDatabase : RoomDatabase() {
    abstract fun appEvents(): AppEventDao

    companion object {
        fun create(context: Context): MindfulDatabase =
            Room.databaseBuilder(context, MindfulDatabase::class.java, "mindful.db").build()
    }
}
