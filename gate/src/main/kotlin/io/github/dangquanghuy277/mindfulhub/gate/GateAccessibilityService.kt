package io.github.dangquanghuy277.mindfulhub.gate

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import io.github.dangquanghuy277.mindfulhub.core.ActiveSession
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.FeedBudget
import io.github.dangquanghuy277.mindfulhub.core.GateDecision
import io.github.dangquanghuy277.mindfulhub.core.GatePolicy
import io.github.dangquanghuy277.mindfulhub.core.GateRules
import io.github.dangquanghuy277.mindfulhub.core.InsteadAction
import io.github.dangquanghuy277.mindfulhub.core.InsteadItem
import io.github.dangquanghuy277.mindfulhub.core.NightClock
import io.github.dangquanghuy277.mindfulhub.core.NightPhase
import io.github.dangquanghuy277.mindfulhub.core.NightState
import io.github.dangquanghuy277.mindfulhub.core.PromptCopy
import io.github.dangquanghuy277.mindfulhub.core.Purpose
import io.github.dangquanghuy277.mindfulhub.core.Screen
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.TimeBox
import io.github.dangquanghuy277.mindfulhub.data.GateEventEntity
import io.github.dangquanghuy277.mindfulhub.data.GoodThingEntity
import io.github.dangquanghuy277.mindfulhub.data.PostcardEntity
import io.github.dangquanghuy277.mindfulhub.data.SessionEntity
import io.github.dangquanghuy277.mindfulhub.data.SettingEntity
import io.github.dangquanghuy277.mindfulhub.data.SettingsStore
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    // D-021 features. Settings rows are kept current from the database; each decision decodes them
    // for its own logical day, so a change that waits for tomorrow starts at 04:00 by itself.
    private var settingRows: List<SettingEntity> = emptyList()
    /** Home-screen apps: going there from a time box is "leaving on your own" (F10). */
    private var launchers: Set<String> = emptySet()
    /** When you left an app whose time box is still running. Cleared if you come back. */
    private val leftAt = mutableMapOf<String, Instant>()
    /** Feeds minutes used today and when they were read (F29). A fresh value skips the wait. */
    private var feedCache: Pair<Instant, Double>? = null

    override fun onServiceConnected() {
        deps = (application as GateHost).gateDeps
        overlay = GateOverlay(this)
        val imes = getSystemService(InputMethodManager::class.java).enabledInputMethodList.map { it.packageName }
        ignored = setOf(packageName, "android", "com.android.systemui") + imes
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        launchers = packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }.toSet()
        scope.launch { deps.rules.observe().collect { settingRows = it } }
        restoreSessions()
        connected = this
    }

    private fun settingsOn(day: LocalDate): Settings = SettingsStore.decode(settingRows, day)

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
        val previous = foreground
        foreground = pkg
        eventUptime = eventTime
        if (previous != null && prompting == null) noteLeaving(previous, pkg)
        onForeground(pkg)
    }

    /**
     * F10: leaving an app while its time box still runs. Confirmed after [LEAVE_CONFIRM_MS] from
     * the window list, because launchers send late events (see [onSwitch]). Going to the home
     * screen with a minute or more left earns a small toast: stopping on your own is the win.
     */
    private fun noteLeaving(previous: String, now: String) {
        val session = sessions[previous] ?: return
        val left = Instant.now()
        if (!session.end.isAfter(left)) return
        main.postDelayed({
            if (topApp() == previous || foreground == previous) return@postDelayed
            leftAt[previous] = left
            val minutes = Duration.between(left, session.end).toMinutes()
            val day = deps.clock.dayOf(left)
            if (now in launchers && minutes >= 1 && settingsOn(day).on(Feature.WORTH_IT)) {
                Toast.makeText(this, "Left $minutes min early 🌿 That counts.", Toast.LENGTH_SHORT).show()
            }
        }, LEAVE_CONFIRM_MS)
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
        if (deps.settings.pausesGate(pkg)) {
            pause(pkg)
            return
        }
        val profile = deps.settings.profileOf(pkg)
        if (profile == AppProfile.NONE) return
        val now = Instant.now()
        val day = deps.clock.dayOf(now)
        leftAt.remove(pkg)
        scope.launch {
            val opens = opensToday(pkg, day)
            val s = settingsOn(day)
            val night = NightClock.state(now, deps.clock, s)
            val rules = rulesFor(pkg, profile, now, s, night)
            when (val d = GatePolicy.decide(pkg, Screen.APP, now, sessions, opens, deps.settings, rules)) {
                GateDecision.Allow -> Unit
                is GateDecision.Prompt -> showPrompt(pkg, d, now, day, s, night)
                is GateDecision.Block -> block(pkg)
                is GateDecision.Locked -> showLocked(pkg, d, now, day, s, night)
            }
        }
    }

    /** Builds the D-021 inputs for one decision. Each part only runs when its switch is on. */
    private suspend fun rulesFor(pkg: String, profile: AppProfile, now: Instant, s: Settings, night: NightState): GateRules {
        if (sessions[pkg]?.end?.isAfter(now) == true) return GateRules() // inside a box: nothing else matters
        val atNight = night.phase != NightPhase.DAY
        val windDown = s.on(Feature.WIND_DOWN)
        val nightFrom = night.bed.minus(Duration.ofMinutes(s.windDownMinutes.toLong()))
        val bedScrollLeft = windDown && atNight && s.allowBedScroll &&
            deps.gate.bedScrollsBetween(nightFrom.toEpochMilli(), night.wake.toEpochMilli()) == 0
        val postcardDue = s.on(Feature.POSTCARD) && atNight && deps.postcards.forNight(night.night.toString()) == null
        val feedLeft = if (s.on(Feature.FEED_BUDGET) && profile == AppProfile.STRICT) {
            FeedBudget.left(s.feedBudgetMinutes, feedMinutes(now) ?: 0.0)
        } else {
            null
        }
        return GateRules(night.phase, windDown, bedScrollLeft, postcardDue, feedLeft)
    }

    /**
     * F29: minutes used today. Reading usage history can take a while, and the prompt has a 300 ms
     * target (N4), so a value under [FEED_FRESH] old is used at once and refreshed in the
     * background; otherwise it waits at most [BUDGET_TIMEOUT_MS] and falls back to the old value.
     */
    private suspend fun feedMinutes(now: Instant): Double? {
        val cached = feedCache?.takeIf { (at, _) -> deps.clock.dayOf(at) == deps.clock.dayOf(now) }
        suspend fun read(): Double? = runCatching { deps.feedMinutesToday(now) }.getOrNull()?.also { feedCache = now to it }
        if (cached != null && Duration.between(cached.first, now) < FEED_FRESH) {
            scope.launch { read() }
            return cached.second
        }
        return withTimeoutOrNull(BUDGET_TIMEOUT_MS) { read() } ?: cached?.second
    }

    /** F03 wind-down or F29 budget: the app rests, and the screen says why and what's possible. */
    private suspend fun showLocked(pkg: String, d: GateDecision.Locked, now: Instant, day: LocalDate, s: Settings, night: NightState) {
        if (promptingFor == pkg) return
        if (foreground != pkg) {
            if (topApp() != pkg) return
            foreground = pkg
        }
        promptingFor = pkg
        val eventId = deps.gate.insertEvent(event(pkg, now, day, "LOCK", 0, "BLOCKED", Screen.APP).copy(screen = d.reason.name))
        if (promptingFor != pkg) return
        promptEventId = null
        val label = labelOf(pkg)
        val close: () -> Unit = {
            closePrompt()
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
        overlay.show(onBack = close) {
            GateLocked(
                appLabel = label,
                decision = d,
                bedtimeText = timeText(night.bed),
                wakeText = timeText(night.wake),
                budgetMinutes = s.feedBudgetMinutes,
                night = night.phase,
                showInstead = s.on(Feature.INSTEAD),
                onInstead = { doInstead(pkg, null, it) },
                onPostcard = { savePostcard(night, it) },
                onBedScroll = { startSession(pkg, eventId, Purpose.BED_SCROLL, TimeBox("10 min", Duration.ofMinutes(10)), logOutcome = false) },
                onClose = close,
            )
        }
    }

    private fun savePostcard(night: NightState, text: String) {
        scope.launch { deps.postcards.save(PostcardEntity(night.night.toString(), text, System.currentTimeMillis())) }
    }

    /**
     * F26: do the thing instead. It's logged as a good thing right away (you can delete it at
     * home), the open counts as abandoned (a win, G1), and the app closes.
     */
    private fun doInstead(pkg: String, eventId: Long?, item: InsteadItem) {
        scope.launch {
            eventId?.let { deps.gate.setOutcome(it, "ABANDONED") }
            deps.goodThings.insert(
                GoodThingEntity(ts = System.currentTimeMillis(), category = item.category.name, minutes = item.minutes, note = item.text, source = "gate"),
            )
        }
        closePrompt()
        performGlobalAction(GLOBAL_ACTION_HOME)
        val started = when (val a = item.action) {
            is InsteadAction.Timer -> launch(
                Intent(AlarmClock.ACTION_SET_TIMER)
                    .putExtra(AlarmClock.EXTRA_LENGTH, a.minutes * 60)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, item.text)
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, true),
            )
            is InsteadAction.OpenApp -> packageManager.getLaunchIntentForPackage(a.pkg)?.let(::launch) ?: false
            InsteadAction.Dial -> launch(Intent(Intent.ACTION_DIAL))
            InsteadAction.Note -> false
        }
        val what = if (item.action is InsteadAction.Timer && started) "Timer started. " else ""
        Toast.makeText(this, "${what}Nice. ${item.category.emoji} logged.", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "instead from $pkg: ${item.category} ${item.minutes} min (started=$started)")
    }

    private fun launch(intent: Intent): Boolean =
        runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess

    private fun timeText(at: Instant): String = at.atZone(ZoneId.systemDefault()).format(HH_MM)

    /**
     * Banking apps refuse to run while we're on (D-020), so the gate switches itself off and
     * [GatePauseService] switches it back on after you leave. If the watcher can't start, the gate
     * stays on: being stuck off with nothing to bring it back would be worse.
     *
     * The bank's window event comes about 600 ms after its code starts, too late for Techcombank,
     * which checks as it starts and closes itself on that first open. [BankLaunchActivity] pauses
     * before the launch instead.
     */
    private fun pause(pkg: String, launching: Boolean = false): Boolean {
        Log.i(TAG, "pausing for $pkg${if (launching) " (before launch)" else ""}")
        if (!GatePauseService.start(this, pkg, launching)) return false
        promptEventId?.let { id -> scope.launch { deps.gate.setOutcome(id, "ABANDONED") } }
        closePrompt()
        disableSelf()
        return true
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

    private suspend fun showPrompt(pkg: String, d: GateDecision.Prompt, now: Instant, day: LocalDate, s: Settings, night: NightState) {
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
                instead = if (s.on(Feature.INSTEAD)) ({ item -> doInstead(pkg, eventId, item) }) else null,
                night = night.phase,
                wakeText = timeText(night.wake),
                onPostcard = { savePostcard(night, it) },
            )
        }
        Log.i(TAG, "prompt for $pkg shown ${SystemClock.uptimeMillis() - eventUptime} ms after the window event (N4 target ≤300)")
    }

    private fun closePrompt() {
        promptingFor = null
        promptEventId = null
        overlay.dismiss()
    }

    private fun startSession(pkg: String, eventId: Long, purpose: Purpose, box: TimeBox, logOutcome: Boolean = true) {
        val start = Instant.now()
        val session = ActiveSession(pkg, purpose, start, start.plus(box.length))
        sessions[pkg] = session
        leftAt.remove(pkg)
        closePrompt()
        scope.launch {
            if (logOutcome) deps.gate.setOutcome(eventId, "PROCEEDED")
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
        // Left on your own and didn't come back: the box ended when you left (F10, a win).
        val left = leftAt.remove(session.pkg)
        if (foreground != session.pkg && left != null) {
            scope.launch { id?.let { deps.gate.endSession(it, left.toEpochMilli(), "USER") } }
            return
        }
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
        val ask = settingsOn(deps.clock.dayOf(end)).on(Feature.WORTH_IT)
        val leave = { rating: Int ->
            if (ask) {
                rateAndLeave(rating)
            } else {
                overlay.dismiss()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
        overlay.show(onBack = { leave(0) }) { GateStop(label, minutes, askRating = ask, onRate = { leave(it) }) }
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

    companion object {
        /** `adb shell setprop log.tag.MindfulGate DEBUG` turns on per-switch logging (off by default). */
        private const val TAG = "MindfulGate"
        private const val LEAVE_CONFIRM_MS = 600L
        private const val DETECT_INTERVAL_MS = 800L
        /** The budget check must not delay the prompt past N4 (≤300 ms); it falls back to the last value. */
        private const val BUDGET_TIMEOUT_MS = 250L
        private val FEED_FRESH: Duration = Duration.ofMinutes(2)
        private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /** The connected gate, if any. Same process as the app, so the bank shortcut can reach it. */
        @Volatile
        private var connected: GateAccessibilityService? = null

        val isConnected: Boolean get() = connected != null

        /** Switches the gate off for [pkg] before it starts (the bank shortcut). Main thread only. */
        fun pauseBeforeLaunch(pkg: String): Boolean = connected?.pause(pkg, launching = true) ?: false
    }

    override fun onDestroy() {
        connected = null
        overlay.dismiss()
        main.removeCallbacksAndMessages(null)
        main.removeCallbacks(confirmLeave)
        scope.cancel()
        super.onDestroy()
    }
}
