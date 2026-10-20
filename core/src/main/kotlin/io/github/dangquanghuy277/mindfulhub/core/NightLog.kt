package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * F64 rules. Changing any number means a new [version]: each stored night says which rules made
 * it, so history never silently changes meaning (docs/sleep/RESEARCH.md, "What to log").
 */
data class NightRules(
    val version: Int = 1,
    /** Look for the night from this long before the planned bedtime ... */
    val searchBefore: Duration = Duration.ofHours(4),
    /** ... to this long after the planned wake time. */
    val searchAfter: Duration = Duration.ofHours(5),
    /** Screen on without an unlock and shorter than this is a glance (a notification, the clock), not a use. */
    val glanceMax: Duration = Duration.ofSeconds(60),
    /** The longest phone-down stretch has to be at least this long to count as the night. */
    val minNight: Duration = Duration.ofHours(3),
    /** A use this short inside the night is an interruption (woke, checked, put it down) ... */
    val maxInterruption: Duration = Duration.ofMinutes(20),
    /** ... when at least this much phone-down time follows it on the far side. */
    val minRest: Duration = Duration.ofMinutes(45),
    /** Other phone-down stretches this long, before the planned wake, are kept too: a split night (sleep, a late match, sleep again). */
    val minSegment: Duration = Duration.ofMinutes(90),
)

/**
 * One stretch of the screen being on. [unlocked] = the lock screen was passed. [main] is the app it
 * was spent in longest, so a one-second flash (Samsung's update service at unlock, the launcher on
 * the way out) doesn't name the use.
 */
data class ScreenUse(
    val start: Instant,
    val end: Instant,
    val unlocked: Boolean,
    val pkgs: List<String>,
    val main: String? = pkgs.firstOrNull(),
) {
    val duration: Duration get() = Duration.between(start, end)
}

data class Stretch(val start: Instant, val end: Instant) {
    val duration: Duration get() = Duration.between(start, end)
}

enum class NightStatus {
    FOUND,
    /** There is data, but no phone-down stretch of [NightRules.minNight] in the window. */
    NO_CLEAR_NIGHT,
    /** No usage events at all in the window: phone off, or nothing imported. Never a perfect night. */
    NO_DATA,
}

/**
 * Last night as the phone saw it. This is a phone-down window, not sleep: a phone can't tell lying
 * awake from sleeping (P42), so nothing here is called sleep.
 */
data class NightFacts(
    val night: LocalDate,
    val status: NightStatus,
    val plannedBed: Instant,
    val plannedWake: Instant,
    /** End of the last use before the night; null when the history doesn't reach back that far. */
    val downAt: Instant? = null,
    /** Start of the first use after it; null while the phone is still down. */
    val upAt: Instant? = null,
    val lastPkg: String? = null,
    val firstPkg: String? = null,
    /** Short uses inside the night: you woke the phone and put it down again. */
    val interruptions: List<ScreenUse> = emptyList(),
    val glances: Int = 0,
    /** Other phone-down stretches of at least [NightRules.minSegment] in the window (a split night). */
    val otherStretches: List<Stretch> = emptyList(),
    /** Shutdowns in the window: the phone being off looks the same as it being down. */
    val shutdowns: Int = 0,
    /** False for history from before SCREEN_ON was imported: uses then start at the first app or unlock, and glances are invisible. */
    val screenOnSignal: Boolean = false,
    /** True once later events can't change this night. */
    val settled: Boolean = false,
    val rulesVersion: Int,
)

object NightLog {
    /** Planned bedtime and the wake time after it, for night [night]. */
    fun planned(night: LocalDate, sleep: SleepTimes, clock: LogicalDayClock): Pair<Instant, Instant> {
        val bed = clock.instantOn(night, sleep.bedtime)
        return bed to NightClock.nextAfter(bed, sleep.wake, clock)
    }

    /** Events to load: half a day either side of the window, so stretches crossing its edges are measured whole. */
    fun fetchRange(night: LocalDate, sleep: SleepTimes, clock: LogicalDayClock, rules: NightRules = NightRules()): Pair<Instant, Instant> {
        val (bed, wake) = planned(night, sleep, clock)
        return bed.minus(rules.searchBefore).minus(HALF_DAY) to wake.plus(rules.searchAfter).plus(HALF_DAY)
    }

    /**
     * Screen-on stretches. From the first SCREEN_ON on, only those open a stretch; history before
     * it has none (they were imported from F64 on), so there an app coming to the front or an
     * unlock opens one. SCREEN_OFF or SHUTDOWN closes it.
     */
    fun screenUses(events: List<UsageEvent>, until: Instant): List<ScreenUse> {
        val sorted = events.sortedBy { it.at }
        val firstScreenOn = sorted.firstOrNull { it.type == UsageEventType.SCREEN_ON }?.at
        val out = mutableListOf<ScreenUse>()
        var start: Instant? = null
        var last: Instant? = null
        var unlocked = false
        val pkgs = mutableListOf<String>()
        val dwell = mutableMapOf<String, Duration>()
        var front: String? = null
        var frontAt: Instant? = null

        fun credit(at: Instant) {
            val p = front ?: return
            dwell.merge(p, Duration.between(frontAt, at).coerceAtLeast(Duration.ZERO), Duration::plus)
            front = null
        }

        fun open(at: Instant) {
            start = at
            last = at
            unlocked = false
            pkgs.clear()
            dwell.clear()
            front = null
        }

        fun close(at: Instant) {
            val s = start ?: return
            credit(at)
            val main = dwell.maxByOrNull { it.value }?.key ?: pkgs.firstOrNull()
            out += ScreenUse(s, maxOf(at, s), unlocked, pkgs.toList(), main)
            start = null
        }

        fun fallback(e: UsageEvent) = firstScreenOn == null || e.at.isBefore(firstScreenOn)

        for (e in sorted) {
            when (e.type) {
                // A second SCREEN_ON means the screen-off was lost: end the first at its last event.
                UsageEventType.SCREEN_ON -> {
                    if (start != null) close(last!!)
                    open(e.at)
                }
                UsageEventType.FOREGROUND -> {
                    if (start == null && fallback(e)) open(e.at)
                    if (start != null) {
                        if (pkgs.lastOrNull() != e.pkg) pkgs += e.pkg
                        credit(e.at)
                        front = e.pkg
                        frontAt = e.at
                        last = e.at
                    }
                }
                UsageEventType.UNLOCK -> {
                    if (start == null && fallback(e)) open(e.at)
                    if (start != null) {
                        unlocked = true
                        last = e.at
                    }
                }
                UsageEventType.BACKGROUND -> if (start != null) {
                    if (e.pkg == front) credit(e.at)
                    last = e.at
                }
                UsageEventType.SCREEN_OFF, UsageEventType.SHUTDOWN -> close(e.at)
                UsageEventType.STARTUP -> Unit
            }
        }
        if (start != null) close(until)
        return out
    }

    /** A phone-down gap between two uses; [before]/[after] index the uses, null at the edges of the loaded range. */
    private data class Gap(val start: Instant?, val end: Instant?, val before: Int?, val after: Int?)

    /**
     * The night is the longest phone-down gap overlapping the window. It grows across short uses
     * (interruptions) when a real rest follows on the far side, so waking at 05:00 to check the time
     * stays one night. Other long gaps are kept as [NightFacts.otherStretches].
     */
    fun night(
        events: List<UsageEvent>,
        night: LocalDate,
        sleep: SleepTimes,
        clock: LogicalDayClock,
        now: Instant,
        rules: NightRules = NightRules(),
    ): NightFacts {
        val (bed, wake) = planned(night, sleep, clock)
        val winStart = bed.minus(rules.searchBefore)
        val winEnd = wake.plus(rules.searchAfter)
        val (from, to) = fetchRange(night, sleep, clock, rules)
        val until = minOf(now, to)
        val loaded = events.filter { !it.at.isBefore(from) && it.at.isBefore(until) }.sortedBy { it.at }
        val inWindow = loaded.filter { !it.at.isBefore(winStart) && it.at.isBefore(winEnd) }
        val base = NightFacts(
            night, NightStatus.NO_DATA, bed, wake,
            shutdowns = inWindow.count { it.type == UsageEventType.SHUTDOWN },
            screenOnSignal = inWindow.any { it.type == UsageEventType.SCREEN_ON },
            settled = !now.isBefore(to),
            rulesVersion = rules.version,
        )
        if (inWindow.isEmpty()) return base

        val all = screenUses(loaded, until)
        val uses = all.filter { it.unlocked || it.duration >= rules.glanceMax }
        val glances = all.filterNot { it.unlocked || it.duration >= rules.glanceMax }
        val gaps = buildList {
            add(Gap(null, uses.firstOrNull()?.start, null, uses.indices.firstOrNull()))
            for (i in 0 until uses.size - 1) add(Gap(uses[i].end, uses[i + 1].start, i, i + 1))
            if (uses.isNotEmpty()) add(Gap(uses.last().end, null, uses.lastIndex, null))
        }
        fun startOf(g: Gap) = g.start ?: from
        fun endOf(g: Gap) = g.end ?: until
        fun length(g: Gap) = Duration.between(startOf(g), endOf(g))
        fun overlap(g: Gap, a: Instant, b: Instant) = Duration.between(maxOf(startOf(g), a), minOf(endOf(g), b)).coerceAtLeast(Duration.ZERO)
        fun overlap(g: Gap) = overlap(g, winStart, winEnd)

        // The core is the gap covering most of the planned bed-to-wake time, so a quiet morning can't
        // beat a night with one check in it; the whole window only breaks ties (e.g. a fully missed plan).
        val core = gaps.indices.filter { overlap(gaps[it]) > Duration.ZERO }
            .maxWithOrNull(compareBy<Int> { overlap(gaps[it], bed, wake) }.thenBy { overlap(gaps[it]) })
        if (core == null || length(gaps[core]) < rules.minNight) return base.copy(status = NightStatus.NO_CLEAR_NIGHT)

        var left = core
        var right = core
        while (right + 1 < gaps.size) {
            val use = uses[gaps[right].after!!]
            val next = gaps[right + 1]
            // Only before the planned wake: a short use after it is getting up, not a check in the night.
            if (use.duration <= rules.maxInterruption && use.start.isBefore(wake) && length(next) >= rules.minRest) right++ else break
        }
        while (left > 0) {
            val use = uses[gaps[left].before!!]
            val prev = gaps[left - 1]
            if (use.duration <= rules.maxInterruption && use.end.isAfter(winStart) && length(prev) >= rules.minRest) left-- else break
        }

        val downAt = gaps[left].start
        val upAt = gaps[right].end
        val nightStart = downAt ?: from
        val nightEnd = upAt ?: until
        val settled = upAt != null && !now.isBefore(winEnd) && !now.isBefore(upAt.plus(rules.maxInterruption).plus(rules.minRest))
        return base.copy(
            status = NightStatus.FOUND,
            downAt = downAt,
            upAt = upAt,
            lastPkg = gaps[left].before?.let { uses[it].main },
            firstPkg = gaps[right].after?.let { uses[it].main },
            interruptions = (left until right).map { uses[gaps[it].after!!] },
            glances = glances.count { !it.start.isBefore(nightStart) && it.start.isBefore(nightEnd) },
            otherStretches = gaps.indices
                // A gap with no known start (the history begins inside it) isn't a stretch anyone can read.
                .filter { it !in left..right && gaps[it].start != null && overlap(gaps[it], winStart, wake) > Duration.ZERO && length(gaps[it]) >= rules.minSegment }
                .map { Stretch(startOf(gaps[it]), endOf(gaps[it])) },
            settled = settled || base.settled,
        )
    }

    private val HALF_DAY: Duration = Duration.ofHours(12)
}
