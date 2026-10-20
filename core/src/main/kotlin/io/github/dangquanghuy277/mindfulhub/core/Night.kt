package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class SleepTimes(val bedtime: LocalTime, val wake: LocalTime)

/**
 * F13 + F44 (trial): the sleep shift as a short quest. Chapter 1 is where you are (D-007:
 * about 02:00 → 09:45); each chapter moves the wake time 15 min earlier and bedtime follows, every
 * 3 days. Chapter 7 is the target, 00:30 → 08:15. "Hold this chapter" adds 3 days instead of
 * failing anything (P33): a step that didn't stick simply repeats.
 */
object SleepQuest {
    val START = SleepTimes(LocalTime.of(2, 0), LocalTime.of(9, 45))
    val STEP: Duration = Duration.ofMinutes(15)
    const val DAYS_PER_CHAPTER = 3
    const val CHAPTERS = 7

    fun chapter(start: LocalDate, holdDays: Int, today: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(start, today).toInt() - holdDays
        return (days.coerceAtLeast(0) / DAYS_PER_CHAPTER + 1).coerceAtMost(CHAPTERS)
    }

    fun times(chapter: Int): SleepTimes {
        val back = STEP.multipliedBy((chapter - 1).toLong())
        return SleepTimes(START.bedtime.minus(back), START.wake.minus(back))
    }

    /** Days left in this chapter, counting today. */
    fun daysLeft(start: LocalDate, holdDays: Int, today: LocalDate): Int {
        val days = (ChronoUnit.DAYS.between(start, today).toInt() - holdDays).coerceAtLeast(0)
        return DAYS_PER_CHAPTER - days % DAYS_PER_CHAPTER
    }
}

/** Tonight's times: the quest's chapter when the quest is on and started, else your manual times. */
fun Settings.sleepTimes(today: LocalDate): SleepTimes {
    val start = sleepQuestStart
    return if (on(Feature.SLEEP_QUEST) && start != null) {
        SleepQuest.times(SleepQuest.chapter(start, sleepQuestHoldDays, today))
    } else {
        SleepTimes(bedtime, wakeTime)
    }
}

enum class NightPhase { DAY, WIND_DOWN, AFTER_BEDTIME }

/** Where [now] falls in the night that belongs to logical day [night]. */
data class NightState(val phase: NightPhase, val night: LocalDate, val bed: Instant, val wake: Instant)

/**
 * The night of logical day D runs from wind-down (bedtime minus [Settings.windDownMinutes]) to the
 * wake time after that bedtime. At 06:00 the logical day has already turned (04:00 boundary), so
 * yesterday's night is checked too: the lock holds until you wake, not until 04:00.
 */
object NightClock {
    fun state(now: Instant, clock: LogicalDayClock, settings: Settings): NightState {
        val today = clock.dayOf(now)
        for (night in listOf(today, today.minusDays(1))) {
            val times = settings.sleepTimes(night)
            val bed = clock.instantOn(night, times.bedtime)
            val wake = nextAfter(bed, times.wake, clock)
            val windDown = bed.minus(Duration.ofMinutes(settings.windDownMinutes.toLong()))
            if (!now.isBefore(windDown) && now.isBefore(wake)) {
                val phase = if (now.isBefore(bed)) NightPhase.WIND_DOWN else NightPhase.AFTER_BEDTIME
                return NightState(phase, night, bed, wake)
            }
        }
        val times = settings.sleepTimes(today)
        val bed = clock.instantOn(today, times.bedtime)
        return NightState(NightPhase.DAY, today, bed, nextAfter(bed, times.wake, clock))
    }

    /** The first [time] strictly after [from], in the clock's zone. */
    internal fun nextAfter(from: Instant, time: LocalTime, clock: LogicalDayClock): Instant {
        val zoned = from.atZone(clock.zone)
        val same = zoned.toLocalDate().atTime(time).atZone(clock.zone).toInstant()
        return if (same.isAfter(from)) same else zoned.toLocalDate().plusDays(1).atTime(time).atZone(clock.zone).toInstant()
    }
}
