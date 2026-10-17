package io.github.dangquanghuy277.mindfulhub.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Maps instants to "logical days" that start at [boundaryHour] instead of midnight.
 *
 * Facebook at 01:30 belongs to the day before: same budget, same review, and it counts as
 * after-bedtime use (ARCHITECTURE.md, "Today is a logical day"). Every budget, aggregate and
 * precommitment must go through this, never through `LocalDate.now()`.
 */
data class LogicalDayClock(val zone: ZoneId, val boundaryHour: Int = 4) {
    init {
        require(boundaryHour in 0..23) { "boundaryHour must be 0..23, was $boundaryHour" }
    }

    fun dayOf(instant: Instant): LocalDate =
        instant.atZone(zone).minusHours(boundaryHour.toLong()).toLocalDate()

    fun startOf(day: LocalDate): Instant = day.atTime(boundaryHour, 0).atZone(zone).toInstant()

    fun endOf(day: LocalDate): Instant = startOf(day.plusDays(1))

    /** Clock hours in logical-day order, e.g. 4, 5, … 23, 0, … 3 for a 04:00 boundary. */
    fun hoursInOrder(): List<Int> = (0 until 24).map { (boundaryHour + it) % 24 }
}
