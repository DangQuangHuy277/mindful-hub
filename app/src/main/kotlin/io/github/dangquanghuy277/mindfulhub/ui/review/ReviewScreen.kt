package io.github.dangquanghuy277.mindfulhub.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.SetupKit
import io.github.dangquanghuy277.mindfulhub.core.WeekSummary
import io.github.dangquanghuy277.mindfulhub.core.Who5
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import io.github.dangquanghuy277.mindfulhub.ui.formatMinutes
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalConfiguration

/**
 * F11 + F12, then F27 + F60: the week in numbers (facts, never verdicts), WHO-5, one change, and
 * next week's plan with the setup kit. Each section shows only when its switch is on.
 */
@Composable
fun ReviewScreen(vm: ReviewViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val s = state.settings
    val date = rememberFormatter("EEE d MMM")
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Weekly review", style = MaterialTheme.typography.headlineMedium)
            if (state.weekStart.year > 1) {
                Text(
                    "${date.format(state.weekStart)} – ${date.format(state.weekStart.plusDays(6))}. About 10 minutes.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (s.on(Feature.REVIEW)) {
                state.summary?.let { WeekCard(it, s.on(Feature.WORTH_IT), s.on(Feature.FEED_BUDGET)) }
                Who5Card(state.who5, state.lastWho5, vm::setWho5)
                SoftCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Did you miss anything you cared about?", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(state.missed, vm::setMissed, Modifier.fillMaxWidth(), placeholder = { Text("A group post, a match, news…") })
                        Text("One change for next week", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(state.oneChange, vm::setChange, Modifier.fillMaxWidth(), placeholder = { Text("Just one. Small is fine.") })
                        PillButton(if (state.saved) "Saved ✓" else "Save this week's review", onClick = vm::save, emoji = "📝")
                    }
                }
            }
            if (s.on(Feature.WEEK_PLAN)) {
                PlanCard(state, vm)
                KitCard(state.kitChecked, vm::toggleKit)
            }
            if (!s.on(Feature.REVIEW) && !s.on(Feature.WEEK_PLAN)) {
                Text("The weekly review and the week plan are both switched off in Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WeekCard(w: WeekSummary, worthIt: Boolean, leakWatch: Boolean) {
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your week in numbers", style = MaterialTheme.typography.titleMedium)
            Fact("Unintended time", formatMinutes(w.unintendedMinutes), w.unintendedBefore?.let { "week before: ${formatMinutes(it)}" })
            Text(
                "Gated-app minutes outside a time box you opened with a purpose, plus boxes you rated 👎.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Fact("After bedtime", formatMinutes(w.afterBedtimeMinutes), null)
            if (worthIt) {
                val rated = w.worthIt + w.soSo + w.notWorthIt
                Fact("Worth it", "👍 ${w.worthIt} · 😐 ${w.soSo} · 👎 ${w.notWorthIt}", if (rated > 0) "${w.worthIt * 100 / rated}% worth it" else null)
                if (w.leftEarly > 0) Fact("Left on your own", "${w.leftEarly}× 🌿", "before the time box ended")
            }
            if (leakWatch) {
                Fact("Apps without a gate", formatMinutes(w.ungatedMinutes), w.ungatedMinutesBefore?.let { "week before: ${formatMinutes(it)}" })
                val before = w.ungatedMinutesBefore
                if (before != null && before > 30 && w.ungatedMinutes > before * 1.2) {
                    Text(
                        "Time in ungated apps went up. Something may be taking the feeds' place (Chrome, Threads?).",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text("Good things", style = MaterialTheme.typography.titleSmall)
            GoodBars(w.goodMinutes)
        }
    }
}

@Composable
private fun Fact(label: String, value: String, note: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.titleSmall)
            note?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

/** One bar per G5 category on a shared baseline: balance at a glance (P18), every category counts. */
@Composable
private fun GoodBars(minutes: Map<Category, Int>) {
    val max = (minutes.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val c = MaterialTheme.colorScheme
    Category.entries.forEach { cat ->
        val m = minutes[cat] ?: 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${cat.emoji} ${cat.label}", Modifier.width(96.dp), style = MaterialTheme.typography.bodySmall)
            Box(Modifier.weight(1f).height(12.dp)) {
                if (m > 0) {
                    Box(Modifier.fillMaxWidth(m / max.toFloat()).fillMaxHeight().background(c.tertiary, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
                }
            }
            Text(if (m > 0) formatMinutes(m.toDouble()) else "–", Modifier.width(64.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Who5Card(answers: List<Int?>, last: Int?, onAnswer: (Int, Int) -> Unit) {
    SoftCard(tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("WHO-5: over the last two weeks…", style = MaterialTheme.typography.titleMedium)
            Text("0 = at no time · 5 = all of the time", style = MaterialTheme.typography.bodySmall)
            Who5.items.forEachIndexed { i, item ->
                Text(item, style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..5).forEach { v -> FilterChip(selected = answers[i] == v, onClick = { onAnswer(i, v) }, label = { Text("$v") }) }
                }
            }
            val score = Who5.score(answers.map { it ?: -1 })
            if (score != null) {
                Text("Score: $score / 100" + (last?.let { " (last time $it)" } ?: ""), style = MaterialTheme.typography.titleSmall)
                if (score <= Who5.LOW) {
                    Text(
                        "A score this low is worth talking about with someone you trust, or a doctor. It's a check-in, not a diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanCard(state: ReviewUiState, vm: ReviewViewModel) {
    val date = rememberFormatter("d MMM")
    val locale = LocalConfiguration.current.locales[0]
    var day by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var hour by remember { mutableIntStateOf(state.suggestedHours.firstOrNull() ?: 20) }
    var category by remember { mutableStateOf(Category.MOVE) }
    var minutes by remember { mutableIntStateOf(30) }
    var title by remember { mutableStateOf("") }
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Plan the week", style = MaterialTheme.typography.titleMedium)
            Text(
                "Put 3–5 good things into the times you usually scroll. Treat them as appointments.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val thisWeek = state.weekStart
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.planWeek == thisWeek, onClick = { vm.choosePlanWeek(thisWeek) }, label = { Text("This week") })
                FilterChip(selected = state.planWeek == thisWeek.plusWeeks(1), onClick = { vm.choosePlanWeek(thisWeek.plusWeeks(1)) }, label = { Text("Next week") })
            }
            state.planItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.category.emoji} ${item.day.getDisplayName(TextStyle.SHORT, locale)} ${item.time} · ${item.title} · ${item.minutes} min" +
                            when (item.status) { "DONE" -> " ✓"; "SKIPPED" -> " (skipped)"; else -> "" },
                        Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = { vm.deletePlanItem(item) }) { Text("Remove") }
                }
            }
            Text("Add one", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DayOfWeek.entries.forEach { d ->
                    val label = d.getDisplayName(TextStyle.SHORT, locale) + " " + date.format(state.planWeek.plusDays(d.value - 1L))
                    FilterChip(selected = day == d, onClick = { day = d }, label = { Text(label) })
                }
            }
            if (state.suggestedHours.isNotEmpty()) {
                Text("Your usual scrolling hours last week:", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.suggestedHours.forEach { h -> FilterChip(selected = hour == h, onClick = { hour = h }, label = { Text("%02d:00".format(h)) }) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Time", Modifier.weight(1f))
                FilledTonalIconButton(onClick = { hour = (hour + 23) % 24 }) { Text("−") }
                Text("%02d:00".format(hour), Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleMedium)
                FilledTonalIconButton(onClick = { hour = (hour + 1) % 24 }) { Text("+") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Category.entries.forEach { cat -> FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text("${cat.emoji} ${cat.label}") }) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(10, 20, 30, 45, 60, 90).forEach { m -> FilterChip(selected = minutes == m, onClick = { minutes = m }, label = { Text("$m min") }) }
            }
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(defaultTitle(category)) })
            PillButton("Add to the plan", onClick = {
                vm.addPlanItem(day, LocalTime.of(hour, 0), category, minutes, title.trim().ifBlank { defaultTitle(category) })
                title = ""
            }, emoji = "🗓️", tone = PillTone.Accent)
        }
    }
}

private fun defaultTitle(c: Category): String = when (c) {
    Category.MOVE -> "Walk, phone in pocket"
    Category.PEOPLE -> "Call or meet someone"
    Category.READ -> "Read (Substack queue or an e-book)"
    Category.BUILD -> "lc-trainer session"
    Category.REST -> "Rest without the phone"
}

/** F60: set up the room for the plan, not the willpower (P1). */
@Composable
private fun KitCard(checked: Set<Int>, onToggle: (Int) -> Unit) {
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Set up the room (2 min)", style = MaterialTheme.typography.titleMedium)
            Text("Making the good thing the easy thing beats trying harder later.", style = MaterialTheme.typography.bodySmall)
            SetupKit.defaults.forEachIndexed { i, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = i in checked, onCheckedChange = { onToggle(i) })
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
