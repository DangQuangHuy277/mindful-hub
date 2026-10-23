package io.github.dangquanghuy277.mindfulhub.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.dangquanghuy277.mindfulhub.core.SleepQuest
import io.github.dangquanghuy277.mindfulhub.core.DayPart
import io.github.dangquanghuy277.mindfulhub.core.GateDecision
import io.github.dangquanghuy277.mindfulhub.core.InsteadContext
import io.github.dangquanghuy277.mindfulhub.core.InsteadItem
import io.github.dangquanghuy277.mindfulhub.core.InsteadMenu
import io.github.dangquanghuy277.mindfulhub.core.LockReason
import io.github.dangquanghuy277.mindfulhub.core.Moment
import io.github.dangquanghuy277.mindfulhub.core.NightPhase
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard

// Screens for the D-021 features. Each is used only when its switch is on, so the gate core
// screens in GateScreens.kt stay as they were.

/** What the "just bored" list needs from the gate (D-025): the moment, and where shows and picks go. */
class InsteadHooks(
    val ctx: InsteadContext,
    val onShown: (Moment, List<InsteadItem>) -> Unit,
    val onPick: (Moment, InsteadItem) -> Unit,
)

/**
 * F26 + F59: things to *start*, matched to the time you have. Tapping one closes the app and does
 * it (a timer, the dialer, Substack) and logs it as a good thing. At night it offers only things
 * that aren't the lit screen (D-021). D-025: at work the only chip is 1–5 min, and each list is
 * reported once per chip, so what was offered can be counted against what was picked.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InsteadPanel(hooks: InsteadHooks) {
    val ctx = hooks.ctx
    val moments = InsteadMenu.momentsFor(ctx.part)
    var moment by remember { mutableStateOf(InsteadMenu.defaultMoment(ctx.part)) }
    val reported = remember { mutableSetOf<Moment>() }
    val items = remember(moment) { InsteadMenu.forMoment(moment, ctx) }
    LaunchedEffect(moment) { if (reported.add(moment)) hooks.onShown(moment, items) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (ctx.night == NightPhase.DAY) {
            Text(if (ctx.part == DayPart.WORK) "At work: something quick" else "How much time do you have?", style = MaterialTheme.typography.titleSmall)
            if (moments.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    moments.forEach { m -> FilterChip(selected = m == moment, onClick = { moment = m }, label = { Text(m.label) }) }
                }
            }
        }
        items.forEach { item ->
            PillButton(
                item.text, onClick = { hooks.onPick(moment, item) }, modifier = Modifier.fillMaxWidth(),
                emoji = item.category.emoji, tone = PillTone.Mint, alignStart = true,
            )
        }
    }
}

/**
 * F31 + F53: one line to tomorrow-you, a specific and positive picture (Rösch 2022: g = .44).
 * Suggestions mean typing is never required at 1 am.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PostcardBox(wakeText: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    SoftCard(Modifier.fillMaxWidth(), tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("💌 Postcard to tomorrow-you", style = MaterialTheme.typography.titleMedium)
            if (saved) {
                Text("Saved. It'll be on your morning screen. 🌅")
                return@Column
            }
            Text("Alarm at $wakeText. What's the first good thing you'll do tomorrow?")
            OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), placeholder = { Text("Coffee by the window, then a walk") })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Coffee and morning light", "A walk before the phone", "A good breakfast", "Message someone I like").forEach { s ->
                    AssistChip(onClick = { text = s }, label = { Text(s) })
                }
            }
            PillButton("Save for the morning", onClick = { if (text.isNotBlank()) { onSave(text.trim()); saved = true } }, emoji = "🌙", tone = PillTone.Lavender)
        }
    }
}

/**
 * F03 wind-down and F29 budget: the app is resting, said plainly, with what's possible instead.
 * Closing is the big button; the one bed scroll (if allowed tonight) is deliberately small.
 */
@Composable
fun GateLocked(
    appLabel: String,
    decision: GateDecision.Locked,
    bedtimeText: String,
    wakeText: String,
    /** F44 · lock (trial, D-032): the sleep-shift chapter these times come from; null hides the line. */
    questChapter: Int?,
    budgetMinutes: Int,
    /** F26 + F59 is on: things to start instead. */
    instead: InsteadHooks?,
    onPostcard: (String) -> Unit,
    onBedScroll: () -> Unit,
    onClose: () -> Unit,
) {
    GateSurface {
        when (decision.reason) {
            LockReason.WIND_DOWN -> {
                Text("Wind-down time 🌙", style = MaterialTheme.typography.headlineSmall)
                Text("Bed at $bedtimeText, alarm at $wakeText. $appLabel rests until then. Messages still work.", textAlign = TextAlign.Center)
                questChapter?.let { c ->
                    Text(
                        if (c < SleepQuest.CHAPTERS) {
                            "🌱 Sleep shift, chapter $c of ${SleepQuest.CHAPTERS}: these times move 15 min earlier every 3 days."
                        } else {
                            "🌅 Sleep shift: you're at the target. Same times every day."
                        },
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                    )
                }
            }
            LockReason.BUDGET -> {
                Text("Today's feeds are done 🌿", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "You've used the $budgetMinutes minutes you set for Facebook, Instagram and Threads. It refills at 04:00.",
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (decision.postcardDue) PostcardBox(wakeText, onPostcard)
        if (instead != null) {
            Text("Something to start instead?", style = MaterialTheme.typography.titleSmall)
            InsteadPanel(instead)
        }
        PillButton("Close $appLabel", onClick = onClose, modifier = Modifier.fillMaxWidth(), emoji = "🌿")
        if (decision.bedScroll) {
            TextButton(onClick = onBedScroll) { Text("One 10-minute scroll (once a night)") }
        }
    }
}
