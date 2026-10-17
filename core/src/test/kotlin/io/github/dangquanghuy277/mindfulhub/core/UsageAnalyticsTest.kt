package io.github.dangquanghuy277.mindfulhub.core

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class UsageAnalyticsTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val d17 = LocalDate.of(2026, 10, 17)
    private val d18 = LocalDate.of(2026, 10, 18)
    private fun at(d: Int, h: Int, m: Int) = ZonedDateTime.of(2026, 10, d, h, m, 0, 0, zone).toInstant()

    @Test
    fun `a session after midnight splits by hour into the previous logical day`() {
        val map = UsageAnalytics.heatmap(listOf(AppSession("fb", at(18, 0, 50), at(18, 1, 20))), clock, listOf(d17, d18))
        assertEquals(10.0, map[d17, 0], 1e-9)
        assertEquals(20.0, map[d17, 1], 1e-9)
        assertEquals(0.0, map[d18, 0], 1e-9)
    }

    @Test
    fun `a session across the 04_00 boundary lands in two logical days`() {
        val map = UsageAnalytics.heatmap(listOf(AppSession("fb", at(18, 3, 50), at(18, 4, 10))), clock, listOf(d17, d18))
        assertEquals(10.0, map[d17, 3], 1e-9)
        assertEquals(10.0, map[d18, 4], 1e-9)
    }

    @Test
    fun `days outside the window and filtered apps are ignored`() {
        val sessions = listOf(
            AppSession("fb", at(16, 10, 0), at(16, 10, 30)),
            AppSession("x", at(17, 10, 0), at(17, 10, 30)),
        )
        val map = UsageAnalytics.heatmap(sessions, clock, listOf(d17), include = { it == "fb" })
        assertEquals(0.0, map.total, 1e-9)
    }

    @Test
    fun `totals count minutes and opens per app, largest first`() {
        val sessions = listOf(
            AppSession("x", at(17, 9, 0), at(17, 9, 5)),
            AppSession("fb", at(17, 10, 0), at(17, 10, 30)),
            AppSession("fb", at(17, 11, 0), at(17, 11, 10)),
        )
        val totals = UsageAnalytics.totals(sessions, clock.startOf(d17), clock.endOf(d17))
        assertEquals(listOf(AppTotal("fb", 40.0, 2), AppTotal("x", 5.0, 1)), totals)
    }
}
