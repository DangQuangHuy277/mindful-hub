package io.github.dangquanghuy277.mindfulhub.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.github.dangquanghuy277.mindfulhub.core.FeatureStatus
import io.github.dangquanghuy277.mindfulhub.core.Settings
import io.github.dangquanghuy277.mindfulhub.core.SettingsCodec
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
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
