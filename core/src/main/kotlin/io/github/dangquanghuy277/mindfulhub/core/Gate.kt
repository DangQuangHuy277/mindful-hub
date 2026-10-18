package io.github.dangquanghuy277.mindfulhub.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** How strictly an app is gated (D-016: Facebook first, X lighter, games by matches). */
enum class AppProfile { STRICT, LIGHT, GAME, MESSAGING, NONE }

/** What the gate saw on screen. Detectors that can't tell fall back to [APP] (ARCHITECTURE.md). */
enum class Screen { APP, SHORT_VIDEO }

/** The answer to "What for?" (E1). */
enum class Purpose(val label: String) {
    GROUP_TIME("Check a group or page"),
    MESSAGE("Message someone"),
    SHARED_ITEM("See something someone sent me"),
    POST("Post something"),
    WATCH_SPECIFIC("Watch a specific video or stream"),
    PLAY_MATCHES("Play a few matches"),
    JUST_BORED("Honestly, just bored"),
}

data class GateSettings(
    val profiles: Map<String, AppProfile> = DEFAULT_PROFILES,
    val baseDelay: Map<AppProfile, Duration> = mapOf(
        AppProfile.STRICT to Duration.ofSeconds(10),
        AppProfile.LIGHT to Duration.ofSeconds(5),
        AppProfile.GAME to Duration.ofSeconds(5),
        AppProfile.MESSAGING to Duration.ofSeconds(2),
    ),
    /** Free opens per day before the delay starts growing (E5). */
    val freeOpens: Int = 2,
    val escalationStep: Duration = Duration.ofSeconds(5),
    val maxDelay: Duration = Duration.ofSeconds(60),
    /** "Just bored" adds this wait before "continue anyway" (E2). */
    val boredExtraDelay: Duration = Duration.ofSeconds(10),
) {
    fun profileOf(pkg: String): AppProfile = profiles[pkg] ?: AppProfile.NONE

    companion object {
        val DEFAULT_PROFILES = mapOf(
            "com.facebook.katana" to AppProfile.STRICT,
            "com.instagram.android" to AppProfile.STRICT,
            "com.instagram.barcelona" to AppProfile.STRICT, // Threads
            "com.twitter.android" to AppProfile.LIGHT,
            "com.google.android.youtube" to AppProfile.LIGHT,
            "com.hhgame.mlbbvn" to AppProfile.GAME, // Mobile Legends (VN)
            "com.facebook.orca" to AppProfile.MESSAGING,
        )
    }
}

/** A session the user opened through the gate, with its time box. */
data class ActiveSession(val pkg: String, val purpose: Purpose, val start: Instant, val end: Instant)

sealed interface GateDecision {
    data object Allow : GateDecision
    data class Prompt(
        val profile: AppProfile,
        val delay: Duration,
        val purposes: List<Purpose>,
        val timeBoxes: List<TimeBox>,
        /** 1-based open number today; drives escalation and the rotating copy. */
        val openNumber: Int,
    ) : GateDecision
    data class Block(val reason: BlockReason) : GateDecision
}

enum class BlockReason { SHORT_VIDEO }

/** A time-box choice. Games are offered in matches; one match ≈ 20 min (D-006). */
data class TimeBox(val label: String, val length: Duration)

object GatePolicy {
    /**
     * Decides what happens when [pkg] comes to the foreground showing [screen]. Pure: everything
     * that changes (time, sessions, today's opens) comes in as arguments.
     */
    fun decide(
        pkg: String,
        screen: Screen,
        now: Instant,
        sessions: Map<String, ActiveSession>,
        opensToday: Int,
        settings: GateSettings = GateSettings(),
    ): GateDecision {
        val profile = settings.profileOf(pkg)
        if (profile == AppProfile.NONE) return GateDecision.Allow
        if (screen == Screen.SHORT_VIDEO && (profile == AppProfile.STRICT || profile == AppProfile.LIGHT)) {
            return GateDecision.Block(BlockReason.SHORT_VIDEO)
        }
        val session = sessions[pkg]
        if (session != null && now.isBefore(session.end)) return GateDecision.Allow
        val openNumber = opensToday + 1
        return GateDecision.Prompt(
            profile = profile,
            delay = delayFor(profile, openNumber, settings),
            purposes = purposesFor(profile),
            timeBoxes = timeBoxesFor(profile),
            openNumber = openNumber,
        )
    }

    /** Base delay, plus one step per open beyond the free ones, capped (E5). */
    fun delayFor(profile: AppProfile, openNumber: Int, settings: GateSettings = GateSettings()): Duration {
        val base = settings.baseDelay[profile] ?: Duration.ZERO
        if (profile == AppProfile.MESSAGING) return base
        val extraSteps = (openNumber - settings.freeOpens).coerceAtLeast(0).toLong()
        val delay = base.plus(settings.escalationStep.multipliedBy(extraSteps))
        return if (delay > settings.maxDelay) settings.maxDelay else delay
    }

    fun purposesFor(profile: AppProfile): List<Purpose> = when (profile) {
        AppProfile.STRICT -> listOf(Purpose.GROUP_TIME, Purpose.MESSAGE, Purpose.SHARED_ITEM, Purpose.POST, Purpose.JUST_BORED)
        AppProfile.LIGHT -> listOf(Purpose.WATCH_SPECIFIC, Purpose.SHARED_ITEM, Purpose.POST, Purpose.JUST_BORED)
        AppProfile.GAME -> listOf(Purpose.PLAY_MATCHES, Purpose.JUST_BORED)
        AppProfile.MESSAGING -> listOf(Purpose.MESSAGE, Purpose.SHARED_ITEM)
        AppProfile.NONE -> emptyList()
    }

    fun timeBoxesFor(profile: AppProfile): List<TimeBox> = when (profile) {
        AppProfile.GAME -> (1..3).map { TimeBox(if (it == 1) "1 match" else "$it matches", Duration.ofMinutes(20L * it)) }
        AppProfile.LIGHT -> listOf(5, 15, 30, 60).map { TimeBox("$it min", Duration.ofMinutes(it.toLong())) }
        else -> listOf(5, 10, 15).map { TimeBox("$it min", Duration.ofMinutes(it.toLong())) }
    }
}

/** Rotating prompt wording, so the pause doesn't fade into the background (E5, Meinhardt 2026). */
object PromptCopy {
    private val lines = listOf(
        "Pause. What did you come here for?",
        "Is this the thing you planned, or the habit?",
        "You opened this %d times today. What's this one for?",
        "Five seconds of honesty: what's the goal?",
        "If you close it now, what would you do instead?",
    )

    fun line(openNumber: Int, day: LocalDate): String {
        // The count line only makes sense once there is a count worth mentioning.
        val pool = if (openNumber >= 3) lines else lines.filterNot { it.contains("%d") }
        val i = Math.floorMod(openNumber + day.dayOfYear, pool.size)
        return pool[i].let { if (it.contains("%d")) it.format(openNumber - 1) else it }
    }
}

/** Default "Instead" list (R1), grouped by the G5 categories. Editable later. */
object InsteadDefaults {
    val items = listOf(
        "Move" to "Walk for 10 minutes, phone in your pocket",
        "People" to "Text or call someone you like",
        "Read" to "Read one saved article or a book chapter",
        "Build" to "One 25-minute lc-trainer session",
        "Rest" to "Close your eyes for 5 minutes, let your mind wander",
    )
}
