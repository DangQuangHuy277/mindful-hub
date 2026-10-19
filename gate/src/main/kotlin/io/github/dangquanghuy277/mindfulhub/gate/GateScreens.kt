package io.github.dangquanghuy277.mindfulhub.gate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dangquanghuy277.mindfulhub.core.GateDecision
import io.github.dangquanghuy277.mindfulhub.core.InsteadDefaults
import io.github.dangquanghuy277.mindfulhub.core.InsteadItem
import io.github.dangquanghuy277.mindfulhub.core.NightPhase
import io.github.dangquanghuy277.mindfulhub.core.Purpose
import io.github.dangquanghuy277.mindfulhub.core.TimeBox
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.CountdownRing
import io.github.dangquanghuy277.mindfulhub.theme.EmojiCircleButton
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import kotlinx.coroutines.delay
import java.time.Duration

private val Purpose.emoji: String
    get() = when (this) {
        Purpose.GROUP_TIME -> "👥"
        Purpose.MESSAGE -> "💬"
        Purpose.SHARED_ITEM -> "💌"
        Purpose.POST -> "✍️"
        Purpose.WATCH_SPECIFIC -> "🎬"
        Purpose.PLAY_MATCHES -> "🎮"
        Purpose.JUST_BORED -> "🥱"
        Purpose.BED_SCROLL -> "🌙"
    }

/** Emoji for the Instead categories (F15); unknown ones get a sparkle. */
private fun insteadEmoji(category: String): String = when (category) {
    "Move" -> "🚶"
    "People" -> "💛"
    "Read" -> "📖"
    "Build" -> "🛠️"
    "Rest" -> "😌"
    else -> "✨"
}

private sealed interface Phase {
    /** F31 + F53: after bedtime, the postcard comes first. */
    data object Postcard : Phase
    data object Countdown : Phase
    data object ChoosePurpose : Phase
    data object Bored : Phase
    data class ChooseBox(val purpose: Purpose) : Phase
}

/**
 * The prompt (E1–E3, E5): wait out the pause, say what for, pick a time box. "Never mind" is
 * always available immediately: leaving must never be slower than staying.
 */
@Composable
fun GatePrompt(
    appLabel: String,
    line: String,
    prompt: GateDecision.Prompt,
    boredExtraDelay: Duration,
    onNeverMind: () -> Unit,
    onProceed: (Purpose, TimeBox) -> Unit,
    /** F26 + F59 is on: "just bored" offers one-tap things to start instead of a list to read. */
    instead: ((InsteadItem) -> Unit)? = null,
    night: NightPhase = NightPhase.DAY,
    wakeText: String = "",
    onPostcard: (String) -> Unit = {},
    /** Reports where the prompt is, so the outcome says where you decided (v4). */
    onStage: (String) -> Unit = {},
) {
    var phase by remember { mutableStateOf<Phase>(if (prompt.postcardDue) Phase.Postcard else Phase.Countdown) }
    LaunchedEffect(phase) {
        onStage(
            when (phase) {
                Phase.Postcard -> "POSTCARD"
                Phase.Countdown -> "COUNTDOWN"
                Phase.ChoosePurpose -> "PURPOSE"
                Phase.Bored -> "BORED"
                is Phase.ChooseBox -> "BOX"
            },
        )
    }
    GateSurface {
        Text(appLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(line, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        when (val p = phase) {
            Phase.Postcard -> {
                PostcardBox(wakeText, onPostcard)
                TextButton(onClick = { phase = Phase.Countdown }) { Text("Continue") }
            }
            Phase.Countdown -> Countdown(prompt.delay) { phase = Phase.ChoosePurpose }
            Phase.ChoosePurpose -> {
                Text("What for?", style = MaterialTheme.typography.titleSmall)
                prompt.purposes.forEach { purpose ->
                    PillButton(
                        purpose.label,
                        onClick = { phase = if (purpose == Purpose.JUST_BORED) Phase.Bored else Phase.ChooseBox(purpose) },
                        modifier = Modifier.fillMaxWidth(), emoji = purpose.emoji, tone = PillTone.Lavender, alignStart = true,
                    )
                }
            }
            Phase.Bored -> {
                Text("That's honest. Any of these instead?", style = MaterialTheme.typography.titleSmall)
                if (instead != null) InsteadPanel(night, instead) else SoftCard(Modifier.fillMaxWidth(), tone = CardTone.Lavender) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InsteadDefaults.items.forEach { (cat, text) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(insteadEmoji(cat), fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                var ready by remember { mutableStateOf(false) }
                // Deliberately plain: staying should never look as inviting as leaving.
                if (!ready) Countdown(boredExtraDelay) { ready = true }
                else TextButton(onClick = { phase = Phase.ChooseBox(Purpose.JUST_BORED) }) { Text("Continue anyway") }
            }
            is Phase.ChooseBox -> {
                Text("For how long?", style = MaterialTheme.typography.titleSmall)
                TimeBoxes(prompt.timeBoxes) { onProceed(p.purpose, it) }
            }
        }
        PillButton("Never mind, close it", onClick = onNeverMind, modifier = Modifier.fillMaxWidth(), emoji = "🌿")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeBoxes(boxes: List<TimeBox>, onPick: (TimeBox) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        boxes.forEach { box -> PillButton(box.label, onClick = { onPick(box) }, emoji = "⏳", tone = PillTone.Accent) }
    }
}

/**
 * End of a time box (E3, A2): one tap to rate, then home. With F10 off there's no question, just
 * the stop. Leaving early never reaches this screen (D-021).
 */
@Composable
fun GateStop(appLabel: String, minutes: Long, askRating: Boolean = true, onRate: (Int) -> Unit) {
    GateSurface {
        Text("Time's up", style = MaterialTheme.typography.headlineSmall)
        if (!askRating) {
            Text("$minutes min of $appLabel.", textAlign = TextAlign.Center)
            PillButton("OK", onClick = { onRate(0) }, emoji = "🌿")
            return@GateSurface
        }
        Text("$minutes min of $appLabel. Was it worth it?", textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            EmojiCircleButton("👍", "Worth it", { onRate(1) }, PillTone.Mint)
            EmojiCircleButton("😐", "So-so", { onRate(0) }, PillTone.Lavender)
            EmojiCircleButton("👎", "Not really", { onRate(-1) }, PillTone.Accent)
        }
    }
}

/** Games: never cut a match off mid-game (D-006). Just a reminder. */
@Composable
fun GateGameReminder(appLabel: String, onOk: () -> Unit) {
    GateSurface {
        Text("Your matches are done", style = MaterialTheme.typography.headlineSmall)
        Text("Finish the one you're in, then close $appLabel.", textAlign = TextAlign.Center)
        PillButton("OK", onClick = onOk, emoji = "🎮")
    }
}

@Composable
fun GateBlocked(onOk: () -> Unit) {
    GateSurface {
        Text("Short videos are blocked", style = MaterialTheme.typography.headlineSmall)
        Text("Reels and Shorts are the endless part. The rest of the app still works.", textAlign = TextAlign.Center)
        PillButton("Got it", onClick = onOk, emoji = "🌿")
    }
}

@Composable
private fun Countdown(length: Duration, onDone: () -> Unit) {
    val total = length.seconds.toInt()
    var left by remember { mutableIntStateOf(total) }
    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1000)
            left--
        }
        onDone()
    }
    CountdownRing(left, total)
}

/** White page with the mascot on top. Scrolls, so long purpose lists still fit small screens. */
@Composable
internal fun GateSurface(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        // min height = the screen, so short content stays centered while long content scrolls.
        BoxWithConstraints(Modifier.safeDrawingPadding()) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(R.drawable.mascot), contentDescription = null, modifier = Modifier.size(112.dp))
                content()
            }
        }
    }
}
