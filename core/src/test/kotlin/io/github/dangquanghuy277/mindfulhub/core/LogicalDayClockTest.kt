package io.github.dangquanghuy277.mindfulhub.core

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class LogicalDayClockTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private fun at(d: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 10, d, h, m, 0, 0, zone).toInstant()

    @Test
    fun `01_30 belongs to the previous day`() =
        assertEquals(LocalDate.of(2026, 10, 17), clock.dayOf(at(18, 1, 30)))

    @Test
    fun `03_59 is still the previous day and 04_00 starts the next`() {
        assertEquals(LocalDate.of(2026, 10, 17), clock.dayOf(at(18, 3, 59)))
        assertEquals(LocalDate.of(2026, 10, 18), clock.dayOf(at(18, 4, 0)))
    }

    @Test
    fun `start and end of a logical day are 04_00 to 04_00`() {
        val day = LocalDate.of(2026, 10, 17)
        assertEquals(at(17, 4), clock.startOf(day))
        assertEquals(at(18, 4), clock.endOf(day))
    }

    @Test
    fun `hours are listed from the boundary`() =
        assertEquals(listOf(4, 5, 6), clock.hoursInOrder().take(3))
}
