package io.github.dangquanghuy277.mindfulhub.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.MatchNight
import io.github.dangquanghuy277.mindfulhub.core.MatchNights
import io.github.dangquanghuy277.mindfulhub.core.FeatureStatus
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.SettingsCodec
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class SettingsUiState(
    val today: LocalDate = LocalDate.MIN,
    /**
     * What you've chosen, including changes that start tomorrow. The screen shows and steps from
     * this, so pressing + twice really adds 30 min, and switching back cancels a waiting change.
     */
    val settings: Settings = Settings(),
    /** key to the logical day a waiting (loosening) change starts. */
    val pending: Map<String, Pair<String, LocalDate>> = emptyMap(),
    /** A6: what the last export did, shown under its button. */
    val exportNote: String? = null,
)

class SettingsViewModel(private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val today = c.clock.dayOf(Instant.now())
            _state.value = SettingsUiState(today, c.settingsStore.on(today.plusDays(1)), c.settingsStore.pending(today), _state.value.exportNote)
        }
    }

    fun export() {
        _state.value = _state.value.copy(exportNote = "Exporting…")
        viewModelScope.launch {
            val note = runCatching { c.exportData() }
                .fold({ "Saved to Downloads/$it" }, { "Export failed: ${it.message}" })
            _state.value = _state.value.copy(exportNote = note)
        }
    }

    fun set(key: String, value: String) {
        viewModelScope.launch {
            c.settingsStore.set(key, value, c.clock.dayOf(Instant.now()))
            refresh()
        }
    }
}

/**
 * D-021: every new feature has a switch here. Approved and trial features are listed apart, and
 * switching off a restriction says plainly that it waits until tomorrow (P2).
 */
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }
    val s = state.settings
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Changes that loosen a rule start tomorrow at 04:00. Tightening, and anything that isn't a rule, starts now.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FeatureGroup("Approved features", FeatureStatus.APPROVED, state, vm)
            FeatureGroup("Trying out", FeatureStatus.TRIAL, state, vm)

            Text("Your rules", style = MaterialTheme.typography.titleLarge)
            SoftCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Stepper(
                        "Feeds budget per day", "${s.feedBudgetMinutes} min", pendingText(state, SettingsCodec.FEED_BUDGET, " min"),
                        onMinus = { vm.set(SettingsCodec.FEED_BUDGET, (s.feedBudgetMinutes - 15).coerceAtLeast(15).toString()) },
                        onPlus = { vm.set(SettingsCodec.FEED_BUDGET, (s.feedBudgetMinutes + 15).coerceAtMost(240).toString()) },
                    )
                    if (s.on(Feature.SLEEP_QUEST) && s.sleepQuestStart != null) {
                        val t = s.sleepTimes(state.today)
                        Text("Bed ${t.bedtime}, wake ${t.wake}: set by the sleep-shift quest.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Stepper(
                            "Bedtime", s.bedtime.toString(), pendingText(state, SettingsCodec.BEDTIME),
                            onMinus = { vm.set(SettingsCodec.BEDTIME, s.bedtime.minusMinutes(15).toString()) },
                            onPlus = { vm.set(SettingsCodec.BEDTIME, s.bedtime.plusMinutes(15).toString()) },
                        )
                        Stepper(
                            "Wake time", s.wakeTime.toString(), pendingText(state, SettingsCodec.WAKE),
                            onMinus = { vm.set(SettingsCodec.WAKE, s.wakeTime.minusMinutes(15).toString()) },
                            onPlus = { vm.set(SettingsCodec.WAKE, s.wakeTime.plusMinutes(15).toString()) },
                        )
                    }
                    Stepper(
                        "Wind-down starts before bed", "${s.windDownMinutes} min", pendingText(state, SettingsCodec.WIND_DOWN, " min"),
                        onMinus = { vm.set(SettingsCodec.WIND_DOWN, (s.windDownMinutes - 15).coerceAtLeast(0).toString()) },
                        onPlus = { vm.set(SettingsCodec.WIND_DOWN, (s.windDownMinutes + 15).coerceAtMost(120).toString()) },
                    )
                    SwitchRow(
                        "One 10-minute scroll in bed",
                        "Once a night during wind-down. For the nights scrolling helps you fall asleep, until we've checked the research.",
                        s.allowBedScroll, pendingText(state, SettingsCodec.BED_SCROLL),
                    ) { vm.set(SettingsCodec.BED_SCROLL, it.toString()) }
                }
            }

            if (s.on(Feature.MATCH_NIGHT)) MatchNightsCard(state, vm)

            Text("Your week", style = MaterialTheme.typography.titleLarge)
            SoftCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Monday to Friday. The \"just bored\" list offers only quick things at work, up to half an hour at lunch, and no push-ups for an hour after a meal.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val w = s.schedule
                    TimeStepper("Work starts", w.workStart, SettingsCodec.WORK_START, state, vm)
                    TimeStepper("Work ends", w.workEnd, SettingsCodec.WORK_END, state, vm)
                    TimeStepper("Lunch starts", w.lunchStart, SettingsCodec.LUNCH_START, state, vm)
                    TimeStepper("Lunch ends", w.lunchEnd, SettingsCodec.LUNCH_END, state, vm)
                    TimeStepper("Dinner starts", w.dinnerStart, SettingsCodec.DINNER_START, state, vm)
                    TimeStepper("Dinner ends", w.dinnerEnd, SettingsCodec.DINNER_END, state, vm)
                }
            }

            Text("Your data", style = MaterialTheme.typography.titleLarge)
            SoftCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Everything the app has recorded, as one SQLite file in Downloads. It stays there if the app is uninstalled.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilledTonalButton(onClick = vm::export) { Text("Export all data") }
                    state.exportNote?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

/**
 * F72: plan a late match at least a day ahead (it loosens the wind-down lock, so it starts tomorrow
 * at 04:00 like any other loosening). Only nights from tomorrow are offered, so nothing looks
 * planned for tonight that won't apply.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchNightsCard(state: SettingsUiState, vm: SettingsViewModel) {
    val upcoming = state.settings.matchNights.filter { !it.night.isBefore(state.today) }
    val nights = (1..7).map { state.today.plusDays(it.toLong()) }
    var night by remember(state.today) { mutableStateOf(nights.first()) }
    var kickoff by remember { mutableStateOf(LocalTime.of(2, 0)) }
    var minutes by remember { mutableIntStateOf(120) }
    val day = rememberFormatter("EEE d MMM")
    fun save(list: List<MatchNight>) = vm.set(SettingsCodec.MATCH_NIGHTS, MatchNights.encode(list.filter { !it.night.isBefore(state.today) }))

    Text("Match nights", style = MaterialTheme.typography.titleLarge)
    SoftCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Plan a late match at least a day ahead. During it, and 15 min either side, YouTube opens through the gate; " +
                    "feeds, X and games stay locked. Keep the same wake time the next morning: it holds the sleep shift in place.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            upcoming.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚽ Night of ${day.format(m.night)} · kick-off ${m.start}, ${m.minutes} min", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { save(upcoming - m) }) { Text("Remove") }
                }
            }
            Text("Night of", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                nights.forEach { n -> FilterChip(selected = night == n, onClick = { night = n }, label = { Text(day.format(n)) }) }
            }
            Text("Kick-off", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KICKOFFS.forEach { t -> FilterChip(selected = kickoff == t, onClick = { kickoff = t }, label = { Text(t.toString()) }) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(120, 150).forEach { m -> FilterChip(selected = minutes == m, onClick = { minutes = m }, label = { Text(if (m == 120) "2 h" else "2 h 30 (extra time)") }) }
            }
            FilledTonalButton(onClick = { save(upcoming + MatchNight(night, kickoff, minutes)) }) { Text("Add match night") }
        }
    }
}

/** Common kick-offs in Vietnam time for European football. */
private val KICKOFFS = listOf(LocalTime.of(23, 30), LocalTime.of(0, 0), LocalTime.of(1, 30), LocalTime.of(2, 0), LocalTime.of(2, 45), LocalTime.of(3, 0))

@Composable
private fun FeatureGroup(title: String, status: FeatureStatus, state: SettingsUiState, vm: SettingsViewModel) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    SoftCard(tone = if (status == FeatureStatus.TRIAL) CardTone.Lavender else CardTone.Plain) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Feature.entries.filter { it.status == status }.forEach { f ->
                SwitchRow(
                    "${f.title} · ${f.ref}",
                    f.summary + if (f.restricts) " Switching it off starts tomorrow." else "",
                    state.settings.on(f), pendingText(state, f.key),
                ) { vm.set(f.key, it.toString()) }
            }
        }
    }
}

private fun pendingText(state: SettingsUiState, key: String, unit: String = ""): String? {
    val (value, from) = state.pending[key] ?: return null
    val shown = when (value) {
        "true" -> "on"
        "false" -> "off"
        else -> runCatching { LocalTime.parse(value).toString() }.getOrDefault(value) + unit
    }
    return if (from == state.today.plusDays(1)) "Becomes $shown tomorrow; today's rule stays until 04:00" else "Becomes $shown on $from"
}

@Composable
private fun SwitchRow(title: String, body: String, checked: Boolean, pending: String?, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            pending?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** D-025: a schedule time, in 15-minute steps. Not a rule, so it applies at once. */
@Composable
private fun TimeStepper(title: String, value: LocalTime, key: String, state: SettingsUiState, vm: SettingsViewModel) =
    Stepper(
        title, value.toString(), pendingText(state, key),
        onMinus = { vm.set(key, value.minusMinutes(15).toString()) },
        onPlus = { vm.set(key, value.plusMinutes(15).toString()) },
    )

@Composable
private fun Stepper(title: String, value: String, pending: String?, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            pending?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        }
        FilledTonalIconButton(onClick = onMinus) { Text("−") }
        Text(value, Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleMedium)
        FilledTonalIconButton(onClick = onPlus) { Text("+") }
    }
}
