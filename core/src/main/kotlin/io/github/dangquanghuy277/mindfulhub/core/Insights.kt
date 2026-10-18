package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** One logical day of use, split at the bedtime target (G7: minutes after bedtime). */
data class DayUse(val day: LocalDate, val minutes: Double, val afterBedtime: Double)

/**
 * A finding stated in words on the usage screen (D-018). Charts alone rarely lead to reflection;
 * a short sentence next to the chart does (Strömel 2024, Choe 2017). The cost is stated plainly
 * (TimeAware, Kim 2016) and paired with a win, which that study's stress finding argues for.
 */
sealed interface Insight {
    /** Average use after [bedtime] over [nights], which are complete nights only. */
    data class AfterBedtime(val bedtime: LocalTime, val averageMinutes: Double, val nights: List<DayUse>) : Insight

    /** [pkg] was opened within minutes of waking on [mornings] of [outOf] mornings. */
    data class FirstAfterWaking(
        val pkg: String, val mornings: Int, val outOf: Int, val earliest: LocalTime, val latest: LocalTime,
    ) : Insight

    /** Same span of the week so far against the same span a week earlier. */
    data class WeekChange(val thisWeek: Double, val lastWeek: Double) : Insight

    data class LongestSession(val session: AppSession, val day: LocalDate) : Insight

    data class PeakHour(val hour: Int, val minutesPerDay: Double) : Insight

    /** Today's gate pauses and how many ended without going in. */
    data class PauseWins(val prompts: Int, val abandoned: Int) : Insight
}

class InsightInput(
    /** Sessions of every app worth counting (launcher, system UI and this app removed). */
    val sessions: List<AppSession>,
    /** The apps the insights are about, e.g. the gated ones. Waking is detected from all [sessions]. */
    val include: (String) -> Boolean,
    val clock: LogicalDayClock,
    val days: List<LocalDate>,
    /** The 7 days before [days], or null when history doesn't fully cover them. */
    val previousDays: List<LocalDate>?,
    val bedtime: LocalTime,
    val now: Instant,
    val prompts: Int = 0,
    val abandoned: Int = 0,
)

object UsageInsights {
    /** D-007 target (00:00–00:30) until the sleep plan (F13) owns it. */
    val DEFAULT_BEDTIME: LocalTime = LocalTime.of(0, 30)

    /** A phone-free gap at least this long ends in "waking". */
    val SLEEP_GAP: Duration = Duration.ofHours(3)
    val AFTER_WAKING: Duration = Duration.ofMinutes(10)

    private const val MIN_LONGEST_MINUTES = 20.0
    private const val MIN_PEAK_MINUTES = 5.0
    private const val MIN_LAST_WEEK_MINUTES = 10.0

    fun dayUse(
        sessions: List<AppSession>,
        clock: LogicalDayClock,
        days: List<LocalDate>,
        bedtime: LocalTime,
        include: (String) -> Boolean = { true },
    ): List<DayUse> = days.map { day ->
        val start = clock.startOf(day)
        val end = clock.endOf(day)
        val bed = clock.instantOn(day, bedtime)
        var total = 0.0
        var late = 0.0
        for (s in sessions) {
            if (!include(s.pkg)) continue
            total += overlapMinutes(s, start, end)
            late += overlapMinutes(s, bed, end)
        }
        DayUse(day, total, late)
    }

    /** Most telling first; the win always comes last. */
    fun insights(input: InsightInput): List<Insight> = buildList {
        val included = input.sessions.filter { input.include(it.pkg) }
        afterBedtime(input, included)?.let(::add)
        firstAfterWaking(input)?.let(::add)
        weekChange(input, included)?.let(::add)
        longestSession(input, included)?.let(::add)
        peakHour(input, included)?.let(::add)
        if (input.prompts > 0) add(Insight.PauseWins(input.prompts, input.abandoned))
    }

    private fun afterBedtime(input: InsightInput, included: List<AppSession>): Insight.AfterBedtime? {
        val nights = dayUse(included, input.clock, input.days, input.bedtime)
            .filter { !input.clock.endOf(it.day).isAfter(input.now) }
        if (nights.isEmpty()) return null
        val average = nights.sumOf { it.afterBedtime } / nights.size
        return if (average >= 1.0) Insight.AfterBedtime(input.bedtime, average, nights) else null
    }

    /**
     * Waking = the end of a phone-free gap of at least [SLEEP_GAP] that spans the day boundary
     * (04:00), so a quiet afternoon is never mistaken for sleep and each day has at most one wake.
     * Reports the included app most often opened within [AFTER_WAKING] of waking, if that's at
     * least half of the mornings.
     */
    private fun firstAfterWaking(input: InsightInput): Insight.FirstAfterWaking? {
        val sorted = input.sessions.sortedBy { it.start }
        val wakes = mutableListOf<Instant>()
        for (i in 1 until sorted.size) {
            val (prev, next) = sorted[i - 1] to sorted[i]
            val day = input.clock.dayOf(next.start)
            if (day in input.days && input.clock.dayOf(prev.end) != day &&
                Duration.between(prev.end, next.start) >= SLEEP_GAP
            ) {
                wakes += next.start
            }
        }
        if (wakes.isEmpty()) return null
        val firstOpens = wakes.mapNotNull { wake ->
            val until = wake.plus(AFTER_WAKING)
            sorted.firstOrNull { !it.start.isBefore(wake) && !it.start.isAfter(until) && input.include(it.pkg) }
        }
        val (pkg, opens) = firstOpens.groupBy { it.pkg }.maxByOrNull { it.value.size } ?: return null
        if (opens.size < 2 || opens.size * 2 < wakes.size) return null
        val times = opens.map { it.start.atZone(input.clock.zone).toLocalTime().withSecond(0).withNano(0) }
        return Insight.FirstAfterWaking(pkg, opens.size, wakes.size, times.min(), times.max())
    }

    private fun weekChange(input: InsightInput, included: List<AppSession>): Insight.WeekChange? {
        val previous = input.previousDays ?: return null
        val thisWeek = included.sumOf { overlapMinutes(it, input.clock.startOf(input.days.first()), input.now) }
        val lastWeek = included.sumOf {
            overlapMinutes(it, input.clock.startOf(previous.first()), input.now.minus(Duration.ofDays(7)))
        }
        return if (lastWeek >= MIN_LAST_WEEK_MINUTES) Insight.WeekChange(thisWeek, lastWeek) else null
    }

    private fun longestSession(input: InsightInput, included: List<AppSession>): Insight.LongestSession? {
        val longest = included.filter { input.clock.dayOf(it.start) in input.days }.maxByOrNull { it.duration } ?: return null
        if (longest.duration.toMillis() / 60_000.0 < MIN_LONGEST_MINUTES) return null
        return Insight.LongestSession(longest, input.clock.dayOf(longest.start))
    }

    private fun peakHour(input: InsightInput, included: List<AppSession>): Insight.PeakHour? {
        val map = UsageAnalytics.heatmap(included, input.clock, input.days)
        val averages = map.averageByHour()
        val i = averages.indices.maxByOrNull { averages[it] } ?: return null
        return if (averages[i] >= MIN_PEAK_MINUTES) Insight.PeakHour(map.hours[i], averages[i]) else null
    }

    private fun overlapMinutes(s: AppSession, from: Instant, to: Instant): Double {
        val start = maxOf(s.start, from)
        val end = minOf(s.end, to)
        return if (end.isAfter(start)) Duration.between(start, end).toMillis() / 60_000.0 else 0.0
    }
}
