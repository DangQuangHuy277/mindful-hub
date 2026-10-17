package io.github.dangquanghuy277.mindfulhub.gate

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val main = Handler(Looper.getMainLooper())
    private lateinit var deps: GateDeps
    private lateinit var overlay: GateOverlay

    /** Packages whose windows don't mean "the user switched apps" (us, system UI, keyboards). */
    private var ignored: Set<String> = emptySet()
    private var foreground: String? = null
    private var promptingFor: String? = null

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
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg in ignored || pkg == foreground) return
        foreground = pkg

        // Switching away while the prompt is up counts as leaving.
        promptingFor?.let { if (it != pkg) closePrompt() }
        onForeground(pkg)
    }

    private fun onForeground(pkg: String) {
        val profile = deps.settings.profileOf(pkg)
        if (profile == AppProfile.NONE) return
        val now = Instant.now()
        val day = deps.clock.dayOf(now)
        scope.launch {
            val opens = opensToday(pkg, day)
            when (val d = GatePolicy.decide(pkg, Screen.APP, now, sessions, opens, deps.settings)) {
                GateDecision.Allow -> Unit
                is GateDecision.Prompt -> showPrompt(pkg, d, now, day)
                is GateDecision.Block -> {
                    deps.gate.insertEvent(event(pkg, now, day, "BLOCK", 0, "BLOCKED"))
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    overlay.show(onBack = overlay::dismiss) { GateBlocked(onOk = overlay::dismiss) }
                }
            }
        }
    }

    private suspend fun opensToday(pkg: String, day: LocalDate): Int =
        opensCache.getOrPut(pkg to day) { deps.gate.promptsOn(pkg, day.toString()) }

    private suspend fun showPrompt(pkg: String, d: GateDecision.Prompt, now: Instant, day: LocalDate) {
        if (foreground != pkg) return // user already left while we were reading the database
        opensCache[pkg to day] = d.openNumber
        val eventId = deps.gate.insertEvent(event(pkg, now, day, "PROMPT", d.delay.toMillis(), null))
        promptingFor = pkg
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
    }

    private fun closePrompt() {
        promptingFor = null
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

    private fun event(pkg: String, now: Instant, day: LocalDate, decision: String, delayMs: Long, outcome: String?) =
        GateEventEntity(
            ts = now.toEpochMilli(), pkg = pkg, screen = Screen.APP.name, decision = decision,
            delayMs = delayMs, outcome = outcome, logicalDay = day.toString(),
        )

    private fun labelOf(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        overlay.dismiss()
        main.removeCallbacksAndMessages(null)
        scope.cancel()
        super.onDestroy()
    }
}
