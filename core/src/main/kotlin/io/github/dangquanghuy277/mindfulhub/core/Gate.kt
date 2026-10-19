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
    /** F03 option: the one 10-minute scroll in bed (D-021). Never offered in the normal list. */
    BED_SCROLL("One wind-down scroll"),
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
    /**
     * Apps that refuse to run while a sideloaded accessibility service is on (D-020). The gate
     * switches itself off while one of them is in front and back on once you've left.
     */
    val pauseFor: Set<String> = DEFAULT_PAUSE_FOR,
) {
    fun profileOf(pkg: String): AppProfile = profiles[pkg] ?: AppProfile.NONE

    /** A pause app is never gated, even if it also has a profile: the pause wins. */
    fun pausesGate(pkg: String): Boolean = pkg in pauseFor

    companion object {
        val DEFAULT_PROFILES = mapOf(
            "com.facebook.katana" to AppProfile.STRICT,
            "com.instagram.android" to AppProfile.STRICT,
            "com.instagram.barcelona" to AppProfile.STRICT, // Threads
            "com.twitter.android" to AppProfile.LIGHT,
            "com.google.android.youtube" to AppProfile.LIGHT,
            "com.hhgame.mlbbvn" to AppProfile.GAME, // Mobile Legends (VN)
            // Messenger is not gated (2026-10-19, your call): a 2 s wait on every incoming message
            // cost more than it saved. MESSAGING stays for any chat app you want gated later.
        )

        /** Banking and payment apps installed on the S22 (checked with `pm list packages`, 2026-10-19). */
        val DEFAULT_PAUSE_FOR = setOf(
            "vn.com.techcombank.bb.app", // Techcombank
            "com.VCB", // Vietcombank
            "com.vnpay.bidv", // BIDV SmartBanking
            "com.mservice.momotransfer", // MoMo
            "vn.com.vng.zalopay", // ZaloPay
            "vnpay.smartacccount", // VNPAY
            "com.fss.tcbs.mobiletrading", // TCBS
            "vn.com.fss.vcbf", // VCBF
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
        /** F31 + F53: after bedtime, the postcard comes before the pause. */
        val postcardDue: Boolean = false,
    ) : GateDecision
    data class Block(val reason: BlockReason) : GateDecision

    /**
     * The app is resting (F03 wind-down, F29 budget used up). Unlike [Block] this is shown at the
     * open, with what's possible instead: the postcard (F53), a bed scroll if allowed tonight.
     */
    data class Locked(val reason: LockReason, val bedScroll: Boolean = false, val postcardDue: Boolean = false) : GateDecision
}

enum class BlockReason { SHORT_VIDEO }

enum class LockReason { WIND_DOWN, BUDGET }

/**
 * What the newer features add to a decision (D-021). Everything defaults to "off", so with
 * `GateRules()` the gate behaves exactly like the gate core (F01, F02, F05, F06).
 */
data class GateRules(
    val night: NightPhase = NightPhase.DAY,
    /** F03 is on. */
    val windDownLock: Boolean = false,
    /** F03 option is on and tonight's scroll hasn't been used. */
    val bedScrollLeft: Boolean = false,
    /** F31 + F53 is on and no postcard has been written tonight. */
    val postcardDue: Boolean = false,
    /** F29: minutes left in today's feeds budget, or null when there's no budget. */
    val feedMinutesLeft: Double? = null,
)

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
        rules: GateRules = GateRules(),
    ): GateDecision {
        val profile = settings.profileOf(pkg)
        if (profile == AppProfile.NONE) return GateDecision.Allow
        if (screen == Screen.SHORT_VIDEO && (profile == AppProfile.STRICT || profile == AppProfile.LIGHT)) {
            return GateDecision.Block(BlockReason.SHORT_VIDEO)
        }
        val session = sessions[pkg]
        if (session != null && now.isBefore(session.end)) return GateDecision.Allow
        // Messages stay light at night: no postcard step before them.
        val postcard = rules.postcardDue && rules.night == NightPhase.AFTER_BEDTIME && profile != AppProfile.MESSAGING
        if (rules.windDownLock && rules.night != NightPhase.DAY && profile != AppProfile.MESSAGING) {
            return GateDecision.Locked(LockReason.WIND_DOWN, bedScroll = rules.bedScrollLeft, postcardDue = rules.postcardDue)
        }
        val left = rules.feedMinutesLeft.takeIf { profile == AppProfile.STRICT }
        if (left != null && left < 1.0) return GateDecision.Locked(LockReason.BUDGET, postcardDue = postcard)
        val openNumber = opensToday + 1
        return GateDecision.Prompt(
            profile = profile,
            delay = delayFor(profile, openNumber, settings),
            purposes = purposesFor(profile, pkg),
            timeBoxes = capTo(timeBoxesFor(profile), left),
            openNumber = openNumber,
            postcardDue = postcard,
        )
    }

    /**
     * F29: never offer a box longer than the budget left. If the shortest box doesn't fit, the one
     * box offered is what's left, so the last few minutes are usable rather than lost.
     */
    fun capTo(boxes: List<TimeBox>, minutesLeft: Double?): List<TimeBox> {
        if (minutesLeft == null) return boxes
        val fits = boxes.filter { it.length.toMinutes() <= minutesLeft }
        val whole = minutesLeft.toLong().coerceAtLeast(1)
        return if (fits.size == boxes.size || fits.any { it.length.toMinutes() == whole }) fits
        else fits + TimeBox("$whole min (all that's left)", Duration.ofMinutes(whole))
    }

    /** Base delay, plus one step per open beyond the free ones, capped (E5). */
    fun delayFor(profile: AppProfile, openNumber: Int, settings: GateSettings = GateSettings()): Duration {
        val base = settings.baseDelay[profile] ?: Duration.ZERO
        if (profile == AppProfile.MESSAGING) return base
        val extraSteps = (openNumber - settings.freeOpens).coerceAtLeast(0).toLong()
        val delay = base.plus(settings.escalationStep.multipliedBy(extraSteps))
        return if (delay > settings.maxDelay) settings.maxDelay else delay
    }

    /** Facebook's own app, whose messages live in Messenger (D-023). */
    const val FACEBOOK = "com.facebook.katana"
    const val YOUTUBE = "com.google.android.youtube"

    fun purposesFor(profile: AppProfile, pkg: String? = null): List<Purpose> = when (profile) {
        // Messaging happens in Messenger, so Facebook never offers the two messaging reasons (D-023).
        AppProfile.STRICT if pkg == FACEBOOK -> listOf(Purpose.GROUP_TIME, Purpose.POST, Purpose.JUST_BORED)
        AppProfile.STRICT -> listOf(Purpose.GROUP_TIME, Purpose.MESSAGE, Purpose.SHARED_ITEM, Purpose.POST, Purpose.JUST_BORED)
        // A video someone sent is still "a specific video" (D-023).
        AppProfile.LIGHT if pkg == YOUTUBE -> listOf(Purpose.WATCH_SPECIFIC, Purpose.POST, Purpose.JUST_BORED)
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
