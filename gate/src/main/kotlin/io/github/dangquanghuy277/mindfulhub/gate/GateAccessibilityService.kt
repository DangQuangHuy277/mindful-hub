package io.github.dangquanghuy277.mindfulhub.gate

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import io.github.dangquanghuy277.mindfulhub.core.ActiveSession
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.core.GateDecision
import io.github.dangquanghuy277.mindfulhub.core.GatePolicy
import io.github.dangquanghuy277.mindfulhub.core.PromptCopy
import io.github.dangquanghuy277.mindfulhub.core.Purpose
import io.github.dangquanghuy277.mindfulhub.core.Screen
import io.github.dangquanghuy277.mindfulhub.core.TimeBox
import io.github.dangquanghuy277.mindfulhub.data.GateEventEntity
import io.github.dangquanghuy277.mindfulhub.data.SessionEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * The gate (ARCHITECTURE.md, "Gate flow"). It watches which app comes to the front, asks
 * [GatePolicy] what to do, and acts: nothing, the prompt overlay, or back-out for blocked screens.
 * All rules live in `:core`; this class only collects events and carries out decisions.
 */
class GateAccessibilityService : AccessibilityService() {
    // An uncaught error would crash the process, and Samsung then marks the service as
    // malfunctioning and leaves it off. Log it and keep gating instead.
    private val crashGuard = CoroutineExceptionHandler { _, e -> Log.e(TAG, "gate coroutine failed", e) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + crashGuard)
    private val main = Handler(Looper.getMainLooper())
    private lateinit var deps: GateDeps
    private lateinit var overlay: GateOverlay

    /** Packages whose windows don't mean "the user switched apps" (us, system UI, keyboards). */
    private var ignored: Set<String> = emptySet()
    private var foreground: String? = null
    private var promptingFor: String? = null
    private var promptEventId: Long? = null
    /** Uptime of the window event that triggered the current decision, for the N4 latency log. */
    private var eventUptime = 0L
    private var lastDetect = 0L
    private var suppressDetectUntil = 0L

    // State 1 in ARCHITECTURE.md. In memory only for now: a restart of the service forgets
    // running time boxes, which errs on the side of prompting again.
    private val sessions = mutableMapOf<String, ActiveSession>()
    private val sessionIds = mutableMapOf<String, Long>()
    private val opensCache = mutableMapOf<Pair<String, LocalDate>, Int>()

    override fun onServiceConnected() {
        deps = (application as GateHost).gateDeps
        overlay = GateOverlay(this)
        val imes = getSystemService(InputMethodManager::class.java).enabledInputMethodList.map { it.packageName }
        ignored = setOf(packageName, "android", "com.android.systemui") + imes
        restoreSessions()
    }

    /**
     * Time boxes must survive a service restart (Samsung battery kills, updates, a bank pause, or
     * anything that re-binds accessibility services). Open sessions are reloaded from the database:
     * still running ones get their timer back, overdue ones are closed as TIMEBOX without
     * interrupting anything. Then the app already in front is checked, since it sends no new
     * window event (e.g. you went from the bank straight to Facebook while the gate was off).
     */
    private fun restoreSessions() {
        scope.launch {
            val now = Instant.now()
            for (e in deps.gate.openSessions()) {
                val session = ActiveSession(e.pkg, Purpose.valueOf(e.purpose), Instant.ofEpochMilli(e.start), Instant.ofEpochMilli(e.plannedEnd))
                if (session.end.isAfter(now)) {
                    sessions[e.pkg] = session
                    sessionIds[e.pkg] = e.id
                    main.postDelayed({ onTimeBoxEnd(session) }, Duration.between(now, session.end).toMillis())
                } else {
                    deps.gate.endSession(e.id, e.plannedEnd, "TIMEBOX")
                }
            }
            val top = topApp()
            if (top != null && top !in ignored && foreground == null) onSwitch(top, SystemClock.uptimeMillis())
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg in ignored) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && pkg != foreground) {
            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "switch: $pkg (fg=$foreground, prompting=$promptingFor)")
            onSwitch(pkg, event.eventTime)
        }
        if (pkg == foreground && promptingFor == null) maybeDetectShortVideo(pkg)
    }

    /**
     * Launchers and system animations can emit a window event *after* the target app's first
     * window. Leaving is therefore confirmed only if the user stays away for [LEAVE_CONFIRM_MS];
     * otherwise the same prompt simply stays up (seen on One UI 7: the prompt restarted).
     */
    private fun onSwitch(pkg: String, eventTime: Long) {
        val prompting = promptingFor
        if (prompting != null) {
            if (pkg == prompting) {
                main.removeCallbacks(confirmLeave)
                foreground = pkg
                return
            }
            main.removeCallbacks(confirmLeave)
            main.postDelayed(confirmLeave, LEAVE_CONFIRM_MS)
        }
        foreground = pkg
        eventUptime = eventTime
        onForeground(pkg)
    }

    /**
     * Walking away from the countdown counts like "Never mind": the pause worked (G1). The window
     * list has the last word, because a late launcher event without a following app event would
     * otherwise close the prompt while you're still in the app.
     */
    private val confirmLeave = Runnable {
        val prompting = promptingFor ?: return@Runnable
        if (foreground == prompting) return@Runnable
        if (topApp() == prompting) {
            foreground = prompting
            return@Runnable
        }
        promptEventId?.let { id -> scope.launch { deps.gate.setOutcome(id, "ABANDONED") } }
        closePrompt()
    }

    private fun maybeDetectShortVideo(pkg: String) {
        val profile = deps.settings.profileOf(pkg)
        if (profile != AppProfile.STRICT && profile != AppProfile.LIGHT) return
        val now = SystemClock.uptimeMillis()
        if (now - lastDetect < DETECT_INTERVAL_MS || now < suppressDetectUntil || overlay.isShowing) return
        lastDetect = now
        val d = ScreenDetectors.detect(pkg, rootInActiveWindow)
        if (d.screen == Screen.SHORT_VIDEO) block(pkg, d.escape)
    }

    /** Leaves the short-video screen: tap the app's own escape control if known, else Back. */
    private fun block(pkg: String, escape: android.view.accessibility.AccessibilityNodeInfo? = null) {
        suppressDetectUntil = SystemClock.uptimeMillis() + 2_000
        val now = Instant.now()
        scope.launch {
            deps.gate.insertEvent(event(pkg, now, deps.clock.dayOf(now), "BLOCK", 0, "BLOCKED", Screen.SHORT_VIDEO))
        }
        val escaped = escape?.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK) == true
        if (!escaped) performGlobalAction(GLOBAL_ACTION_BACK)
        val dismiss = {
            overlay.dismiss()
            suppressDetectUntil = SystemClock.uptimeMillis() + 1_000
        }
        overlay.show(onBack = dismiss) { GateBlocked(onOk = dismiss) }
    }

    private fun onForeground(pkg: String) {
        if (deps.settings.pausesGate(pkg)) return pause(pkg)
        val profile = deps.settings.profileOf(pkg)
        if (profile == AppProfile.NONE) return
        val now = Instant.now()
        val day = deps.clock.dayOf(now)
        scope.launch {
            val opens = opensToday(pkg, day)
            when (val d = GatePolicy.decide(pkg, Screen.APP, now, sessions, opens, deps.settings)) {
                GateDecision.Allow -> Unit
                is GateDecision.Prompt -> showPrompt(pkg, d, now, day)
                is GateDecision.Block -> block(pkg)
            }
        }
    }

    /**
     * Banking apps refuse to run while we're on (D-020), so the gate switches itself off and
     * [GatePauseService] switches it back on after you leave. If the watcher can't start, the gate
     * stays on: being stuck off with nothing to bring it back would be worse.
     */
    private fun pause(pkg: String) {
        Log.i(TAG, "pausing for $pkg")
        if (!GatePauseService.start(this, pkg)) return
        promptEventId?.let { id -> scope.launch { deps.gate.setOutcome(id, "ABANDONED") } }
        closePrompt()
        disableSelf()
    }

    /**
     * The app really on top, from the window list rather than the event stream: events arrive out
     * of order (the launcher's late event, see [onSwitch]). Null if the list is empty, e.g. locked.
     */
    private fun topApp(): String? {
        val apps = windows.filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && !it.isInPictureInPictureMode }
        val w = apps.firstOrNull { it.isActive } ?: apps.maxByOrNull { it.layer }
        return w?.root?.packageName?.toString()
    }

    private suspend fun opensToday(pkg: String, day: LocalDate): Int =
        opensCache.getOrPut(pkg to day) { deps.gate.promptsOn(pkg, day.toString()) }

    private suspend fun showPrompt(pkg: String, d: GateDecision.Prompt, now: Instant, day: LocalDate) {
        // Two window events for one open (FB, launcher, FB) start two decisions; show one prompt.
        if (promptingFor == pkg) return
        // Left while we read the database? Only if the window list agrees: the launcher's late
        // event lands exactly here on the first open of the day (cache miss), and trusting it
        // let Facebook through with no prompt at all.
        if (foreground != pkg) {
            if (topApp() != pkg) return
            foreground = pkg
        }
        // Claim the prompt before the next suspension, so a second decision sees it.
        promptingFor = pkg
        opensCache[pkg to day] = d.openNumber
        val eventId = deps.gate.insertEvent(event(pkg, now, day, "PROMPT", d.delay.toMillis(), null))
        if (promptingFor != pkg) { // left while we were logging
            deps.gate.setOutcome(eventId, "ABANDONED")
            return
        }
        promptEventId = eventId
        val label = labelOf(pkg)
        val neverMind = {
            scope.launch { deps.gate.setOutcome(eventId, "ABANDONED") }
            closePrompt()
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
        overlay.show(onBack = { neverMind() }) {
            GatePrompt(
                appLabel = label,
                line = PromptCopy.line(d.openNumber, day),
                prompt = d,
                boredExtraDelay = deps.settings.boredExtraDelay,
                onNeverMind = { neverMind() },
                onProceed = { purpose, box -> startSession(pkg, eventId, purpose, box) },
            )
        }
        Log.i(TAG, "prompt for $pkg shown ${SystemClock.uptimeMillis() - eventUptime} ms after the window event (N4 target ≤300)")
    }

    private fun closePrompt() {
        promptingFor = null
        promptEventId = null
        overlay.dismiss()
    }

    private fun startSession(pkg: String, eventId: Long, purpose: Purpose, box: TimeBox) {
        val start = Instant.now()
        val session = ActiveSession(pkg, purpose, start, start.plus(box.length))
        sessions[pkg] = session
        closePrompt()
        scope.launch {
            deps.gate.setOutcome(eventId, "PROCEEDED")
            sessionIds[pkg] = deps.gate.insertSession(
                SessionEntity(
                    pkg = pkg, purpose = purpose.name, timeBoxMin = box.length.toMinutes().toInt(),
                    start = start.toEpochMilli(), plannedEnd = session.end.toEpochMilli(),
                ),
            )
        }
        main.postDelayed({ onTimeBoxEnd(session) }, box.length.toMillis())
    }

    private fun onTimeBoxEnd(session: ActiveSession) {
        if (sessions[session.pkg] != session) return // replaced by a newer session
        val id = sessionIds[session.pkg]
        val end = Instant.now()
        scope.launch { id?.let { deps.gate.endSession(it, end.toEpochMilli(), "TIMEBOX") } }
        if (foreground != session.pkg) return // already left: nothing to interrupt
        val label = labelOf(session.pkg)
        if (deps.settings.profileOf(session.pkg) == AppProfile.GAME) {
            overlay.show(onBack = overlay::dismiss) { GateGameReminder(label, onOk = overlay::dismiss) }
            return
        }
        val minutes = Duration.between(session.start, end).toMinutes()
        val rateAndLeave = { rating: Int ->
            scope.launch { id?.let { deps.gate.rate(it, rating) } }
            overlay.dismiss()
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
        overlay.show(onBack = { rateAndLeave(0) }) { GateStop(label, minutes, onRate = { rateAndLeave(it) }) }
    }

    private fun event(
        pkg: String, now: Instant, day: LocalDate, decision: String, delayMs: Long, outcome: String?,
        screen: Screen = Screen.APP,
    ) =
        GateEventEntity(
            ts = now.toEpochMilli(), pkg = pkg, screen = screen.name, decision = decision,
            delayMs = delayMs, outcome = outcome, logicalDay = day.toString(),
        )

    private fun labelOf(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun onInterrupt() = Unit

    private companion object {
        /** `adb shell setprop log.tag.MindfulGate DEBUG` turns on per-switch logging (off by default). */
        const val TAG = "MindfulGate"
        const val LEAVE_CONFIRM_MS = 600L
        const val DETECT_INTERVAL_MS = 800L
    }

    override fun onDestroy() {
        overlay.dismiss()
        main.removeCallbacksAndMessages(null)
        main.removeCallbacks(confirmLeave)
        scope.cancel()
        super.onDestroy()
    }
}
