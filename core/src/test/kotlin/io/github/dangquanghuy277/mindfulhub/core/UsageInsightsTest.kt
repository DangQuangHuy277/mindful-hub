package io.github.dangquanghuy277.mindfulhub.core

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UsageInsightsTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val bedtime = LocalTime.of(0, 30)
    private fun at(d: Int, h: Int, m: Int) = ZonedDateTime.of(2026, 10, d, h, m, 0, 0, zone).toInstant()
    private fun day(d: Int) = LocalDate.of(2026, 10, d)
    private val week = (12..18).map(::day)

    private fun input(
        sessions: List<AppSession>, now: java.time.Instant = at(18, 14, 0), previous: List<LocalDate>? = null,
        prompts: Int = 0, abandoned: Int = 0,
    ) = InsightInput(sessions, { it == "fb" || it == "x" }, clock, week, previous, bedtime, now, prompts, abandoned)

    private inline fun <reified T : Insight> List<Insight>.find(): T? = filterIsInstance<T>().firstOrNull()

    @Test
    fun `instantOn puts times before the boundary on the next date`() {
        assertEquals(at(13, 0, 30), clock.instantOn(day(12), LocalTime.of(0, 30)))
        assertEquals(at(12, 22, 0), clock.instantOn(day(12), LocalTime.of(22, 0)))
    }

    @Test
    fun `day use splits at bedtime and at the 04_00 boundary`() {
        val s = listOf(AppSession("fb", at(13, 0, 0), at(13, 1, 0)), AppSession("fb", at(13, 3, 50), at(13, 4, 10)))
        val (mon, tue) = UsageInsights.dayUse(s, clock, listOf(day(12), day(13)), bedtime)
        assertEquals(DayUse(day(12), 70.0, 40.0), mon)
        assertEquals(DayUse(day(13), 10.0, 0.0), tue)
    }

    @Test
    fun `after bedtime averages complete nights only`() {
        val s = listOf(
            AppSession("fb", at(13, 1, 0), at(13, 2, 0)), // Mon night: 60
            AppSession("fb", at(18, 1, 0), at(18, 1, 30)), // Sat night: 30
            AppSession("chrome", at(16, 1, 0), at(16, 3, 0)), // not included
        )
        val i = UsageInsights.insights(input(s)).find<Insight.AfterBedtime>()!!
        assertEquals(6, i.nights.size) // Sun 18 hasn't ended at 14:00
        assertEquals(15.0, i.averageMinutes, 1e-9)
    }

    @Test
    fun `first app after waking needs half the mornings`() {
        val s = (13..16).flatMap { d ->
            listOf(AppSession("chrome", at(d, 1, 0), at(d, 1, 10)), AppSession(if (d == 16) "x" else "fb", at(d, 9, 45), at(d, 9, 50)))
        }
        val i = UsageInsights.insights(input(s)).find<Insight.FirstAfterWaking>()!!
        assertEquals("fb", i.pkg)
        assertEquals(3, i.mornings)
        assertEquals(4, i.outOf)
        assertEquals(LocalTime.of(9, 45), i.earliest)
    }

    @Test
    fun `an app opened later than ten minutes after waking does not count`() {
        val s = (13..16).flatMap { d ->
            listOf(
                AppSession("chrome", at(d, 1, 0), at(d, 1, 10)),
                AppSession("chrome", at(d, 9, 45), at(d, 9, 50)),
                AppSession("fb", at(d, 10, 0), at(d, 10, 5)),
            )
        }
        assertNull(UsageInsights.insights(input(s)).find<Insight.FirstAfterWaking>())
    }

    @Test
    fun `week change compares the same span a week earlier`() {
        val s = listOf(
            AppSession("fb", at(5, 10, 0), at(5, 11, 0)), // last week, before the cut-off: 60
            AppSession("fb", at(11, 15, 0), at(11, 16, 0)), // last week, after 14:00 Sun 11: not counted
            AppSession("fb", at(12, 10, 0), at(12, 10, 30)), // this week: 30
        )
        val i = UsageInsights.insights(input(s, previous = (5..11).map(::day))).find<Insight.WeekChange>()!!
        assertEquals(30.0, i.thisWeek, 1e-9)
        assertEquals(60.0, i.lastWeek, 1e-9)
        assertNull(UsageInsights.insights(input(s)).find<Insight.WeekChange>())
    }

    @Test
    fun `longest session and peak hour ignore small numbers`() {
        val short = listOf(AppSession("fb", at(16, 22, 0), at(16, 22, 10)))
        val shortInsights = UsageInsights.insights(input(short))
        assertNull(shortInsights.find<Insight.LongestSession>())
        assertNull(shortInsights.find<Insight.PeakHour>())

        val long = listOf(AppSession("fb", at(17, 1, 4), at(17, 2, 16)), AppSession("fb", at(16, 22, 0), at(16, 23, 0)))
        val i = UsageInsights.insights(input(long))
        assertEquals(day(16), i.find<Insight.LongestSession>()!!.day)
        assertEquals(22, i.find<Insight.PeakHour>()!!.hour)
    }

    @Test
    fun `the pause win comes last and only when there were pauses`() {
        val s = listOf(AppSession("fb", at(13, 1, 0), at(13, 2, 0)))
        assertIs<Insight.PauseWins>(UsageInsights.insights(input(s, prompts = 4, abandoned = 2)).last())
        assertTrue(UsageInsights.insights(input(s)).none { it is Insight.PauseWins })
    }
}
