package io.github.dangquanghuy277.mindfulhub.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MorningTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val s = Settings() // 02:00 → 09:45, work 10:00 on Mon–Fri

    /** "23 09:30" = Oct 23 2026 (a Friday), 09:30 local. */
    private fun at(t: String): Instant {
        val (day, time) = t.split(" ")
        return LocalDateTime.of(LocalDate.of(2026, 10, day.toInt()), LocalTime.parse(time)).atZone(zone).toInstant()
    }

    @Test
    fun `a work-day morning runs from an hour before waking until work starts`() {
        val w = MorningWindow.of(LocalDate.of(2026, 10, 23), s, clock)!!
        assertEquals(at("23 08:45"), w.start)
        assertEquals(at("23 10:00"), w.endInclusive)
    }

    @Test
    fun `a free-day morning runs until 14 00`() {
        val w = MorningWindow.of(LocalDate.of(2026, 10, 24), s, clock)!!
        assertEquals(at("24 08:45"), w.start)
        assertEquals(at("24 14:00"), w.endInclusive)
    }

    @Test
    fun `a wake time after work starts leaves no window`() {
        val late = s.copy(wakeTime = LocalTime.of(11, 30))
        assertNull(MorningWindow.of(LocalDate.of(2026, 10, 23), late, clock))
    }

    @Test
    fun `the nudge goes out once, inside the window, while the check-in is open`() {
        assertTrue(MorningNudge.shouldPost(at("23 09:50"), s, clock, alreadyPosted = false, checkInDone = false))
        assertFalse(MorningNudge.shouldPost(at("23 09:50"), s, clock, alreadyPosted = true, checkInDone = false))
        assertFalse(MorningNudge.shouldPost(at("23 09:50"), s, clock, alreadyPosted = false, checkInDone = true))
    }

    @Test
    fun `no nudge for a night check, or in work hours`() {
        assertFalse(MorningNudge.shouldPost(at("23 05:10"), s, clock, alreadyPosted = false, checkInDone = false))
        assertFalse(MorningNudge.shouldPost(at("23 10:00"), s, clock, alreadyPosted = false, checkInDone = false))
        assertFalse(MorningNudge.shouldPost(at("23 15:00"), s, clock, alreadyPosted = false, checkInDone = false))
    }

    @Test
    fun `no nudge when its switch or the check-in is off`() {
        val noNudge = s.copy(enabled = s.enabled - Feature.MORNING_NUDGE)
        val noCheckIn = s.copy(enabled = s.enabled - Feature.MORNING_CHECKIN)
        assertFalse(MorningNudge.shouldPost(at("23 09:50"), noNudge, clock, alreadyPosted = false, checkInDone = false))
        assertFalse(MorningNudge.shouldPost(at("23 09:50"), noCheckIn, clock, alreadyPosted = false, checkInDone = false))
    }

    @Test
    fun `the nudge clears itself when work starts`() {
        assertEquals(at("23 10:00"), MorningNudge.expiresAt(at("23 09:50"), s, clock))
    }
}
