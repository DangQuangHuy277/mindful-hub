package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

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

/**
 * The morning before work (D-029, D-030): from an hour before the planned wake time (never before
 * the 04:00 day boundary) until work starts on a work day, or until 14:00 on a free day. A 05:00
 * check of the phone mid-night doesn't count as morning. The D-030 nudge and the F75 reaction
 * check live inside it, so nothing asks for a tap during work hours.
 */
object MorningWindow {
    val BEFORE_WAKE: Duration = Duration.ofMinutes(60)

    /** [start, end) for logical [day], or null when it's empty (a wake time after work starts). */
    fun of(day: LocalDate, s: Settings, clock: LogicalDayClock): ClosedRange<Instant>? {
        val wake = clock.instantOn(day, s.sleepTimes(day.minusDays(1)).wake)
        val start = maxOf(clock.startOf(day), wake.minus(BEFORE_WAKE))
        val workDay = day.dayOfWeek in s.schedule.workDays
        val end = clock.instantOn(day, if (workDay) s.schedule.workStart else LocalTime.of(MorningCheckIn.UNTIL_HOUR, 0))
        return if (end.isAfter(start)) start..end else null
    }

    fun contains(day: LocalDate, s: Settings, clock: LogicalDayClock, at: Instant): Boolean =
        of(day, s, clock)?.let { at >= it.start && at < it.endInclusive } ?: false
}
