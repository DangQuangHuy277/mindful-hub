package io.github.dangquanghuy277.mindfulhub.core

import java.time.Instant
import java.time.LocalDate

/** F65: the sleep-latency answer, in the Consensus Sleep Diary's spirit but as buckets (two taps). */
enum class SleepLatency(val label: String) {
    UNDER_15("under 15 min"),
    FROM_15_TO_30("15–30 min"),
    FROM_30_TO_60("30–60 min"),
    OVER_60("over an hour"),
}

/**
 * F65: a two-tap morning check-in, asked before any sleep numbers are shown so they can't colour
 * the answer (docs/sleep/RESEARCH.md, F65). Skipping is fine and nothing reminds you.
 */
object MorningCheckIn {
    const val UNTIL_HOUR = 14

    /** The night to ask about at [now]: last night, from the new logical day until 14:00; otherwise none. */
    fun nightToAsk(now: Instant, clock: LogicalDayClock): LocalDate? {
        val hour = now.atZone(clock.zone).hour
        if (hour < clock.boundaryHour || hour >= UNTIL_HOUR) return null
        return clock.dayOf(now).minusDays(1)
    }
}
