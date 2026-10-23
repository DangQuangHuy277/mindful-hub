package io.github.dangquanghuy277.mindfulhub.core

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReactionTest {
    private fun s(ms: Long) = PvtEvent.Stimulus(ms)
    private fun t(ms: Long) = PvtEvent.Tap(ms)

    @Test
    fun `waits stay within PVT-B's 1 to 4 seconds`() {
        val run = PvtRun(Random(7))
        run.start()
        var now = 0L
        repeat(200) {
            val next = run.nextStimulusAt!!
            assertTrue(next - now in Pvtb.ISI_MIN_MS..Pvtb.ISI_MAX_MS)
            run.showStimulus(next)
            now = next + 250
            run.tap(now)
        }
    }

    @Test
    fun `a tap reports the reaction time, a guess reports too soon`() {
        val run = PvtRun(Random(1))
        run.start()
        assertIs<PvtFeedback.TooSoon>(run.tap(10))
        run.showStimulus(run.nextStimulusAt!!)
        val shown = run.stimulusAt!!
        assertEquals(PvtFeedback.Reaction(240), run.tap(shown + 240))
        run.showStimulus(run.nextStimulusAt!!)
        assertIs<PvtFeedback.TooSoon>(run.tap(run.stimulusAt!! + 60))
    }

    @Test
    fun `no answer for 30 seconds is a miss`() {
        val run = PvtRun(Random(1))
        run.start()
        run.showStimulus(2_000)
        assertNull(run.tick(31_999))
        assertIs<PvtFeedback.Missed>(run.tick(32_000))
        assertNull(run.stimulusAt)
    }

    @Test
    fun `no new star after three minutes, but one on screen still counts`() {
        val run = PvtRun(Random(1))
        run.start()
        run.showStimulus(179_500)
        assertTrue(!run.isOver(180_100))
        run.tap(179_800)
        assertTrue(run.isOver(180_100))
        run.showStimulus(180_500)
        assertNull(run.stimulusAt)
    }

    @Test
    fun `scoring follows Basner 2011`() {
        // RTs 200, 300, 400 (a lapse), a 50 ms guess, a tap with nothing showing, and a miss.
        val events = listOf(
            s(1_000), t(1_200),
            s(3_000), t(3_300),
            s(5_000), t(5_400),
            s(7_000), t(7_050),
            t(8_000),
            s(9_000), PvtEvent.Timeout(39_000),
        )
        val score = PvtScoring.score(events)!!
        assertEquals(4, score.stimuli)
        assertEquals(2, score.falseStarts)
        assertEquals(2, score.lapses) // 400 ms and the 30 s miss
        assertEquals(350.0, score.medianRtMs)
        assertEquals((5.0 + 1000.0 / 300 + 2.5 + 1000.0 / 30_000) / 4, score.meanSpeed, 1e-9)
        assertEquals(1.0 - 4.0 / 6.0, score.performance, 1e-9)
        assertEquals(200.0, score.fastest10RtMs)
    }

    @Test
    fun `raw events survive a round trip`() {
        val events = listOf(s(1_000), t(1_234), t(2_000), s(4_000), PvtEvent.Timeout(34_000))
        assertEquals(events, PvtScoring.decode(PvtScoring.encode(events)))
    }

    @Test
    fun `nothing to score without a stimulus`() {
        assertNull(PvtScoring.score(listOf(t(500))))
    }

    @Test
    fun `the line compares with your own usual only after three mornings`() {
        assertEquals(VsUsual.FIRST_MORNINGS, ReactionUsual.compare(300.0, listOf(250.0, 260.0)))
        val usual = listOf(250.0, 260.0, 255.0)
        assertEquals(VsUsual.ABOUT_USUAL, ReactionUsual.compare(270.0, usual))
        assertEquals(VsUsual.SLOWER, ReactionUsual.compare(290.0, usual))
        assertEquals(VsUsual.FASTER, ReactionUsual.compare(220.0, usual))
    }
}
