package io.github.dangquanghuy277.mindfulhub.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

// D-030 morning nudge and F75 reaction check, schema v7. Each has its own tables, so dropping
// either is a migration that drops them and nothing else.

/**
 * D-030: the one morning notification of logical [day]. Its outcome is read from the columns:
 * tapped, dismissed (swiped away), or neither by [expiresAt] (cleared itself when work started).
 * Answering the check-in is in `sleep_checkin`, joined by night = day − 1.
 */
@Entity(tableName = "morning_nudge")
data class MorningNudgeEntity(
    @PrimaryKey val day: String,
    /** The unlock that triggered it: the live unlock broadcast, or the first UNLOCK in the usage history. */
    val firstUnlockAt: Long?,
    val postedAt: Long,
    val expiresAt: Long?,
    /** "unlock" (the broadcast) or "worker" (the hourly fallback found the unlock in the history). */
    val source: String,
    val tappedAt: Long? = null,
    val dismissedAt: Long? = null,
)

@Dao
interface MorningNudgeDao {
    /** Keeps the first; a second post on the same day is ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: MorningNudgeEntity): Long

    @Query("SELECT * FROM morning_nudge WHERE day = :day")
    suspend fun get(day: String): MorningNudgeEntity?

    @Query("UPDATE morning_nudge SET tappedAt = :at WHERE day = :day AND tappedAt IS NULL")
    suspend fun tapped(day: String, at: Long)

    @Query("UPDATE morning_nudge SET dismissedAt = :at WHERE day = :day AND dismissedAt IS NULL")
    suspend fun dismissed(day: String, at: Long)
}

/**
 * F75: one attempt at the 3-minute reaction check. Abandoned attempts are kept ([completed] false):
 * "started and left" is data. [events] is the raw run (`PvtScoring.encode`), so every number here
 * can be recomputed under a better rule; the scores are a convenience.
 */
@Entity(tableName = "reaction_run", indices = [Index("day")])
data class ReactionRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String,
    val startedAt: Long,
    val endedAt: Long,
    val completed: Boolean,
    /** `Pvtb.VERSION`: the rules the run was played under. */
    val protocolVersion: Int,
    val events: String,
    val stimuli: Int?,
    val meanSpeed: Double?,
    val medianRtMs: Double?,
    val lapses: Int?,
    val falseStarts: Int?,
    val fastest10RtMs: Double?,
    val slowest10Speed: Double?,
    val performance: Double?,
    /** The first unlock this morning, so grogginess (time since waking) can be accounted for. */
    val firstUnlockAt: Long?,
    /** Touch and screen latency differ between phones: compare runs on the same one only. */
    val device: String,
    val refreshHz: Float,
    val gitSha: String,
)

/** F75: the first time the check's card was shown on a day, so "never offered" ≠ "skipped". */
@Entity(tableName = "reaction_offer")
data class ReactionOfferEntity(@PrimaryKey val day: String, val shownAt: Long)

@Dao
interface ReactionDao {
    @Insert
    suspend fun insertRun(e: ReactionRunEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun offered(e: ReactionOfferEntity)

    @Query("SELECT COUNT(*) FROM reaction_run WHERE day = :day AND completed = 1")
    suspend fun completedOn(day: String): Int

    /** Each earlier day's first completed run, oldest first: the basis for "your usual". */
    @Query(
        "SELECT medianRtMs FROM reaction_run r WHERE completed = 1 AND day < :day AND medianRtMs IS NOT NULL " +
            "AND id = (SELECT MIN(id) FROM reaction_run WHERE day = r.day AND completed = 1) ORDER BY day",
    )
    suspend fun earlierMedians(day: String): List<Double>
}
