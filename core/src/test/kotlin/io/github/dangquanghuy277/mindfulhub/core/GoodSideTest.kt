package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** D-021 features: switches, precommitment, night, budget, instead, review metrics. */
class GoodSideTest {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = LogicalDayClock(zone)
    private val fb = "com.facebook.katana"
    private val yt = "com.google.android.youtube"
    private val orca = "com.facebook.orca"
    private val ml = "com.hhgame.mlbbvn"
    private val mon = LocalDate.of(2026, 10, 19)

    private fun at(date: LocalDate, h: Int, m: Int = 0): Instant = date.atTime(h, m).atZone(zone).toInstant()

    // --- switches and precommitment ---

    @Test
    fun `every feature is on until switched off`() {
        val s = SettingsCodec.decode(emptyMap())
        assertEquals(Feature.entries.toSet(), s.enabled)
        val off = SettingsCodec.decode(mapOf(Feature.SLEEP_QUEST.key to "false"))
        assertFalse(off.on(Feature.SLEEP_QUEST))
        assertTrue(off.on(Feature.WIND_DOWN))
    }

    @Test
    fun `switching a restriction off waits until tomorrow, a trial switches now`() {
        assertEquals(mon.plusDays(1), Precommit.effectiveFrom(Feature.FEED_BUDGET.key, "true", "false", mon))
        assertEquals(mon, Precommit.effectiveFrom(Feature.FEED_BUDGET.key, "false", "true", mon))
        assertEquals(mon, Precommit.effectiveFrom(Feature.SLEEP_QUEST.key, "true", "false", mon))
    }

    @Test
    fun `a bigger budget or a later bedtime is a loosening`() {
        assertTrue(Precommit.loosens(SettingsCodec.FEED_BUDGET, "60", "90"))
        assertFalse(Precommit.loosens(SettingsCodec.FEED_BUDGET, "60", "45"))
        assertTrue(Precommit.loosens(SettingsCodec.BEDTIME, "23:30", "01:00"))
        assertFalse(Precommit.loosens(SettingsCodec.BEDTIME, "02:00", "01:45"))
        assertTrue(Precommit.loosens(SettingsCodec.WIND_DOWN, "45", "30"))
        assertTrue(Precommit.loosens(SettingsCodec.BED_SCROLL, "false", "true"))
    }

    @Test
    fun `a waiting change counts from its day, and the latest row wins`() {
        val rows = listOf(
            SettingRow(SettingsCodec.FEED_BUDGET, "60", mon.minusDays(5)),
            SettingRow(SettingsCodec.FEED_BUDGET, "90", mon.plusDays(1)), // loosened today: waits
            SettingRow(Feature.WIND_DOWN.key, "false", mon.plusDays(1)),
        )
        assertEquals(60, SettingsCodec.decode(SettingsHistory.valuesOn(rows, mon)).feedBudgetMinutes)
        assertTrue(SettingsCodec.decode(SettingsHistory.valuesOn(rows, mon)).on(Feature.WIND_DOWN))
        val tomorrow = SettingsCodec.decode(SettingsHistory.valuesOn(rows, mon.plusDays(1)))
        assertEquals(90, tomorrow.feedBudgetMinutes)
        assertFalse(tomorrow.on(Feature.WIND_DOWN))
    }

    // --- night (F03, F31, F53) and the sleep quest (F13) ---

    private val noQuest = Settings(enabled = Feature.entries.toSet() - Feature.SLEEP_QUEST)

    @Test
    fun `wind-down starts 45 min before bedtime and lasts until the wake time`() {
        assertEquals(NightPhase.DAY, NightClock.state(at(mon, 23, 0), clock, noQuest).phase)
        assertEquals(NightPhase.WIND_DOWN, NightClock.state(at(mon.plusDays(1), 1, 20), clock, noQuest).phase)
        val late = NightClock.state(at(mon.plusDays(1), 2, 30), clock, noQuest)
        assertEquals(NightPhase.AFTER_BEDTIME, late.phase)
        assertEquals(mon, late.night)
        // 06:00 is already the next logical day, but the night of Monday still holds until 09:45.
        val dawn = NightClock.state(at(mon.plusDays(1), 6, 0), clock, noQuest)
        assertEquals(NightPhase.AFTER_BEDTIME, dawn.phase)
        assertEquals(mon, dawn.night)
        assertEquals(NightPhase.DAY, NightClock.state(at(mon.plusDays(1), 9, 50), clock, noQuest).phase)
    }

    @Test
    fun `an evening bedtime works the same way`() {
        val s = noQuest.copy(bedtime = LocalTime.of(23, 30), wakeTime = LocalTime.of(7, 30))
        assertEquals(NightPhase.WIND_DOWN, NightClock.state(at(mon, 22, 50), clock, s).phase)
        assertEquals(NightPhase.AFTER_BEDTIME, NightClock.state(at(mon.plusDays(1), 3, 0), clock, s).phase)
        assertEquals(NightPhase.DAY, NightClock.state(at(mon.plusDays(1), 7, 31), clock, s).phase)
    }

    @Test
    fun `the quest moves 15 minutes every 3 days and holds without failing`() {
        assertEquals(1, SleepQuest.chapter(mon, 0, mon))
        assertEquals(1, SleepQuest.chapter(mon, 0, mon.plusDays(2)))
        assertEquals(2, SleepQuest.chapter(mon, 0, mon.plusDays(3)))
        assertEquals(1, SleepQuest.chapter(mon, 3, mon.plusDays(3)))
        assertEquals(7, SleepQuest.chapter(mon, 0, mon.plusDays(60)))
        assertEquals(SleepTimes(LocalTime.of(1, 30), LocalTime.of(9, 15)), SleepQuest.times(3))
        assertEquals(SleepTimes(LocalTime.of(0, 30), LocalTime.of(8, 15)), SleepQuest.times(7))
        val quest = Settings(sleepQuestStart = mon)
        assertEquals(SleepQuest.times(2), quest.sleepTimes(mon.plusDays(4)))
    }

    // --- the gate with the new rules ---

    @Test
    fun `wind-down locks feeds, video and games but not messages`() {
        val r = GateRules(night = NightPhase.WIND_DOWN, windDownLock = true, bedScrollLeft = true)
        val d = assertIs<GateDecision.Locked>(GatePolicy.decide(fb, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r))
        assertEquals(LockReason.WIND_DOWN, d.reason)
        assertTrue(d.bedScroll)
        assertIs<GateDecision.Locked>(GatePolicy.decide(yt, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r))
        assertIs<GateDecision.Locked>(GatePolicy.decide(ml, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r))
        assertIs<GateDecision.Prompt>(GatePolicy.decide(orca, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r))
    }

    @Test
    fun `a time box started before wind-down runs to its end`() {
        val r = GateRules(night = NightPhase.WIND_DOWN, windDownLock = true)
        val s = mapOf(fb to ActiveSession(fb, Purpose.GROUP_TIME, at(mon, 23), at(mon, 23, 10)))
        assertEquals(GateDecision.Allow, GatePolicy.decide(fb, Screen.APP, at(mon, 23, 5), s, 1, rules = r))
    }

    @Test
    fun `without the lock, the postcard comes before the pause after bedtime`() {
        val r = GateRules(night = NightPhase.AFTER_BEDTIME, postcardDue = true)
        assertTrue(assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r)).postcardDue)
        assertFalse(assertIs<GateDecision.Prompt>(GatePolicy.decide(orca, Screen.APP, at(mon, 23), emptyMap(), 0, rules = r)).postcardDue)
        val day = GateRules(night = NightPhase.DAY, postcardDue = true)
        assertFalse(assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, at(mon, 15), emptyMap(), 0, rules = day)).postcardDue)
    }

    @Test
    fun `a used-up feeds budget locks strict apps only, and boxes never exceed what's left`() {
        val used = GateRules(feedMinutesLeft = 0.4)
        assertEquals(LockReason.BUDGET, assertIs<GateDecision.Locked>(GatePolicy.decide(fb, Screen.APP, at(mon, 15), emptyMap(), 0, rules = used)).reason)
        assertIs<GateDecision.Prompt>(GatePolicy.decide(yt, Screen.APP, at(mon, 15), emptyMap(), 0, rules = used))
        val some = GateRules(feedMinutesLeft = 12.5)
        val p = assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, at(mon, 15), emptyMap(), 0, rules = some))
        assertEquals(listOf(5L, 10L, 12L), p.timeBoxes.map { it.length.toMinutes() })
        assertEquals(listOf(3L), GatePolicy.capTo(GatePolicy.timeBoxesFor(AppProfile.STRICT), 3.2).map { it.length.toMinutes() })
        assertEquals(GatePolicy.timeBoxesFor(AppProfile.STRICT), GatePolicy.capTo(GatePolicy.timeBoxesFor(AppProfile.STRICT), 40.0))
    }

    @Test
    fun `default rules leave the gate core unchanged`() {
        val p = assertIs<GateDecision.Prompt>(GatePolicy.decide(fb, Screen.APP, at(mon, 15), emptyMap(), 0, rules = GateRules()))
        assertEquals(GatePolicy.timeBoxesFor(AppProfile.STRICT), p.timeBoxes)
        assertFalse(p.postcardDue)
    }

    @Test
    fun `feeds budget counts only strict apps inside the day`() {
        val sessions = listOf(
            AppSession(fb, at(mon, 3, 50), at(mon, 4, 10)), // straddles the 04:00 boundary
            AppSession(yt, at(mon, 12), at(mon, 13)),
            AppSession("com.instagram.android", at(mon, 20), at(mon, 20, 15)),
        )
        assertEquals(25.0, FeedBudget.usedMinutes(sessions, setOf(fb, "com.instagram.android"), clock.startOf(mon), at(mon, 23)), 0.01)
        assertEquals(0.0, FeedBudget.left(60, 75.0))
    }

    // --- instead menu (F26 + F59) ---

    @Test
    fun `instead items fit the moment, and bedtime never offers the lit screen`() {
        assertTrue(InsteadMenu.forMoment(Moment.QUICK).all { it.minutes <= 2 })
        assertTrue(InsteadMenu.forMoment(Moment.LONG).all { it.minutes >= 15 })
        val bed = InsteadMenu.forMoment(Moment.LONG, NightPhase.AFTER_BEDTIME)
        assertTrue(bed.none { it.action is InsteadAction.OpenApp })
        assertTrue(InsteadMenu.all.none { it.text.lowercase().startsWith("don't") })
    }

    // --- review metrics (F11, F12, F32) ---

    @Test
    fun `unintended time is gated use outside purposeful boxes`() {
        val usage = listOf(
            AppSession(fb, at(mon, 10), at(mon, 10, 20)), // box of 10 min, stayed 20: 10 unintended
            AppSession(fb, at(mon, 12), at(mon, 12, 5)), // "just bored": 5 unintended
            AppSession(yt, at(mon, 14), at(mon, 14, 30)), // rated 👎: 30 unintended
            AppSession("com.android.chrome", at(mon, 15), at(mon, 16)), // not gated
        )
        val boxes = listOf(
            GateBox(fb, Purpose.GROUP_TIME, at(mon, 10), at(mon, 10, 10)),
            GateBox(fb, Purpose.JUST_BORED, at(mon, 12), at(mon, 12, 10)),
            GateBox(yt, Purpose.WATCH_SPECIFIC, at(mon, 14), at(mon, 14, 30), rating = -1),
        )
        assertEquals(45.0, UnintendedTime.minutes(usage, setOf(fb, yt), boxes, clock.startOf(mon), clock.endOf(mon)), 0.01)
    }

    @Test
    fun `leaving a minute or more early counts as a win`() {
        assertTrue(GateBox(fb, Purpose.GROUP_TIME, at(mon, 10), at(mon, 10, 10), endedBy = "USER", end = at(mon, 10, 4)).leftEarly)
        assertFalse(GateBox(fb, Purpose.GROUP_TIME, at(mon, 10), at(mon, 10, 10), endedBy = "TIMEBOX", end = at(mon, 10, 10)).leftEarly)
        assertFalse(GateBox(fb, Purpose.GROUP_TIME, at(mon, 10), at(mon, 10, 10), endedBy = "USER", end = at(mon, 10, 9).plusSeconds(30)).leftEarly)
    }

    @Test
    fun `a slip is clearly above your own normal`() {
        val days = (1L..7L).associate { mon.minusDays(it) to 40.0 }
        assertTrue(Comeback.wasSlip(days + (mon to 80.0), mon))
        assertFalse(Comeback.wasSlip(days + (mon to 45.0), mon))
        assertFalse(Comeback.wasSlip(mapOf(mon.minusDays(1) to 5.0, mon to 90.0), mon)) // too little history
        assertFalse(Comeback.wasSlip((1L..7L).associate { mon.minusDays(it) to 5.0 } + (mon to 20.0), mon)) // under the minimum
        assertTrue(Comeback.isFreshStart(mon))
    }

    @Test
    fun `who-5 scores 0 to 100 and needs every answer`() {
        assertEquals(100, Who5.score(List(5) { 5 }))
        assertEquals(48, Who5.score(listOf(3, 2, 2, 3, 2)))
        assertNull(Who5.score(listOf(3, 2)))
    }

    @Test
    fun `plan suggestions avoid the bed slot`() {
        val hours = clock.hoursInOrder()
        val avg = hours.map { h -> if (h == 1 || h == 21 || h == 13 || h == 20) 30.0 else 1.0 }
        assertEquals(listOf(13, 20, 21), WeekPlan.suggestedHours(avg, hours, LocalTime.of(2, 0)))
        val item = PlanItem(weekStart = mon, day = java.time.DayOfWeek.WEDNESDAY, time = LocalTime.of(0, 30), category = Category.READ, minutes = 20, title = "Audiobook")
        assertEquals(at(mon.plusDays(3), 0, 30), WeekPlan.instantOf(item, clock))
        assertEquals(Duration.ZERO, Duration.between(WeekPlan.instantOf(item, clock), at(mon.plusDays(3), 0, 30)))
    }
}
