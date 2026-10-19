package io.github.dangquanghuy277.mindfulhub.core

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/** A session opened through the gate, as stored (purpose, planned box, rating). */
data class GateBox(
    val pkg: String,
    val purpose: Purpose,
    val start: Instant,
    val plannedEnd: Instant,
    /** 1 = 👍, 0 = 😐, -1 = 👎, null = not rated. */
    val rating: Int? = null,
    val endedBy: String? = null,
    val end: Instant? = null,
) {
    /** A box opened with a purpose and not rated 👎 is "intended" time (D-004). */
    val intended: Boolean get() = purpose != Purpose.JUST_BORED && rating != -1

    /** F10: left on your own at least a minute before the box ended. */
    val leftEarly: Boolean
        get() = endedBy == "USER" && end != null && Duration.between(end, plannedEnd) >= Duration.ofMinutes(1)
}

/**
 * G1, the headline metric (D-004): minutes in gated apps that you didn't intend. That's every
 * gated minute *outside* a box you opened with a purpose: "just bored" boxes, time past a box, boxes
 * rated 👎, and any use while the gate was off. One definition, so the review, the comeback card and
 * the findings can't disagree.
 */
object UnintendedTime {
    fun minutes(usage: List<AppSession>, gated: Set<String>, boxes: List<GateBox>, from: Instant, to: Instant): Double {
        val intended = boxes.filter { it.intended }.groupBy { it.pkg }
        var total = 0.0
        for (s in usage) {
            if (s.pkg !in gated) continue
            val start = maxOf(s.start, from)
            val end = minOf(s.end, to)
            if (!end.isAfter(start)) continue
            val covered = intended[s.pkg].orEmpty().sumOf { b -> overlap(start, end, b.start, b.plannedEnd) }
            total += (minutesOf(start, end) - covered).coerceAtLeast(0.0)
        }
        return total
    }

    fun byDay(usage: List<AppSession>, gated: Set<String>, boxes: List<GateBox>, clock: LogicalDayClock, days: List<LocalDate>): Map<LocalDate, Double> =
        days.associateWith { minutes(usage, gated, boxes, clock.startOf(it), clock.endOf(it)) }

    private fun overlap(aStart: Instant, aEnd: Instant, bStart: Instant, bEnd: Instant): Double {
        val s = maxOf(aStart, bStart)
        val e = minOf(aEnd, bEnd)
        return if (e.isAfter(s)) minutesOf(s, e) else 0.0
    }

    private fun minutesOf(a: Instant, b: Instant) = Duration.between(a, b).toMillis() / 60_000.0
}

/** Weeks run Monday to Sunday, in logical days. The review is on Sunday (F3). */
object Weeks {
    fun startOf(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun daysOf(weekStart: LocalDate): List<LocalDate> = (0L..6L).map(weekStart::plusDays)
}

/** The week in numbers, for the review (F11). Every field is a fact, never a verdict (P33). */
data class WeekSummary(
    val weekStart: LocalDate,
    val unintendedMinutes: Double,
    val afterBedtimeMinutes: Double,
    val worthIt: Int,
    val soSo: Int,
    val notWorthIt: Int,
    val leftEarly: Int,
    val goodMinutes: Map<Category, Int>,
    /** F29 leak watch: minutes in apps that aren't gated, this week and the week before. */
    val ungatedMinutes: Double,
    val ungatedMinutesBefore: Double?,
    val unintendedBefore: Double?,
)

object WeekReview {
    fun summarize(
        weekStart: LocalDate,
        now: Instant,
        clock: LogicalDayClock,
        usage: List<AppSession>,
        gated: Set<String>,
        boxes: List<GateBox>,
        good: List<GoodThing>,
        bedtime: LocalTime,
        historyFrom: Instant?,
    ): WeekSummary {
        val days = Weeks.daysOf(weekStart)
        val from = clock.startOf(days.first())
        val to = minOf(now, clock.endOf(days.last()))
        val before = Weeks.daysOf(weekStart.minusWeeks(1))
        val beforeFrom = clock.startOf(before.first())
        val haveBefore = historyFrom != null && !historyFrom.isAfter(beforeFrom)
        val inWeek = boxes.filter { !it.start.isBefore(from) && it.start.isBefore(to) }
        val afterBed = UsageInsights.dayUse(usage, clock, days, bedtime) { it in gated }.sumOf { it.afterBedtime }
        return WeekSummary(
            weekStart = weekStart,
            unintendedMinutes = UnintendedTime.minutes(usage, gated, boxes, from, to),
            afterBedtimeMinutes = afterBed,
            worthIt = inWeek.count { it.rating == 1 },
            soSo = inWeek.count { it.rating == 0 },
            notWorthIt = inWeek.count { it.rating == -1 },
            leftEarly = inWeek.count { it.leftEarly },
            goodMinutes = GoodThings.minutesByCategory(good, clock, days),
            ungatedMinutes = ungated(usage, gated, from, to),
            ungatedMinutesBefore = if (haveBefore) ungated(usage, gated, beforeFrom, clock.endOf(before.last())) else null,
            unintendedBefore = if (haveBefore) UnintendedTime.minutes(usage, gated, boxes, beforeFrom, clock.endOf(before.last())) else null,
        )
    }

    private fun ungated(usage: List<AppSession>, gated: Set<String>, from: Instant, to: Instant): Double =
        usage.filter { it.pkg !in gated }.sumOf { s ->
            val a = maxOf(s.start, from)
            val b = minOf(s.end, to)
            if (b.isAfter(a)) Duration.between(a, b).toMillis() / 60_000.0 else 0.0
        }
}

/**
 * WHO-5 Well-Being Index (F12, A3): five statements about the last two weeks, each 0–5. The raw
 * sum × 4 gives 0–100. A score of 50 or below is the usual cut-off for "worth looking at".
 */
object Who5 {
    val items = listOf(
        "I have felt cheerful and in good spirits",
        "I have felt calm and relaxed",
        "I have felt active and vigorous",
        "I woke up feeling fresh and rested",
        "My daily life has been filled with things that interest me",
    )
    val scale = listOf("At no time", "Some of the time", "Less than half the time", "More than half the time", "Most of the time", "All of the time")

    fun score(answers: List<Int>): Int? =
        if (answers.size == items.size && answers.all { it in 0..5 }) answers.sum() * 4 else null

    const val LOW = 50
}

/**
 * F32: a slip is a day clearly above your own normal, judged against you, never against others.
 * It needs a few days of history, and a minimum, so a quiet week doesn't make 12 minutes a "slip".
 */
object Comeback {
    const val MIN_SLIP_MINUTES = 30.0
    const val SLIP_FACTOR = 1.3

    fun wasSlip(byDay: Map<LocalDate, Double>, day: LocalDate): Boolean {
        val value = byDay[day] ?: return false
        val before = (1L..7L).mapNotNull { byDay[day.minusDays(it)] }
        if (before.size < 3) return false
        val median = before.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }
        return value >= MIN_SLIP_MINUTES && value > median * SLIP_FACTOR
    }

    fun isFreshStart(today: LocalDate): Boolean = today.dayOfWeek == DayOfWeek.MONDAY
}
