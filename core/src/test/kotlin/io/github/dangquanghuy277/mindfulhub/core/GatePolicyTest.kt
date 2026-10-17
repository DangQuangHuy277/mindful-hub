package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GatePolicyTest {
    private val fb = "com.facebook.katana"
    private val x = "com.twitter.android"
    private val now = Instant.parse("2026-10-18T14:00:00Z")

    @Test
    fun `untracked apps are always allowed`() =
        assertEquals(GateDecision.Allow, GatePolicy.decide("com.android.chrome", Screen.APP, now, emptyMap(), 0))

    @Test
    fun `a new facebook open is prompted with the strict delay`() {
        val d = assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, now, emptyMap(), 0))
        assertEquals(AppProfile.STRICT, d.profile)
        assertEquals(Duration.ofSeconds(10), d.delay)
        assertEquals(1, d.openNumber)
    }

    @Test
    fun `an open inside an active time box is allowed`() {
        val s = mapOf(fb to ActiveSession(fb, Purpose.GROUP_TIME, now.minusSeconds(60), now.plusSeconds(240)))
        assertEquals(GateDecision.Allow, GatePolicy.decide(fb, Screen.APP, now, s, 3))
    }

    @Test
    fun `an expired time box prompts again`() {
        val s = mapOf(fb to ActiveSession(fb, Purpose.GROUP_TIME, now.minusSeconds(900), now))
        assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, now, s, 3))
    }

    @Test
    fun `short video is blocked even inside a session`() {
        val s = mapOf(fb to ActiveSession(fb, Purpose.MESSAGE, now, now.plusSeconds(600)))
        assertEquals(GateDecision.Block(BlockReason.SHORT_VIDEO), GatePolicy.decide(fb, Screen.SHORT_VIDEO, now, s, 0))
    }

    @Test
    fun `delay grows after the free opens and is capped`() {
        assertEquals(Duration.ofSeconds(10), GatePolicy.delayFor(AppProfile.STRICT, 2))
        assertEquals(Duration.ofSeconds(15), GatePolicy.delayFor(AppProfile.STRICT, 3))
        assertEquals(Duration.ofSeconds(60), GatePolicy.delayFor(AppProfile.STRICT, 40))
    }

    @Test
    fun `messaging delay never escalates`() =
        assertEquals(Duration.ofSeconds(2), GatePolicy.delayFor(AppProfile.MESSAGING, 30))

    @Test
    fun `x gets the light profile with longer watch boxes`() {
        val d = assertIs<GateDecision.Prompt>(GatePolicy.decide(x, Screen.APP, now, emptyMap(), 0))
        assertEquals(AppProfile.LIGHT, d.profile)
        assertEquals(Duration.ofSeconds(5), d.delay)
        assertEquals(Duration.ofMinutes(60), d.timeBoxes.last().length)
    }

    @Test
    fun `games are boxed in matches`() {
        val boxes = GatePolicy.timeBoxesFor(AppProfile.GAME)
        assertEquals(listOf("1 match", "2 matches", "3 matches"), boxes.map { it.label })
    }
}
