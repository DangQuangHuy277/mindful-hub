package io.github.dangquanghuy277.mindfulhub.ui.reaction

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.BuildConfig
import io.github.dangquanghuy277.mindfulhub.MindfulApp
import io.github.dangquanghuy277.mindfulhub.core.MorningWindow
import io.github.dangquanghuy277.mindfulhub.core.PvtFeedback
import io.github.dangquanghuy277.mindfulhub.core.PvtRun
import io.github.dangquanghuy277.mindfulhub.core.PvtScoring
import io.github.dangquanghuy277.mindfulhub.core.Pvtb
import io.github.dangquanghuy277.mindfulhub.core.ReactionUsual
import io.github.dangquanghuy277.mindfulhub.core.VsUsual
import io.github.dangquanghuy277.mindfulhub.data.ReactionRunEntity
import io.github.dangquanghuy277.mindfulhub.theme.MindfulTheme
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * F75 (trial): the morning reaction check, full screen. The rules are `Pvtb` and `PvtRun` (:core);
 * this screen only draws the star and times things. Stimulus onset is the frame time it was drawn
 * in, and a tap is the touch's own event time; both are on the monotonic clock, so the reaction
 * time doesn't depend on how busy the app is. Leaving the screen ends the run (kept as abandoned).
 */
class ReactionActivity : ComponentActivity() {
    private lateinit var game: ReactionGame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        // MorningDebug's practice run: debug builds only, and nothing is saved (A6).
        val practice = intent.getBooleanExtra(EXTRA_PRACTICE, false) &&
            applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        game = ReactionGame((application as MindfulApp).container, display?.refreshRate ?: 0f, practice)
        setContent { MindfulTheme { ReactionScreen(game, onClose = ::finish) } }
    }

    override fun onPause() {
        super.onPause()
        if (game.phase == Phase.RUNNING) {
            game.abandon()
            finish()
        }
    }

    companion object {
        const val EXTRA_PRACTICE = "practice"
    }
}

enum class Phase { INTRO, RUNNING, DONE }

/** One sitting: intro → run → the one line. Saving outlives the screen (an app-wide scope). */
class ReactionGame(private val c: AppContainer, private val refreshHz: Float, val practice: Boolean = false) {
    var phase by mutableStateOf(Phase.INTRO)
        private set
    var starVisible by mutableStateOf(false)
        private set
    var feedback by mutableStateOf<PvtFeedback?>(null)
        private set
    var progress by mutableFloatStateOf(0f)
        private set
    var result by mutableStateOf<VsUsual?>(null)
        private set

    private val run = PvtRun()
    private var startedAt: Instant = Instant.EPOCH
    private var startMs = -1L
    private var feedbackUntil = 0L

    fun start() {
        run.start()
        startMs = -1L
        startedAt = Instant.now()
        phase = Phase.RUNNING
    }

    /** Every frame while running. [frameMs] is the frame time on the monotonic clock. */
    fun onFrame(frameMs: Long) {
        if (phase != Phase.RUNNING) return
        if (startMs < 0) startMs = frameMs
        val t = frameMs - startMs
        progress = (t.toFloat() / Pvtb.DURATION_MS).coerceIn(0f, 1f)
        run.tick(t)?.let { show(it, t) }
        val next = run.nextStimulusAt
        if (next != null && t >= next) {
            run.showStimulus(t)
            starVisible = run.stimulusAt != null
            if (starVisible) feedback = null
        }
        if (feedback != null && t >= feedbackUntil) feedback = null
        if (run.isOver(t)) finish()
    }

    /** [uptimeMs] is the touch's event time, on the same clock as the frames. */
    fun onTap(uptimeMs: Long) {
        if (phase != Phase.RUNNING || startMs < 0) return
        val t = uptimeMs - startMs
        show(run.tap(t), t)
    }

    private fun show(f: PvtFeedback, t: Long) {
        starVisible = false
        feedback = f
        feedbackUntil = t + Pvtb.FEEDBACK_MS
    }

    fun abandon() = save(completed = false)

    private fun finish() {
        starVisible = false
        feedback = null
        save(completed = true)
        phase = Phase.DONE
    }

    private fun save(completed: Boolean) {
        val events = run.events.toList()
        val ended = Instant.now()
        val day = c.clock.dayOf(startedAt)
        if (practice) {
            val score = PvtScoring.score(events)
            Log.d("MindfulMorning", "practice run, completed=$completed, ${events.size} events: $score")
            return
        }
        io.launch {
            val score = PvtScoring.score(events)
            val firstUnlock = c.morningNudges.get(day.toString())?.firstUnlockAt
                ?: MorningWindow.of(day, c.settingsStore.on(day), c.clock)?.let { w ->
                    if (c.importer.hasPermission()) runCatching { c.importer.importNew() }
                    c.usage.firstUnlock(w.start, startedAt)?.toEpochMilli()
                }
            val earlier = c.reactions.earlierMedians(day.toString())
            c.reactions.insertRun(
                ReactionRunEntity(
                    day = day.toString(), startedAt = startedAt.toEpochMilli(), endedAt = ended.toEpochMilli(),
                    completed = completed, protocolVersion = Pvtb.VERSION, events = PvtScoring.encode(events),
                    stimuli = score?.stimuli, meanSpeed = score?.meanSpeed, medianRtMs = score?.medianRtMs,
                    lapses = score?.lapses, falseStarts = score?.falseStarts, fastest10RtMs = score?.fastest10RtMs,
                    slowest10Speed = score?.slowest10Speed, performance = score?.performance,
                    firstUnlockAt = firstUnlock, device = "${Build.MANUFACTURER} ${Build.MODEL}", refreshHz = refreshHz,
                    gitSha = BuildConfig.GIT_SHA,
                ),
            )
            if (completed && score != null) {
                val line = ReactionUsual.compare(score.medianRtMs, earlier)
                withContext(Dispatchers.Main) { result = line }
            }
        }
    }

    private companion object {
        val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

@Composable
private fun ReactionScreen(game: ReactionGame, onClose: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (game.phase) {
            Phase.INTRO -> Intro(onStart = game::start, onClose = onClose)
            Phase.RUNNING -> Running(game)
            Phase.DONE -> Done(game.result, game.practice, onClose)
        }
    }
}

@Composable
private fun Intro(onStart: () -> Unit, onClose: () -> Unit) {
    Column(
        Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Text("⭐ Morning reaction check", style = MaterialTheme.typography.headlineSmall)
        Text("A star appears every few seconds. Tap anywhere as soon as you see it.")
        Text("Wait for the star: a tap before it counts as too soon.")
        Text("3 minutes. Leaving the screen ends the run.")
        Text(
            "The rules are the same every morning, so each morning compares with your own.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PillButton("Start", onClick = onStart, emoji = "⭐", tone = PillTone.Accent)
        PillButton("Not now", onClick = onClose, emoji = "🌿", tone = PillTone.Mint)
    }
}

@Composable
private fun Running(game: ReactionGame) {
    LaunchedEffect(Unit) {
        while (game.phase == Phase.RUNNING) withFrameNanos { game.onFrame(it / 1_000_000) }
    }
    val c = MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            awaitEachGesture { game.onTap(awaitFirstDown(requireUnconsumed = false).uptimeMillis) }
        },
    ) {
        LinearProgressIndicator(
            progress = { game.progress },
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 24.dp, vertical = 16.dp),
        )
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(180.dp).background(c.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                if (game.starVisible) Text("⭐", fontSize = 96.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                when (val f = game.feedback) {
                    is PvtFeedback.Reaction -> "${f.ms} ms"
                    PvtFeedback.TooSoon -> "Too soon"
                    PvtFeedback.Missed -> "Missed"
                    null -> " "
                },
                style = MaterialTheme.typography.headlineMedium, color = c.onSurfaceVariant,
            )
        }
        Text(
            "Tap anywhere", Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
            style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
        )
    }
}

/** One line against your own usual (P48): no score, nothing to beat. */
@Composable
private fun Done(result: VsUsual?, practice: Boolean, onClose: () -> Unit) {
    Column(
        Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Done 🌤️", style = MaterialTheme.typography.headlineSmall)
        if (practice) {
            Text("Practice run (debug build): nothing was saved.", textAlign = TextAlign.Center)
        } else {
            Text(
                when (result) {
                    null -> "Saving…"
                    VsUsual.FIRST_MORNINGS -> "Saved. After ${ReactionUsual.MIN_RUNS} mornings, this compares with your own usual."
                    VsUsual.ABOUT_USUAL -> "About your usual this morning."
                    VsUsual.FASTER -> "A bit quicker than your usual this morning."
                    VsUsual.SLOWER -> "A bit slower than your usual this morning. Maybe start with the easier tasks."
                },
                textAlign = TextAlign.Center,
            )
        }
        PillButton("Back to today", onClick = onClose, emoji = "🏡", tone = PillTone.Lavender)
    }
}
