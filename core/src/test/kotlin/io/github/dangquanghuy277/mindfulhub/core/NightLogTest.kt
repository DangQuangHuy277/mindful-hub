package io.github.dangquanghuy277.mindfulhub.core

import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.BACKGROUND
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.FOREGROUND
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.SCREEN_OFF
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.SCREEN_ON
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.SHUTDOWN
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.STARTUP
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.UNLOCK
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

class NightLogTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val night = LocalDate.of(2026, 10, 19)
    private val sleep = SleepTimes(LocalTime.of(2, 0), LocalTime.of(9, 45))

    /** "19 23:30" = Oct 19, 23:30 local. */
    private fun at(s: String): Instant {
        val (day, time) = s.split(" ")
        return LocalDateTime.of(LocalDate.of(2026, 10, day.toInt()), LocalTime.parse(time)).atZone(zone).toInstant()
    }

    /** A real use: screen on, unlock, [pkg] in front, screen off. */
    private fun use(from: String, to: String, pkg: String = "fb") = listOf(
        UsageEvent(at(from), "android", SCREEN_ON),
        UsageEvent(at(from), "android", UNLOCK),
        UsageEvent(at(from), pkg, FOREGROUND),
        UsageEvent(at(to), pkg, BACKGROUND),
        UsageEvent(at(to), "android", SCREEN_OFF),
    )

    private fun glance(from: String, to: String) = listOf(UsageEvent(at(from), "android", SCREEN_ON), UsageEvent(at(to), "android", SCREEN_OFF))

    private fun run(events: List<UsageEvent>, now: String = "21 08:00") = NightLog.night(events, night, sleep, clock, at(now))

    @Test
    fun `the night runs from the last use to the first one`() {
        val n = run(use("19 22:00", "20 02:41", "fb") + use("20 09:52", "20 10:10", "messenger"))
        assertEquals(NightStatus.FOUND, n.status)
        assertEquals(at("20 02:41"), n.downAt)
        assertEquals(at("20 09:52"), n.upAt)
        assertEquals("fb", n.lastPkg)
        assertEquals("messenger", n.firstPkg)
        assertTrue(n.interruptions.isEmpty())
        assertTrue(n.settled)
        assertTrue(n.screenOnSignal)
    }

    @Test
    fun `a short check in the middle stays one night, as an interruption`() {
        val n = run(use("19 23:00", "20 02:30") + use("20 05:00", "20 05:04", "clock") + use("20 09:30", "20 10:00"))
        assertEquals(at("20 02:30"), n.downAt)
        assertEquals(at("20 09:30"), n.upAt)
        assertEquals(listOf("clock"), n.interruptions.map { it.main })
    }

    @Test
    fun `a short use after the planned wake is getting up, even with a quiet morning after it`() {
        val n = run(use("19 23:00", "20 02:30") + use("20 09:50", "20 10:00") + use("20 14:00", "20 14:30"))
        assertEquals(at("20 09:50"), n.upAt)
        assertTrue(n.interruptions.isEmpty())
        assertTrue(n.otherStretches.isEmpty())
    }

    @Test
    fun `a use is named by the app it was spent in longest`() {
        val events = use("19 23:00", "20 02:30") + listOf(
            UsageEvent(at("20 09:50"), "android", SCREEN_ON), UsageEvent(at("20 09:50:05"), "android", UNLOCK),
            UsageEvent(at("20 09:50:05"), "update", FOREGROUND), UsageEvent(at("20 09:50:06"), "update", BACKGROUND),
            UsageEvent(at("20 09:50:06"), "clock", FOREGROUND), UsageEvent(at("20 09:51"), "clock", BACKGROUND),
            UsageEvent(at("20 09:51"), "android", SCREEN_OFF),
        )
        assertEquals("clock", run(events).firstPkg)
    }

    @Test
    fun `history switches from the fallback to SCREEN_ON where SCREEN_ON starts`() {
        val events = listOf(UsageEvent(at("19 23:00"), "fb", FOREGROUND), UsageEvent(at("20 02:30"), "android", SCREEN_OFF)) +
            use("20 09:30", "20 10:00")
        assertEquals(at("20 02:30"), run(events).downAt)
    }

    @Test
    fun `a glance without an unlock doesn't break the night`() {
        val n = run(use("19 23:00", "20 02:30") + glance("20 04:00", "20 04:00:10") + use("20 09:30", "20 10:00"))
        assertEquals(at("20 02:30"), n.downAt)
        assertEquals(1, n.glances)
        assertTrue(n.interruptions.isEmpty())
    }

    @Test
    fun `a football night keeps both stretches`() {
        // Asleep 23:30-02:00, a match 02:00-04:00, asleep again 04:00-09:30.
        val events = use("19 21:00", "19 23:30") + use("20 02:00", "20 04:00", "tv") + use("20 09:30", "20 10:00")
        val n = run(events)
        assertEquals(at("20 04:00"), n.downAt)
        assertEquals(at("20 09:30"), n.upAt)
        assertEquals("tv", n.lastPkg)
        assertEquals(listOf(Stretch(at("19 23:30"), at("20 02:00"))), n.otherStretches)
    }

    @Test
    fun `still down means no wake time yet, and not settled`() {
        val n = run(use("19 23:00", "20 02:30"), now = "20 08:00")
        assertEquals(at("20 02:30"), n.downAt)
        assertNull(n.upAt)
        assertFalse(n.settled)
    }

    @Test
    fun `no events in the window is no data, never a perfect night`() {
        val n = run(use("19 10:00", "19 10:30"))
        assertEquals(NightStatus.NO_DATA, n.status)
        assertNull(n.downAt)
    }

    @Test
    fun `no long stretch is no clear night`() {
        val events = (0 until 10).flatMap { h ->
            val t = LocalTime.of(22, 0).plusHours(h.toLong() * 2)
            val day = if (h < 1) "19" else "20"
            use("$day $t", "$day ${t.plusMinutes(30)}")
        }
        assertEquals(NightStatus.NO_CLEAR_NIGHT, run(events).status)
    }

    @Test
    fun `a shutdown is counted, since off looks like down`() {
        val events = use("19 23:00", "20 02:30") +
            listOf(UsageEvent(at("20 02:31"), "android", SHUTDOWN), UsageEvent(at("20 09:00"), "android", STARTUP)) +
            use("20 09:30", "20 10:00")
        assertEquals(1, run(events).shutdowns)
    }

    @Test
    fun `old history without SCREEN_ON starts uses at the app or unlock`() {
        val events = listOf(
            UsageEvent(at("19 23:00"), "fb", FOREGROUND), UsageEvent(at("20 02:30"), "android", SCREEN_OFF),
            UsageEvent(at("20 09:30"), "android", UNLOCK), UsageEvent(at("20 09:31"), "x", FOREGROUND),
            UsageEvent(at("20 10:00"), "android", SCREEN_OFF),
        )
        val n = run(events)
        assertEquals(at("20 02:30"), n.downAt)
        assertEquals(at("20 09:30"), n.upAt)
        assertEquals("x", n.firstPkg)
        assertFalse(n.screenOnSignal)
    }

    @Test
    fun `with SCREEN_ON, an app event while the screen is off opens nothing`() {
        val events = use("19 23:00", "20 02:30") + UsageEvent(at("20 03:00"), "launcher", FOREGROUND) + use("20 09:30", "20 10:00")
        assertEquals(at("20 02:30"), run(events).downAt)
    }

    @Test
    fun `the check-in asks about last night until 14 00`() {
        assertEquals(LocalDate.of(2026, 10, 19), MorningCheckIn.nightToAsk(at("20 09:00"), clock))
        assertNull(MorningCheckIn.nightToAsk(at("20 14:00"), clock))
        assertNull(MorningCheckIn.nightToAsk(at("20 03:00"), clock))
    }
}
