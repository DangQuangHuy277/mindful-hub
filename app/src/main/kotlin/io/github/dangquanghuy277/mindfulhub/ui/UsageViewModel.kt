package io.github.dangquanghuy277.mindfulhub.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.core.AppSession
import io.github.dangquanghuy277.mindfulhub.core.Heatmap
import io.github.dangquanghuy277.mindfulhub.core.Sessionizer
import io.github.dangquanghuy277.mindfulhub.core.UsageAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

data class AppRow(val pkg: String, val label: String, val minutesPerDay: Double, val opensPerDay: Double)

data class UsageUiState(
    val hasPermission: Boolean = true,
    val loading: Boolean = false,
    val historySince: Instant? = null,
    val days: List<LocalDate> = emptyList(),
    val apps: List<AppRow> = emptyList(),
    /** null = all apps (except launcher, system UI and this app). */
    val selected: String? = null,
    val heatmap: Heatmap? = null,
)

class UsageViewModel(private val app: Application, private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(UsageUiState())
    val state = _state.asStateFlow()

    private var sessions: List<AppSession> = emptyList()
    private val excluded: Set<String> by lazy { excludedPackages() }

    fun refresh() {
        viewModelScope.launch {
            if (!c.importer.hasPermission()) {
                _state.update { it.copy(hasPermission = false) }
                return@launch
            }
            _state.update { it.copy(hasPermission = true, loading = true) }
            runCatching { c.importer.importNew() }

            val now = Instant.now()
            val today = c.clock.dayOf(now)
            val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val from = c.clock.startOf(days.first())
            // Start 3 h early so a session already running at the window start is not lost.
            val events = c.usage.events(from.minus(Duration.ofHours(3)), now)
            val (apps, history) = withContext(Dispatchers.Default) {
                sessions = Sessionizer.sessions(events, now)
                val totals = UsageAnalytics.totals(sessions, from, now).filter { it.pkg !in excluded }
                totals.take(10).map {
                    AppRow(it.pkg, label(it.pkg), it.minutes / days.size, it.opens.toDouble() / days.size)
                } to c.usage.historySince()
            }
            val selected = _state.value.selected?.takeIf { s -> apps.any { it.pkg == s } }
            _state.update {
                it.copy(loading = false, historySince = history, days = days, apps = apps, selected = selected,
                    heatmap = heatmapFor(selected, days))
            }
        }
    }

    fun select(pkg: String?) {
        _state.update { it.copy(selected = pkg, heatmap = heatmapFor(pkg, it.days)) }
    }

    private fun heatmapFor(pkg: String?, days: List<LocalDate>): Heatmap =
        UsageAnalytics.heatmap(sessions, c.clock, days) { if (pkg == null) it !in excluded else it == pkg }

    private fun label(pkg: String): String = runCatching {
        val pm = app.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    private fun excludedPackages(): Set<String> {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val launchers = app.packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }
        return launchers.toSet() + setOf(app.packageName, "android", "com.android.systemui")
    }
}
