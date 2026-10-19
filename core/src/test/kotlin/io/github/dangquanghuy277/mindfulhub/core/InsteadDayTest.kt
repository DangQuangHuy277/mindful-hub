package io.github.dangquanghuy277.mindfulhub.core

import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** D-025: the "just bored" list follows the day and learns gently. */
class InsteadDayTest {
    private val schedule = Schedule()
    private fun tue(h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, 20, h, m)
    private fun sat(h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, 24, h, m)

    @Test
    fun `weekday hours split into work, lunch and free`() {
        assertEquals(DayPart.FREE, schedule.partAt(tue(9, 50)))
        assertEquals(DayPart.WORK, schedule.partAt(tue(10, 0)))
        assertEquals(DayPart.LUNCH, schedule.partAt(tue(11, 45)))
        assertEquals(DayPart.WORK, schedule.partAt(tue(13, 0)))
        assertEquals(DayPart.FREE, schedule.partAt(tue(19, 0)))
        assertEquals(DayPart.FREE, schedule.partAt(sat(11)))
    }

    @Test
    fun `at work only quick things are offered`() {
        assertEquals(listOf(Moment.QUICK), InsteadMenu.momentsFor(DayPart.WORK))
        val items = InsteadMenu.forMoment(Moment.QUICK, InsteadContext(part = DayPart.WORK, now = LocalTime.of(15, 0)))
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.minutes <= 5 && DayPart.WORK in it.parts })
        assertTrue(items.none { it.exercise })
    }

    @Test
    fun `lunch offers the nap, the article and eating with the team`() {
        val ctx = InsteadContext(part = DayPart.LUNCH, now = LocalTime.of(12, 0))
        val keys = Moment.entries.flatMap { InsteadMenu.forMoment(it, ctx) }.map { it.key }
        assertTrue(listOf("nap_15", "read", "eat_team").all { it in keys })
    }

    @Test
    fun `exercise waits an hour after meals and stops 90 minutes before bed`() {
        fun ok(h: Int, m: Int = 0, toBed: Long = 300) =
            InsteadMenu.exerciseOk(InsteadContext(now = LocalTime.of(h, m), minutesToBed = toBed))
        assertFalse(ok(13, 30)) // lunch ended 13:00
        assertTrue(ok(14, 0))
        assertFalse(ok(21, 0)) // dinner ended 20:30
        assertTrue(ok(21, 30))
        assertFalse(ok(0, 45, toBed = 75))
        val evening = InsteadMenu.forMoment(Moment.QUICK, InsteadContext(now = LocalTime.of(22, 0), minutesToBed = 240))
        assertTrue(evening.any { it.key == "pushups" })
        val afterDinner = InsteadMenu.forMoment(Moment.QUICK, InsteadContext(now = LocalTime.of(20, 0), minutesToBed = 360))
        assertTrue(afterDinner.none { it.exercise })
    }

    @Test
    fun `picked things move up, skipped ones sink but stay`() {
        val base = InsteadMenu.forMoment(Moment.SHORT, InsteadContext(now = LocalTime.of(22, 0), minutesToBed = 240))
        val last = base.last().key
        val first = base.first().key
        val stats = mapOf(last to OfferStats(shown = 6, picked = 4), first to OfferStats(shown = 8, picked = 0))
        val ranked = InsteadMenu.forMoment(Moment.SHORT, InsteadContext(now = LocalTime.of(22, 0), minutesToBed = 240, stats = stats))
        assertEquals(last, ranked.first().key)
        assertEquals(first, ranked.last().key)
        assertEquals(base.toSet(), ranked.toSet())
    }

    @Test
    fun `one skip ranks an item below ones never offered, nothing more`() {
        val ctx = InsteadContext(now = LocalTime.of(22, 0), minutesToBed = 240)
        val base = InsteadMenu.forMoment(Moment.SHORT, ctx)
        val ranked = InsteadMenu.forMoment(Moment.SHORT, ctx.copy(stats = mapOf(base.first().key to OfferStats(1, 0))))
        assertEquals(base.drop(1), ranked.take(base.size - 1))
    }

    @Test
    fun `schedule times come from settings`() {
        val s = SettingsCodec.decode(mapOf(SettingsCodec.WORK_START to "10:30", SettingsCodec.DINNER_START to "20:00"))
        assertEquals(LocalTime.of(10, 30), s.schedule.workStart)
        assertEquals(LocalTime.of(20, 0), s.schedule.dinnerStart)
        assertEquals(LocalTime.of(19, 0), s.schedule.workEnd)
    }
}
