package io.github.dangquanghuy277.mindfulhub.core

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Which part of the day it is, for the "just bored" list (D-025). At [WORK] there's only time for
 * something quick; [LUNCH] has up to half an hour; [FREE] is evenings, mornings and weekends.
 */
enum class DayPart { WORK, LUNCH, FREE }

/** Your week (D-025): office hours on [workDays], with lunch and dinner. Set in Settings. */
data class Schedule(
    val workStart: LocalTime = LocalTime.of(10, 0),
    val workEnd: LocalTime = LocalTime.of(19, 0),
    /** Lunch with the team can start at 11:45. */
    val lunchStart: LocalTime = LocalTime.of(11, 45),
    val lunchEnd: LocalTime = LocalTime.of(13, 0),
    val dinnerStart: LocalTime = LocalTime.of(19, 30),
    val dinnerEnd: LocalTime = LocalTime.of(20, 30),
    val workDays: Set<DayOfWeek> = DayOfWeek.entries.toSet() - DayOfWeek.SATURDAY - DayOfWeek.SUNDAY,
) {
    fun partAt(at: LocalDateTime): DayPart {
        if (at.dayOfWeek !in workDays) return DayPart.FREE
        val t = at.toLocalTime()
        return when {
            t.inWindow(lunchStart, lunchEnd) -> DayPart.LUNCH
            t.inWindow(workStart, workEnd) -> DayPart.WORK
            else -> DayPart.FREE
        }
    }

    /**
     * True from the start of a meal until an hour after it ends, any day of the week: no push-ups
     * on a full stomach. A light walk is fine then, and even helps (D-025).
     */
    fun nearMeal(t: LocalTime): Boolean =
        t.inWindow(lunchStart, lunchEnd.plusMinutes(AFTER_MEAL_MIN)) || t.inWindow(dinnerStart, dinnerEnd.plusMinutes(AFTER_MEAL_MIN))

    companion object {
        const val AFTER_MEAL_MIN = 60L
    }
}

/** [start, end), for windows that don't cross midnight. */
private fun LocalTime.inWindow(start: LocalTime, end: LocalTime): Boolean = !isBefore(start) && isBefore(end)
