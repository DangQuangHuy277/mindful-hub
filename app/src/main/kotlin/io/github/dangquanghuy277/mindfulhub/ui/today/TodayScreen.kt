package io.github.dangquanghuy277.mindfulhub.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import io.github.dangquanghuy277.mindfulhub.core.CalmLinks
import io.github.dangquanghuy277.mindfulhub.core.Category
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.NightPhase
import io.github.dangquanghuy277.mindfulhub.core.NightStatus
import io.github.dangquanghuy277.mindfulhub.core.SleepLatency
import io.github.dangquanghuy277.mindfulhub.core.PlanItem
import io.github.dangquanghuy277.mindfulhub.core.SleepQuest
import io.github.dangquanghuy277.mindfulhub.core.WeekPlan
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import io.github.dangquanghuy277.mindfulhub.ui.BankShortcutCard
import io.github.dangquanghuy277.mindfulhub.ui.GateOffCard
import io.github.dangquanghuy277.mindfulhub.ui.GateOnPill
import io.github.dangquanghuy277.mindfulhub.ui.NotificationPermission
import io.github.dangquanghuy277.mindfulhub.ui.formatMinutes
import io.github.dangquanghuy277.mindfulhub.ui.reaction.ReactionActivity
import io.github.dangquanghuy277.mindfulhub.ui.greeting
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Home (D-021): what today asks of you, then the good things. Every card is one feature and only
 * appears when its switch is on, so turning a feature off removes its card and nothing else.
 * Copy rules (F32): suggest, state task facts, never a verdict about you; nothing resets.
 */
@Composable
fun TodayScreen(vm: TodayViewModel, onOpenReview: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val s = state.settings
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotificationPermission()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(greeting(LocalTime.now()), Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                if (state.gateEnabled) GateOnPill()
            }
            if (!state.gateEnabled) GateOffCard(onTurnedOn = vm::refresh)
            if (state.banksToPin.isNotEmpty()) BankShortcutCard(state.banksToPin)
            HelloCard(state.hello, state.plan.firstOrNull { it.status == null })
            state.postcard?.let { PostcardCard(it) }
            if (state.checkInNight != null) CheckInCard(state.checkInPlannedLate, vm::answerCheckIn, vm::skipCheckIn)
            if (state.reactionDue) ReactionCard()
            state.receipt?.let { ReceiptCard(it) }
            state.nightCard?.let { NightLogCard(it) }
            state.quest?.let { QuestCard(it, state.night?.phase, vm::startQuest, vm::holdChapter) }
            if (state.reviewDue) ReviewDueCard(onOpenReview)
            state.feedUsed?.let { FeedsCard(it, s.feedBudgetMinutes) }
            if (s.on(Feature.WEEK_PLAN)) PlanTodayCard(state.plan, state.hasPlanThisWeek, vm::markPlan, onOpenReview)
            if (s.on(Feature.GOOD_LOG)) GoodLogCard(state, vm::logGood, vm::deleteGood)
            if (s.on(Feature.CALM_LINKS)) CalmLinksCard()
        }
    }
}

@Composable
private fun HelloCard(hello: Hello, next: PlanItem?) {
    if (hello == Hello.NONE) return
    val step = next?.let { "Next on your plan: ${it.time} ${it.title}." } ?: "One next step: a 10-minute walk before noon."
    SoftCard(tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (hello) {
                Hello.WELCOME_BACK -> {
                    Text("Welcome back 🌱", style = MaterialTheme.typography.titleMedium)
                    Text("Yesterday ran longer than usual. That happens; today is a new day. $step")
                }
                Hello.FRESH_START -> {
                    Text("New week, fresh start ✨", style = MaterialTheme.typography.titleMedium)
                    Text("Mondays are a good day to begin things. $step")
                }
                Hello.NONE -> Unit
            }
        }
    }
}

@Composable
private fun PostcardCard(text: String) {
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("💌 From last night's you", style = MaterialTheme.typography.titleMedium)
            Text("“$text”", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ReceiptCard(r: Receipt) {
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🧾 Last night · trial", style = MaterialTheme.typography.titleMedium)
            Text("${formatMinutes(r.lastNight)} on gated apps after ${r.bedtime}.")
            Text(
                "Your best of the ${r.nights} nights before: ${formatMinutes(r.best)}.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** F65: two taps, before any sleep numbers. Saves as soon as both are answered. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckInCard(plannedLate: Boolean, onAnswer: (Int, SleepLatency, Boolean) -> Unit, onSkip: (Boolean) -> Unit) {
    var rested by remember { mutableStateOf<Int?>(null) }
    var latency by remember { mutableStateOf<SleepLatency?>(null) }
    var late by remember(plannedLate) { mutableStateOf(plannedLate) }
    fun maybeSave() {
        val r = rested ?: return
        val l = latency ?: return
        onAnswer(r, l, late)
    }
    SoftCard(tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("☀️ Good morning: two quick taps", style = MaterialTheme.typography.titleMedium)
            FilterChip(selected = late, onClick = { late = !late }, label = { Text("⚽ Planned late night (a match)") })
            Text("How rested do you feel?", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { n ->
                    val label = when (n) { 1 -> "1 not at all"; 5 -> "5 very"; else -> "$n" }
                    FilterChip(selected = rested == n, onClick = { rested = n; maybeSave() }, label = { Text(label) })
                }
            }
            Text("How long did falling asleep take?", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SleepLatency.entries.forEach { l ->
                    FilterChip(selected = latency == l, onClick = { latency = l; maybeSave() }, label = { Text(l.label) })
                }
            }
            TextButton(onClick = { onSkip(late) }) { Text("Skip today") }
        }
    }
}

/** F75 (trial): the morning reaction check opens full screen; the card disappears once it's done. */
@Composable
private fun ReactionCard() {
    val context = LocalContext.current
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⭐ Morning reaction check · trial", style = MaterialTheme.typography.titleMedium)
            Text("3 minutes: tap as soon as the star appears. It shows what sleep debt does even when you feel fine.")
            PillButton("Start", onClick = { context.startActivity(Intent(context, ReactionActivity::class.java)) }, emoji = "⭐", tone = PillTone.Accent)
        }
    }
}

/** F64: facts about the phone, labelled as such. No score and no verdict (F32, P44). */
@Composable
private fun NightLogCard(n: NightCard) {
    val time = rememberFormatter("HH:mm")
    fun hm(i: Instant) = time.format(i.atZone(ZoneId.systemDefault()))
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🌙 Last night, as the phone saw it", style = MaterialTheme.typography.titleMedium)
            n.match?.let { m ->
                Text("⚽ Planned match night: kick-off ${m.start}, ${m.minutes} min.", color = muted)
            }
            when (n.status) {
                NightStatus.NO_DATA -> Text("No phone data for last night: the phone was off, or the usage import didn't run.")
                NightStatus.NO_CLEAR_NIGHT -> Text("No phone-down stretch of 3 hours or more last night.")
                NightStatus.FOUND -> {
                    val down = n.down
                    val up = n.up
                    if (down != null && up != null) {
                        Text("Down ${hm(down)} → up ${hm(up)} · ${formatMinutes(Duration.between(down, up).toMinutes().toDouble())}", style = MaterialTheme.typography.titleSmall)
                        val late = Duration.between(n.plannedBed, down).toMinutes()
                        Text(
                            when {
                                late > 0 -> "Phone down ${formatMinutes(late.toDouble())} after the planned ${hm(n.plannedBed)} bedtime."
                                late < 0 -> "Phone down ${formatMinutes(-late.toDouble())} before the planned ${hm(n.plannedBed)} bedtime."
                                else -> "Phone down right at the planned ${hm(n.plannedBed)} bedtime."
                            },
                        )
                    } else if (up != null) {
                        Text("Up at ${hm(up)}. The history doesn't reach back to when it went down.")
                    }
                    Text(
                        if (n.interruptions == 0) {
                            "No checks in between."
                        } else {
                            "You woke the phone ${n.interruptions}× (${formatMinutes(n.interruptionMinutes)})" +
                                if (n.interruptionApps.isNotEmpty()) ": ${n.interruptionApps.joinToString()}." else "."
                        },
                    )
                    n.split.forEach { (a, b) -> Text("Also down ${hm(a)} → ${hm(b)}.") }
                    n.firstApp?.let { Text("First app this morning: $it.", color = muted) }
                    if (n.shutdown) Text("The phone was off for part of the night, so this may read longer than it was.", color = muted)
                }
            }
            Text(
                "From phone use, not a sleep sensor: lying awake with the phone down looks the same.",
                style = MaterialTheme.typography.bodySmall, color = muted,
            )
        }
    }
}

@Composable
private fun QuestCard(q: QuestState, phase: NightPhase?, onStart: () -> Unit, onHold: () -> Unit) {
    SoftCard(tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🌙 Sleep-shift quest · trial", style = MaterialTheme.typography.titleMedium)
            if (!q.started) {
                Text(
                    "Seven short chapters. Each moves your wake time 15 min earlier for 3 days, and bedtime follows: " +
                        "from ${SleepQuest.START.bedtime} → ${SleepQuest.START.wake} to ${SleepQuest.times(SleepQuest.CHAPTERS).bedtime} → " +
                        "${SleepQuest.times(SleepQuest.CHAPTERS).wake}. Same times every day, weekends too.",
                )
                PillButton("Start chapter 1 tonight", onClick = onStart, emoji = "🌱", tone = PillTone.Lavender)
                return@Column
            }
            Text("Chapter ${q.chapter} of ${SleepQuest.CHAPTERS}: bed ${q.times.bedtime}, wake ${q.times.wake}", style = MaterialTheme.typography.titleSmall)
            LinearProgressIndicator(progress = { q.chapter / SleepQuest.CHAPTERS.toFloat() }, Modifier.fillMaxWidth())
            if (q.chapter < SleepQuest.CHAPTERS) {
                val left = if (q.daysLeft == 1) "Last day" else "${q.daysLeft} days left"
                Text("$left in this chapter. Get outside light within 30 min of waking.", style = MaterialTheme.typography.bodyMedium)
                if (phase == NightPhase.DAY) {
                    TextButton(onClick = onHold) { Text("This step didn't stick: stay on this chapter 3 more days") }
                }
            } else {
                Text("You're at the target. Keep the same times every day. 🌅")
            }
        }
    }
}

@Composable
private fun ReviewDueCard(onOpen: () -> Unit) {
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📝 It's Sunday", style = MaterialTheme.typography.titleMedium)
            Text("About 10 minutes: your week in numbers, five quick questions, and one change for next week.")
            PillButton("Open the weekly review", onClick = onOpen, emoji = "📝", tone = PillTone.Accent)
        }
    }
}

@Composable
private fun FeedsCard(used: Double, budget: Int) {
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Feeds today", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { (used / budget).toFloat().coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
            val left = (budget - used).coerceAtLeast(0.0)
            Text(
                "${formatMinutes(used)} of $budget min across Facebook, Instagram and Threads. " +
                    if (left >= 1) "${formatMinutes(left)} left." else "Done for today; it refills at 04:00.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PlanTodayCard(items: List<PlanItem>, hasPlan: Boolean, onMark: (PlanItem, String?) -> Unit, onPlan: () -> Unit) {
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Today's plan", style = MaterialTheme.typography.titleMedium)
            if (!hasPlan) {
                Text("No plan for this week yet. Put 3–5 good things into the times you usually scroll.")
                PillButton("Plan this week", onClick = onPlan, emoji = "🗓️", tone = PillTone.Accent)
                return@Column
            }
            if (items.isEmpty()) Text("Nothing planned for today. The rest of the week is in the review tab.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.category.emoji} ${item.time} ${item.title}", Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    when (item.status) {
                        WeekPlan.DONE -> TextButton(onClick = { onMark(item, null) }) { Text("Done ✓") }
                        WeekPlan.SKIPPED -> TextButton(onClick = { onMark(item, null) }) { Text("Skipped") }
                        else -> {
                            TextButton(onClick = { onMark(item, WeekPlan.SKIPPED) }) { Text("Skip") }
                            TextButton(onClick = { onMark(item, WeekPlan.DONE) }) { Text("Done") }
                        }
                    }
                }
            }
        }
    }
}

/** F17 (trial): calm, finite places for the real reasons you open the feeds. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalmLinksCard() {
    val context = LocalContext.current
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Calm links · trial", style = MaterialTheme.typography.titleMedium)
            Text(
                "News and reading without a feed. Your Facebook groups and X Lists can go here once you add their links.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CalmLinks.defaults.forEach { link ->
                    PillButton(link.title, onClick = {
                        val app = link.pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
                        runCatching { context.startActivity(app ?: Intent(Intent.ACTION_VIEW, link.url.toUri())) }
                    }, emoji = link.emoji, tone = PillTone.Accent)
                }
            }
        }
    }
}

/** A5: one tap per category, a time, done. Mistakes can be deleted; nothing is ever "lost" (P33). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoodLogCard(state: TodayUiState, onLog: (Category, Int, String) -> Unit, onDelete: (Long) -> Unit) {
    var picked by remember { mutableStateOf<Category?>(null) }
    var minutes by remember { mutableIntStateOf(10) }
    var note by remember { mutableStateOf("") }
    val time = rememberFormatter("HH:mm")
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Log a good thing", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { cat ->
                    PillButton(cat.label, onClick = { picked = if (picked == cat) null else cat }, emoji = cat.emoji, tone = if (picked == cat) PillTone.Primary else PillTone.Mint)
                }
            }
            picked?.let { cat ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 30, 60, 90).forEach { m -> FilterChip(selected = minutes == m, onClick = { minutes = m }, label = { Text("$m min") }) }
                }
                OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), placeholder = { Text("What was it? (optional)") }, singleLine = true)
                PillButton("Log ${cat.label.lowercase()}, $minutes min", onClick = {
                    onLog(cat, minutes, note.trim())
                    picked = null
                    note = ""
                }, emoji = cat.emoji)
            }
            if (state.good.isNotEmpty()) {
                Text("Today", style = MaterialTheme.typography.titleSmall)
                state.good.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${g.category.emoji} ${time.format(g.at.atZone(ZoneId.systemDefault()))} · ${g.minutes} min" + if (g.note.isNotBlank()) " · ${g.note}" else "",
                            Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { onDelete(g.id) }) { Text("Remove") }
                    }
                }
            }
            val week = state.weekGood.filterValues { it > 0 }
            if (week.isNotEmpty()) {
                Text(
                    "This week: " + week.entries.joinToString("  ") { (cat, m) -> "${cat.emoji} ${formatMinutes(m.toDouble())}" },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
