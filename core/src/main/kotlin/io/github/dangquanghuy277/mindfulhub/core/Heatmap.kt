package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Minutes of use per (logical day, clock hour). Rows are days, columns follow [hours]. */
class Heatmap(
    val days: List<LocalDate>,
    val hours: List<Int>,
    private val minutes: Map<Pair<LocalDate, Int>, Double>,
) {
    operator fun get(day: LocalDate, hour: Int): Double = minutes[day to hour] ?: 0.0
    val total: Double get() = minutes.values.sum()

    /** Average minutes per day for each hour, in [hours] order. */
    fun averageByHour(): List<Double> = hours.map { h -> days.sumOf { this[it, h] } / days.size }
}

data class AppTotal(val pkg: String, val minutes: Double, val opens: Int)

object UsageAnalytics {
    /**
     * Splits each session at clock-hour boundaries. A segment's row is the logical day of its
     * start, so 01:00–02:00 lands in the previous day's row, in hour column 1.
     */
    fun heatmap(
        sessions: List<AppSession>,
        clock: LogicalDayClock,
        days: List<LocalDate>,
        include: (String) -> Boolean = { true },
    ): Heatmap {
        val wanted = days.toSet()
        val acc = HashMap<Pair<LocalDate, Int>, Double>()
        for (s in sessions) {
            if (!include(s.pkg)) continue
            var cursor = s.start
            while (cursor.isBefore(s.end)) {
                val local = cursor.atZone(clock.zone)
                val nextHour = local.truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant()
                val segEnd = minOf(nextHour, s.end)
                val day = clock.dayOf(cursor)
                if (day in wanted) {
                    val key = day to local.hour
                    acc[key] = (acc[key] ?: 0.0) + Duration.between(cursor, segEnd).toMillis() / 60_000.0
                }
                cursor = segEnd
            }
        }
        return Heatmap(days, clock.hoursInOrder(), acc)
    }

    /** Minutes and opens per app, for sessions that start inside [from, to). Sorted by minutes. */
    fun totals(sessions: List<AppSession>, from: Instant, to: Instant): List<AppTotal> =
        sessions.filter { !it.start.isBefore(from) && it.start.isBefore(to) }
            .groupBy { it.pkg }
            .map { (pkg, list) -> AppTotal(pkg, list.sumOf { it.duration.toMillis() } / 60_000.0, list.size) }
            .sortedByDescending { it.minutes }
}
