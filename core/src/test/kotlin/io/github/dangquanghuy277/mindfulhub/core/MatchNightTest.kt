package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchNightTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val sat = LocalDate.of(2026, 10, 24)
    private val match = MatchNight(sat, LocalTime.of(2, 0), 120)
    private val yt = GatePolicy.YOUTUBE
    private fun at(day: Int, h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, day, h, m).atZone(zone).toInstant()
    private val night = GateRules(night = NightPhase.AFTER_BEDTIME, windDownLock = true)

    @Test
    fun `a 02 00 kick-off on night Sat is Sunday 02 00, with 15 min either side`() {
        assertEquals(at(25, 1, 45) to at(25, 4, 15), MatchNights.window(match, clock))
    }

    @Test
    fun `match nights round-trip through the setting text`() {
        val list = listOf(match, MatchNight(sat.plusDays(3), LocalTime.of(23, 30), 150))
        assertEquals(list, MatchNights.decode(MatchNights.encode(list)))
        assertEquals(emptyList(), MatchNights.decode("garbage"))
    }

    @Test
    fun `adding a match waits a day, removing one doesn't`() {
        val one = MatchNights.encode(listOf(match))
        assertTrue(Precommit.loosens(SettingsCodec.MATCH_NIGHTS, null, one))
        assertFalse(Precommit.loosens(SettingsCodec.MATCH_NIGHTS, one, ""))
        assertEquals(sat.plusDays(1), Precommit.effectiveFrom(SettingsCodec.MATCH_NIGHTS, null, one, sat))
    }

    @Test
    fun `a match only counts with the switch on`() {
        val s = Settings(matchNights = listOf(match))
        assertEquals(match, s.matchOn(sat))
        assertNull(s.matchOn(sat.plusDays(1)))
        assertNull(s.copy(enabled = s.enabled - Feature.MATCH_NIGHT).matchOn(sat))
    }

    @Test
    fun `during the match youtube gets a pause with a box until it ends`() {
        val now = at(25, 2, 30)
        val d = assertIs<GateDecision.Prompt>(GatePolicy.decide(yt, Screen.APP, now, emptyMap(), 0, rules = night.copy(matchUntil = at(25, 4, 15))))
        assertEquals(GatePolicy.MATCH_BOX, d.timeBoxes.first().label)
        assertEquals(Duration.ofMinutes(105), d.timeBoxes.first().length)
    }

    @Test
    fun `feeds, X and games stay locked during the match`() {
        val r = night.copy(matchUntil = at(25, 4, 15))
        listOf("com.facebook.katana", "com.twitter.android", "com.hhgame.mlbbvn").forEach { pkg ->
            assertIs<GateDecision.Locked>(GatePolicy.decide(pkg, Screen.APP, at(25, 2, 30), emptyMap(), 0, rules = r))
        }
    }

    @Test
    fun `without a match youtube stays locked`() {
        assertIs<GateDecision.Locked>(GatePolicy.decide(yt, Screen.APP, at(25, 2, 30), emptyMap(), 0, rules = night))
    }
}
