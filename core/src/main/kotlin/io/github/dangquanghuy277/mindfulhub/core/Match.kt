package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * F72: a late match you planned. [night] is the logical day the night belongs to, so a 02:00
 * kickoff on night Sat is Sunday 02:00 on the clock (LogicalDayClock.instantOn).
 */
data class MatchNight(val night: LocalDate, val start: LocalTime, val minutes: Int)

/**
 * F72 (D-027): match nights loosen the wind-down lock, so adding one waits a logical day like any
 * other loosening (P2): plan the match a day ahead. Removing one applies at once.
 */
object MatchNights {
    /** Kick-off margin and the walk back to bed: the window is the match plus this on each side. */
    val MARGIN: Duration = Duration.ofMinutes(15)

    fun encode(list: List<MatchNight>): String =
        list.distinct().sortedWith(compareBy({ it.night }, { it.start })).joinToString(",") { "${it.night} ${it.start} ${it.minutes}" }

    fun decode(value: String?): List<MatchNight> =
        value.orEmpty().split(",").filter { it.isNotBlank() }.mapNotNull { item ->
            runCatching {
                val (day, time, minutes) = item.trim().split(" ")
                MatchNight(LocalDate.parse(day), LocalTime.parse(time), minutes.toInt())
            }.getOrNull()
        }

    fun window(m: MatchNight, clock: LogicalDayClock): Pair<Instant, Instant> {
        val start = clock.instantOn(m.night, m.start)
        return start.minus(MARGIN) to start.plus(Duration.ofMinutes(m.minutes.toLong())).plus(MARGIN)
    }

    /** True if [new] plans a match [old] didn't have. */
    fun adds(old: String?, new: String): Boolean = (decode(new) - decode(old).toSet()).isNotEmpty()
}

/** Tonight's planned match, when F72 is on. */
fun Settings.matchOn(night: LocalDate): MatchNight? =
    if (on(Feature.MATCH_NIGHT)) matchNights.firstOrNull { it.night == night } else null
