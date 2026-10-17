package io.github.dangquanghuy277.mindfulhub.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.dangquanghuy277.mindfulhub.core.GateDecision
import io.github.dangquanghuy277.mindfulhub.core.InsteadDefaults
import io.github.dangquanghuy277.mindfulhub.core.Purpose
import io.github.dangquanghuy277.mindfulhub.core.TimeBox
import kotlinx.coroutines.delay
import java.time.Duration

private sealed interface Phase {
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
) {
    var phase by remember { mutableStateOf<Phase>(Phase.Countdown) }
    GateSurface {
        Text(appLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(line, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        when (val p = phase) {
            Phase.Countdown -> Countdown(prompt.delay) { phase = Phase.ChoosePurpose }
            Phase.ChoosePurpose -> prompt.purposes.forEach { purpose ->
                OutlinedButton(onClick = {
                    phase = if (purpose == Purpose.JUST_BORED) Phase.Bored else Phase.ChooseBox(purpose)
                }, modifier = Modifier.fillMaxWidth()) { Text(purpose.label) }
            }
            Phase.Bored -> {
                Text("That's honest. Any of these instead?", style = MaterialTheme.typography.titleSmall)
                InsteadDefaults.items.forEach { (cat, text) ->
                    Text("• $cat: $text", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth())
                }
                var ready by remember { mutableStateOf(false) }
                if (!ready) Countdown(boredExtraDelay) { ready = true }
                else TextButton(onClick = { phase = Phase.ChooseBox(Purpose.JUST_BORED) }) { Text("Continue anyway") }
            }
            is Phase.ChooseBox -> {
                Text("For how long?", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    prompt.timeBoxes.forEach { box -> OutlinedButton(onClick = { onProceed(p.purpose, box) }) { Text(box.label) } }
                }
            }
        }
        Button(onClick = onNeverMind, modifier = Modifier.fillMaxWidth()) { Text("Never mind, close it") }
    }
}

/** End of a time box (E3, A2): one tap to rate, then home. */
@Composable
fun GateStop(appLabel: String, minutes: Long, onRate: (Int) -> Unit) {
    GateSurface {
        Text("Time's up", style = MaterialTheme.typography.headlineSmall)
        Text("$minutes min of $appLabel. Was it worth it?", textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onRate(1) }) { Text("👍") }
            OutlinedButton(onClick = { onRate(0) }) { Text("😐") }
            OutlinedButton(onClick = { onRate(-1) }) { Text("👎") }
        }
    }
}

/** Games: never cut a match off mid-game (D-006). Just a reminder. */
@Composable
fun GateGameReminder(appLabel: String, onOk: () -> Unit) {
    GateSurface {
        Text("Your matches are done", style = MaterialTheme.typography.headlineSmall)
        Text("Finish the one you're in, then close $appLabel.", textAlign = TextAlign.Center)
        Button(onClick = onOk) { Text("OK") }
    }
}

@Composable
fun GateBlocked(onOk: () -> Unit) {
    GateSurface {
        Text("Short videos are blocked", style = MaterialTheme.typography.headlineSmall)
        Text("Reels and Shorts are the endless part. The rest of the app still works.", textAlign = TextAlign.Center)
        Button(onClick = onOk) { Text("OK") }
    }
}

@Composable
private fun Countdown(length: Duration, onDone: () -> Unit) {
    var left by remember { mutableIntStateOf(length.seconds.toInt()) }
    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1000)
            left--
        }
        onDone()
    }
    Text("$left", style = MaterialTheme.typography.displayMedium)
}

@Composable
private fun GateSurface(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}
