package io.github.dangquanghuy277.mindfulhub.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import io.github.dangquanghuy277.mindfulhub.core.OfferStats

/**
 * D-025: one item shown on the "just bored" list, and whether you picked it. One row per item per
 * time chip per prompt, linked to the prompt or lock it was on, so "never saw it" and "saw it and
 * skipped it" stay apart. The list's order is learnt from these rows.
 */
@Entity(tableName = "instead_offer", indices = [Index("ts"), Index("gateEventId")])
data class InsteadOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val gateEventId: Long?,
    /** WORK / LUNCH / FREE, or NIGHT for the bedtime list. */
    val dayPart: String,
    /** The time chip it was shown under: QUICK / SHORT / LONG. */
    val moment: String,
    val itemKey: String,
    /** 0 = top of the list. */
    val position: Int,
    @ColumnInfo(defaultValue = "0") val picked: Boolean = false,
)

/** How many times each item was shown and picked since [from]. */
data class OfferCount(val itemKey: String, val shown: Int, val picked: Int)

@Dao
interface InsteadOfferDao {
    @Insert
    suspend fun insertAll(rows: List<InsteadOfferEntity>)

    @Query("UPDATE instead_offer SET picked = 1 WHERE gateEventId = :gateEventId AND moment = :moment AND itemKey = :itemKey")
    suspend fun markPicked(gateEventId: Long, moment: String, itemKey: String)

    @Query("SELECT itemKey, COUNT(*) AS shown, SUM(picked) AS picked FROM instead_offer WHERE ts >= :from GROUP BY itemKey")
    suspend fun countsSince(from: Long): List<OfferCount>
}

fun List<OfferCount>.toStats(): Map<String, OfferStats> = associate { it.itemKey to OfferStats(it.shown, it.picked) }
