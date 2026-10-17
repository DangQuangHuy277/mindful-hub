package io.github.dangquanghuy277.mindfulhub.core

import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.BACKGROUND
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.FOREGROUND
import io.github.dangquanghuy277.mindfulhub.core.UsageEventType.SCREEN_OFF
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionizerTest {
    private val t0 = Instant.parse("2026-10-17T17:00:00Z")
    private fun t(min: Long, sec: Long = 0) = t0.plusSeconds(min * 60 + sec)
    private fun ev(min: Long, pkg: String, type: UsageEventType, sec: Long = 0) = UsageEvent(t(min, sec), pkg, type)

    @Test
    fun `foreground then background makes one session`() {
        val s = Sessionizer.sessions(listOf(ev(0, "fb", FOREGROUND), ev(10, "fb", BACKGROUND)), now = t(60))
        assertEquals(listOf(AppSession("fb", t(0), t(10))), s)
    }

    @Test
    fun `next app's foreground closes the previous app`() {
        val s = Sessionizer.sessions(listOf(ev(0, "fb", FOREGROUND), ev(5, "x", FOREGROUND), ev(7, "x", BACKGROUND)), now = t(60))
        assertEquals(listOf(AppSession("fb", t(0), t(5)), AppSession("x", t(5), t(7))), s)
    }

    @Test
    fun `screen off closes the session`() {
        val s = Sessionizer.sessions(listOf(ev(0, "fb", FOREGROUND), ev(3, "android", SCREEN_OFF)), now = t(60))
        assertEquals(listOf(AppSession("fb", t(0), t(3))), s)
    }

    @Test
    fun `an open session is capped at now and at maxOpen`() {
        val events = listOf(ev(0, "fb", FOREGROUND))
        assertEquals(t(20), Sessionizer.sessions(events, now = t(20)).single().end)
        assertEquals(t(30), Sessionizer.sessions(events, now = t(600), maxOpen = Duration.ofMinutes(30)).single().end)
    }

    @Test
    fun `activity switches inside one app merge into one session`() {
        val events = listOf(
            ev(0, "fb", FOREGROUND), ev(4, "fb", BACKGROUND),
            ev(4, "fb", FOREGROUND, sec = 1), ev(9, "fb", BACKGROUND),
        )
        assertEquals(listOf(AppSession("fb", t(0), t(9))), Sessionizer.sessions(events, now = t(60)))
    }

    @Test
    fun `a stray background from another app is ignored`() {
        val s = Sessionizer.sessions(listOf(ev(0, "fb", FOREGROUND), ev(1, "x", BACKGROUND), ev(6, "fb", BACKGROUND)), now = t(60))
        assertEquals(listOf(AppSession("fb", t(0), t(6))), s)
    }
}
