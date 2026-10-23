package io.github.dangquanghuy277.mindfulhub.ui.today

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dangquanghuy277.mindfulhub.core.NightStatus
import io.github.dangquanghuy277.mindfulhub.core.SleepQuest
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import io.github.dangquanghuy277.mindfulhub.ui.formatMinutes
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * F64 + F61 (+ F13/F44 while the quest runs): last night in one card (D-031, D-032). Collapsed it's
 * a glance: two bars (plan, phone down), one line of facts, and chips for the quest and night feeds.
 * A tap opens it in place: hours, the down / up / checks tiles, and tonight's plan with "stay on this
 * chapter", decided next to the facts. Each switch still controls its own part. Facts only, no
 * score, "down" and never "slept" (sleep/RESEARCH.md §6, P42, P44).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LastNightCard(n: NightCard?, receipt: Receipt?, quest: QuestState?, canHold: Boolean, onHold: () -> Unit) {
    val time = rememberFormatter("HH:mm")
    fun hm(i: Instant) = time.format(i.atZone(ZoneId.systemDefault()))
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val down = n?.down
    val up = n?.up
    val found = n?.status == NightStatus.FOUND && down != null && up != null
    var open by rememberSaveable(n?.down, n?.up) { mutableStateOf(false) }
    SoftCard {
        Column(
            Modifier.clickable(onClickLabel = if (open) "Show less" else "Show details") { open = !open }
                .animateContentSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌙 Last night", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (found) Text("${formatMinutes(Duration.between(down, up).toMinutes().toDouble())} down", color = muted, style = MaterialTheme.typography.labelLarge)
                Text(if (open) "  ▴" else "  ▾", color = muted)
            }
            when {
                n == null -> receipt?.let { Text("Feeds in the night: ${formatMinutes(it.lastNight)} · your best of ${it.nights} before: ${formatMinutes(it.best)}") }
                n.status == NightStatus.NO_DATA -> Text("No phone data for last night.")
                n.status == NightStatus.NO_CLEAR_NIGHT -> Text("No phone-down stretch of 3 hours or more.")
                !found -> Text("The phone hasn't come back up yet.")
                else -> {
                    NightTimeline(n, ::hm, detailed = open)
                    Text(glanceLine(n, ::hm), style = MaterialTheme.typography.bodyMedium)
                }
            }
            val chips = buildList {
                quest?.let { add("Chapter ${it.chapter}/${SleepQuest.CHAPTERS} · tonight ${it.times.bedtime}" + if (it.movedTonight) " (new)" else "") }
                if (n != null) receipt?.let { add("Night feeds ${formatMinutes(it.lastNight)}") }
                n?.match?.let { add("⚽ match night") }
            }
            if (chips.isNotEmpty() && !open) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { chips.forEach { Chip(it) } }
            }
            if (open) Details(n, receipt, quest, canHold, onHold, found, ::hm)
        }
    }
}

/** "02:25 → 09:54 · 40 min after plan · no checks": the one line the collapsed card keeps. */
private fun glanceLine(n: NightCard, hm: (Instant) -> String): String {
    val down = n.down ?: return ""
    val up = n.up ?: return ""
    val late = Duration.between(n.plannedBed, down).toMinutes()
    val vsPlan = when {
        late > 0 -> "${formatMinutes(late.toDouble())} after plan"
        late < 0 -> "${formatMinutes(-late.toDouble())} before plan"
        else -> "on plan"
    }
    val checks = when (n.interruptions) {
        0 -> "no checks"
        1 -> "1 check"
        else -> "${n.interruptions} checks"
    }
    return "${hm(down)} → ${hm(up)} · $vsPlan · $checks"
}

@Composable
private fun Details(n: NightCard?, receipt: Receipt?, quest: QuestState?, canHold: Boolean, onHold: () -> Unit, found: Boolean, hm: (Instant) -> String) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val down = n?.down
    val up = n?.up
    if (n != null && found && down != null && up != null) {
        n.match?.let { m -> Text("⚽ Planned match night: kick-off ${m.start}, ${m.minutes} min.", color = muted) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tile("Down", hm(down), vsPlan(Duration.between(n.plannedBed, down)), Modifier.weight(1f))
            Tile("Up", hm(up), vsPlan(Duration.between(n.plannedWake, up)), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val apps = if (n.interruptions == 0) "none" else n.interruptionApps.distinct().joinToString().ifBlank { formatMinutes(n.interruptionMinutes) }
            Tile("Checks", "${n.interruptions}", apps, Modifier.weight(1f))
            if (receipt != null) {
                Tile("Night feeds · trial", formatMinutes(receipt.lastNight), "best of ${receipt.nights}: ${formatMinutes(receipt.best)}", Modifier.weight(1f))
            } else {
                Tile("First app", n.firstApp ?: "—", "this morning", Modifier.weight(1f))
            }
        }
        n.split.forEach { (a, b) -> Text("Also down ${hm(a)} → ${hm(b)}.", style = MaterialTheme.typography.bodyMedium) }
        if (n.shutdown) Text("The phone was off for part of the night, so this may read longer than it was.", color = muted, style = MaterialTheme.typography.bodySmall)
    }
    quest?.let { QuestStrip(it, canHold, onHold) }
    Text(
        if (n != null) "From phone use, not a sleep sensor: lying awake with the phone down looks the same." else "Feeds and YouTube from bedtime until the phone came up.",
        style = MaterialTheme.typography.bodySmall, color = muted,
    )
}

/** Tonight's plan from the sleep-shift quest. Holding is a choice, never a failure (P33). */
@Composable
private fun QuestStrip(q: QuestState, canHold: Boolean, onHold: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().background(c.secondaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌱 Sleep shift · chapter ${q.chapter} of ${SleepQuest.CHAPTERS}", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = c.onSecondaryContainer)
            Text(if (q.daysLeft == 1) "last day" else "${q.daysLeft} days left", style = MaterialTheme.typography.labelMedium, color = c.onSecondaryContainer)
        }
        LinearProgressIndicator(progress = { q.chapter / SleepQuest.CHAPTERS.toFloat() }, Modifier.fillMaxWidth())
        Text("Tonight: bed ${q.times.bedtime}, wake ${q.times.wake}.", style = MaterialTheme.typography.titleSmall, color = c.onSecondaryContainer)
        val tip = if (q.movedTonight) "New chapter: 15 min earlier than last night." else "Get outside light within 30 min of waking."
        Text(tip, style = MaterialTheme.typography.bodySmall, color = c.onSecondaryContainer)
        if (canHold && q.chapter < SleepQuest.CHAPTERS) {
            TextButton(onClick = onHold) { Text("Last step didn't stick? Stay on this chapter 3 more days") }
        }
    }
}

/** "+25 min" late, "−10 min" early, against the plan. A fact, not a verdict (F32). */
private fun vsPlan(d: Duration): String {
    val m = d.toMinutes()
    return when {
        m > 0 -> "+${formatMinutes(m.toDouble())} vs plan"
        m < 0 -> "−${formatMinutes(-m.toDouble())} vs plan"
        else -> "on plan"
    }
}

@Composable
private fun Tile(label: String, value: String, note: String, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Column(modifier.background(c.surfaceContainer, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant, maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Text(note, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun Chip(text: String) {
    val c = MaterialTheme.colorScheme
    Text(
        text, Modifier.background(c.secondaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium, color = c.onSecondaryContainer,
    )
}

/**
 * Two thin bars on one clock: the planned night (light lavender) and the phone down (lavender), a
 * blue tick for each check, lighter bars for the other stretches of a split night. [detailed] adds
 * the hours and a legend.
 */
@Composable
private fun NightTimeline(n: NightCard, hm: (Instant) -> String, detailed: Boolean) {
    val down = n.down ?: return
    val up = n.up ?: return
    val c = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, color = c.onSurfaceVariant)
    val starts = listOf(n.plannedBed, down) + n.split.map { it.first }
    val ends = listOf(n.plannedWake, up) + n.split.map { it.second }
    val from = starts.min().minus(Duration.ofMinutes(30)).truncatedTo(ChronoUnit.HOURS)
    val to = ends.max().plus(Duration.ofMinutes(89)).truncatedTo(ChronoUnit.HOURS)
    val span = Duration.between(from, to).toMinutes().toFloat()
    val step = if (span > 9 * 60) 3L else 2L
    val description = "Planned ${hm(n.plannedBed)} to ${hm(n.plannedWake)}. Phone down ${hm(down)} to ${hm(up)}, ${n.interruptions} checks."
    Column {
        Canvas(Modifier.fillMaxWidth().height(if (detailed) 44.dp else 24.dp).semantics { contentDescription = description }) {
            fun x(i: Instant) = size.width * (Duration.between(from, i).toMinutes() / span)
            val bar = 9.dp.toPx()
            val gap = 5.dp.toPx()
            val r = CornerRadius(bar / 2)
            fun band(a: Instant, b: Instant, top: Float, color: Color) =
                drawRoundRect(color, Offset(x(a), top), Size((x(b) - x(a)).coerceAtLeast(bar), bar), r)
            val row2 = bar + gap
            drawRoundRect(c.surfaceContainerHighest, Offset(0f, 0f), Size(size.width, bar), r)
            drawRoundRect(c.surfaceContainerHighest, Offset(0f, row2), Size(size.width, bar), r)
            band(n.plannedBed, n.plannedWake, 0f, c.secondaryContainer.darken())
            n.split.forEach { (a, b) -> band(a, b, row2, c.secondary.copy(alpha = 0.45f)) }
            band(down, up, row2, c.secondary)
            n.checks.forEach { (a, _) ->
                drawRoundRect(c.primary, Offset(x(a) - 1.5.dp.toPx(), row2 - 2.dp.toPx()), Size(3.dp.toPx(), bar + 4.dp.toPx()), CornerRadius(1.5.dp.toPx()))
            }
            if (detailed) {
                var t = from
                while (!t.isAfter(to)) {
                    val label = measurer.measure(hm(t), labelStyle)
                    val lx = (x(t) - label.size.width / 2f).coerceIn(0f, size.width - label.size.width)
                    drawText(label, topLeft = Offset(lx, size.height - label.size.height))
                    t = t.plus(Duration.ofHours(step))
                }
            }
        }
        if (detailed) {
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(c.secondaryContainer.darken(), "Planned")
                Legend(c.secondary, "Phone down")
                if (n.checks.isNotEmpty()) Legend(c.primary, "Check", tick = true)
            }
        }
    }
}

/** The theme's lavender container is too pale to read as a bar on white. */
private fun Color.darken(): Color = copy(red = red * 0.88f, green = green * 0.88f, blue = blue * 0.95f)

@Composable
private fun Legend(color: Color, text: String, tick: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = if (tick) 3.dp else 10.dp, height = 10.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(5.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
