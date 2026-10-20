package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant

/** SCREEN_ON, SHUTDOWN and STARTUP were added for the night log (F64); older history has none. */
enum class UsageEventType { FOREGROUND, BACKGROUND, SCREEN_OFF, UNLOCK, SCREEN_ON, SHUTDOWN, STARTUP }

/** One system usage event, already mapped from Android's `UsageEvents.Event` by `:data`. */
data class UsageEvent(val at: Instant, val pkg: String, val type: UsageEventType)

/** A continuous stretch of one app in the foreground. */
data class AppSession(val pkg: String, val start: Instant, val end: Instant) {
    val duration: Duration get() = Duration.between(start, end)
}

object Sessionizer {
    /**
     * Turns raw events into foreground sessions.
     *
     * - A session ends at the app's own BACKGROUND event, at the next app's FOREGROUND, or at
     *   SCREEN_OFF or SHUTDOWN, whichever comes first.
     * - A session still open at the end of the log is closed at `min(now, start + maxOpen)`, so one
     *   lost BACKGROUND event can't inflate a whole night.
     * - Same-app sessions separated by at most [mergeGap] are merged: switching activities inside
     *   one app emits PAUSED then RESUMED, which isn't a new "open".
     */
    fun sessions(
        events: List<UsageEvent>,
        now: Instant,
        maxOpen: Duration = Duration.ofHours(3),
        mergeGap: Duration = Duration.ofSeconds(5),
    ): List<AppSession> {
        val raw = mutableListOf<AppSession>()
        var openPkg: String? = null
        var openAt: Instant? = null

        fun close(at: Instant) {
            val pkg = openPkg
            val start = openAt
            if (pkg != null && start != null && at.isAfter(start)) raw += AppSession(pkg, start, at)
            openPkg = null
            openAt = null
        }

        for (e in events.sortedBy { it.at }) {
            when (e.type) {
                UsageEventType.FOREGROUND -> if (e.pkg != openPkg) {
                    close(e.at)
                    openPkg = e.pkg
                    openAt = e.at
                }
                UsageEventType.BACKGROUND -> if (e.pkg == openPkg) close(e.at)
                UsageEventType.SCREEN_OFF, UsageEventType.SHUTDOWN -> close(e.at)
                UsageEventType.UNLOCK, UsageEventType.SCREEN_ON, UsageEventType.STARTUP -> Unit
            }
        }
        openAt?.let { close(minOf(now, it.plus(maxOpen))) }
        return merge(raw, mergeGap)
    }

    private fun merge(sessions: List<AppSession>, gap: Duration): List<AppSession> {
        val out = mutableListOf<AppSession>()
        for (s in sessions) {
            val last = out.lastOrNull()
            if (last != null && last.pkg == s.pkg && Duration.between(last.end, s.start) <= gap) {
                out[out.lastIndex] = last.copy(end = s.end)
            } else {
                out += s
            }
        }
        return out
    }
}
