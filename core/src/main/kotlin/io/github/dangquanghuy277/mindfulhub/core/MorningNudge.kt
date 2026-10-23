package io.github.dangquanghuy277.mindfulhub.core

import java.time.Instant

/**
 * D-030: one quiet notification after the first unlock of the morning, opening the F65 check-in.
 * It never goes out during work hours (D-029), never twice a day, and not once the check-in is
 * answered or skipped. It clears itself when the morning window ends.
 */
object MorningNudge {
    fun shouldPost(now: Instant, s: Settings, clock: LogicalDayClock, alreadyPosted: Boolean, checkInDone: Boolean): Boolean {
        if (!s.on(Feature.MORNING_NUDGE) || !s.on(Feature.MORNING_CHECKIN)) return false
        if (alreadyPosted || checkInDone) return false
        return MorningWindow.contains(clock.dayOf(now), s, clock, now)
    }

    /** When a nudge posted at [now] should clear itself: the end of this morning's window. */
    fun expiresAt(now: Instant, s: Settings, clock: LogicalDayClock): Instant? =
        MorningWindow.of(clock.dayOf(now), s, clock)?.endInclusive
}
