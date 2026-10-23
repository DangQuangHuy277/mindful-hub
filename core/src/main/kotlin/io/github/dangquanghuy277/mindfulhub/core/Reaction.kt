package io.github.dangquanghuy277.mindfulhub.core

import kotlin.random.Random

/**
 * F75 (trial): the 3-minute psychomotor vigilance test, PVT-B (Basner, Mollicone & Dinges 2011,
 * docs/energy/RESEARCH.md §10). The game look can change; these numbers can't, or mornings stop
 * being comparable with each other and with the research. A new rule needs a new [VERSION].
 */
object Pvtb {
    const val VERSION = 1
    const val DURATION_MS = 180_000L

    /** From a response to the next stimulus, including [FEEDBACK_MS] showing the reaction time. */
    const val ISI_MIN_MS = 1_000L
    const val ISI_MAX_MS = 4_000L
    const val FEEDBACK_MS = 1_000L

    /** Faster than this is a guess, counted as a false start. */
    const val FALSE_START_MS = 100L

    /** PVT-B's lapse threshold (355 ms, not the 10-min PVT's 500 ms). */
    const val LAPSE_MS = 355L

    /** No response for this long counts as a lapse with this reaction time. */
    const val TIMEOUT_MS = 30_000L
}

/** One raw event, in milliseconds since the run started. Stored as is, so runs can be re-scored. */
sealed interface PvtEvent {
    val atMs: Long

    data class Stimulus(override val atMs: Long) : PvtEvent
    data class Tap(override val atMs: Long) : PvtEvent
    data class Timeout(override val atMs: Long) : PvtEvent
}

/** What the screen shows after a tap or a timeout. */
sealed interface PvtFeedback {
    data class Reaction(val ms: Long) : PvtFeedback
    data object TooSoon : PvtFeedback
    data object Missed : PvtFeedback
}

/**
 * One run, as a state machine the screen drives. Times are milliseconds since [start] on one
 * monotonic clock: the frame time a stimulus was drawn, the touch's own event time for a tap.
 */
class PvtRun(private val random: Random = Random.Default) {
    private val _events = mutableListOf<PvtEvent>()
    val events: List<PvtEvent> get() = _events

    /** When the next stimulus is due, or null while one is showing (or the run is over). */
    var nextStimulusAt: Long? = null
        private set
    var stimulusAt: Long? = null
        private set

    fun start() {
        _events.clear()
        stimulusAt = null
        nextStimulusAt = nextIsi()
    }

    private fun nextIsi() = random.nextLong(Pvtb.ISI_MIN_MS, Pvtb.ISI_MAX_MS + 1)

    /** No new stimulus once the 3 minutes are up; one already showing still gets its answer. */
    fun isOver(nowMs: Long): Boolean = stimulusAt == null && nowMs >= Pvtb.DURATION_MS

    fun showStimulus(atMs: Long) {
        if (stimulusAt != null || atMs >= Pvtb.DURATION_MS) return
        _events += PvtEvent.Stimulus(atMs)
        stimulusAt = atMs
        nextStimulusAt = null
    }

    fun tap(atMs: Long): PvtFeedback {
        _events += PvtEvent.Tap(atMs)
        val shown = stimulusAt
        if (shown == null) {
            // A tap with nothing on screen: a false start. The wait restarts (our choice; the paper
            // doesn't say), so guessing never brings the next star sooner.
            nextStimulusAt = atMs + nextIsi()
            return PvtFeedback.TooSoon
        }
        stimulusAt = null
        nextStimulusAt = atMs + nextIsi()
        val rt = atMs - shown
        return if (rt < Pvtb.FALSE_START_MS) PvtFeedback.TooSoon else PvtFeedback.Reaction(rt)
    }

    /** Call on every frame; ends a stimulus nobody answered. */
    fun tick(nowMs: Long): PvtFeedback? {
        val shown = stimulusAt ?: return null
        if (nowMs - shown < Pvtb.TIMEOUT_MS) return null
        _events += PvtEvent.Timeout(nowMs)
        stimulusAt = null
        nextStimulusAt = nowMs + nextIsi()
        return PvtFeedback.Missed
    }
}

/**
 * Basner 2011's outcome measures. [meanSpeed] (mean of 1/RT, per second) was the most sensitive
 * to sleep loss; [performance] is 1 − (lapses + false starts) / (valid stimuli + false starts).
 */
data class PvtScore(
    val stimuli: Int,
    val meanSpeed: Double,
    val medianRtMs: Double,
    val lapses: Int,
    val falseStarts: Int,
    val fastest10RtMs: Double,
    val slowest10Speed: Double,
    val performance: Double,
)

object PvtScoring {
    fun score(events: List<PvtEvent>): PvtScore? {
        val rts = mutableListOf<Long>()
        var falseStarts = 0
        var shown: Long? = null
        for (e in events) {
            when (e) {
                is PvtEvent.Stimulus -> shown = e.atMs
                is PvtEvent.Tap -> {
                    val s = shown
                    if (s == null) {
                        falseStarts++
                    } else {
                        val rt = e.atMs - s
                        if (rt < Pvtb.FALSE_START_MS) falseStarts++ else rts += rt
                        shown = null
                    }
                }
                is PvtEvent.Timeout -> if (shown != null) {
                    rts += Pvtb.TIMEOUT_MS
                    shown = null
                }
            }
        }
        if (rts.isEmpty()) return null
        val sorted = rts.sorted()
        val tenth = maxOf(1, sorted.size / 10)
        val speeds = sorted.map { 1000.0 / it }
        val lapses = rts.count { it >= Pvtb.LAPSE_MS }
        return PvtScore(
            stimuli = rts.size,
            meanSpeed = speeds.average(),
            medianRtMs = median(sorted.map { it.toDouble() }),
            lapses = lapses,
            falseStarts = falseStarts,
            fastest10RtMs = sorted.take(tenth).average(),
            slowest10Speed = speeds.takeLast(tenth).average(),
            performance = 1.0 - (lapses + falseStarts).toDouble() / (rts.size + falseStarts),
        )
    }

    /** "s1234,t1500,x31234": stimulus, tap, timeout. Compact, and readable in sqlite3. */
    fun encode(events: List<PvtEvent>): String = events.joinToString(",") {
        when (it) {
            is PvtEvent.Stimulus -> "s${it.atMs}"
            is PvtEvent.Tap -> "t${it.atMs}"
            is PvtEvent.Timeout -> "x${it.atMs}"
        }
    }

    fun decode(text: String): List<PvtEvent> = text.split(",").filter { it.length > 1 }.mapNotNull {
        val ms = it.substring(1).toLongOrNull() ?: return@mapNotNull null
        when (it[0]) {
            's' -> PvtEvent.Stimulus(ms)
            't' -> PvtEvent.Tap(ms)
            'x' -> PvtEvent.Timeout(ms)
            else -> null
        }
    }

    internal fun median(xs: List<Double>): Double {
        val s = xs.sorted()
        return if (s.size % 2 == 1) s[s.size / 2] else (s[s.size / 2 - 1] + s[s.size / 2]) / 2
    }
}

/** The one line after a run: against your own usual, never a score to beat (P48, P39). */
enum class VsUsual { FIRST_MORNINGS, FASTER, ABOUT_USUAL, SLOWER }

object ReactionUsual {
    /** Mornings needed before a comparison means anything. */
    const val MIN_RUNS = 3

    /** Your usual = the median of up to the last 14 mornings' median reaction times. */
    const val WINDOW = 14

    /**
     * ±8% around your usual reads as "about your usual". Not from a study: a band wide enough that
     * ordinary day-to-day noise doesn't flip the line every morning. Kept here so it can be tuned.
     */
    const val BAND = 0.08

    fun compare(todayMedianMs: Double, earlierMedians: List<Double>): VsUsual {
        if (earlierMedians.size < MIN_RUNS) return VsUsual.FIRST_MORNINGS
        val usual = PvtScoring.median(earlierMedians.takeLast(WINDOW))
        val diff = (todayMedianMs - usual) / usual
        return when {
            diff > BAND -> VsUsual.SLOWER
            diff < -BAND -> VsUsual.FASTER
            else -> VsUsual.ABOUT_USUAL
        }
    }
}
