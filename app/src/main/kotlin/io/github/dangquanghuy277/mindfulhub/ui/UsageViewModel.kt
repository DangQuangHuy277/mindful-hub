package io.github.dangquanghuy277.mindfulhub.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dangquanghuy277.mindfulhub.AppContainer
import io.github.dangquanghuy277.mindfulhub.core.AppProfile
import io.github.dangquanghuy277.mindfulhub.core.Feature
import io.github.dangquanghuy277.mindfulhub.core.sleepTimes
import io.github.dangquanghuy277.mindfulhub.data.toGateBox
import io.github.dangquanghuy277.mindfulhub.core.Sessionizer
import io.github.dangquanghuy277.mindfulhub.core.UsageAnalytics
import io.github.dangquanghuy277.mindfulhub.core.UsageInsights
import io.github.dangquanghuy277.mindfulhub.gate.GateStatus
import io.github.dangquanghuy277.mindfulhub.ui.views.AppFilter
import io.github.dangquanghuy277.mindfulhub.ui.views.UsageSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant

data class AppRow(val pkg: String, val label: String, val minutesPerDay: Double, val opensPerDay: Double)

data class BankApp(val pkg: String, val label: String)

/** Today's gate outcomes. "Abandoned" opens are the wins (G1). */
data class GateToday(val prompts: Int = 0, val abandoned: Int = 0, val proceeded: Int = 0, val blocked: Int = 0, val leftEarly: Int = 0)

data class UsageUiState(
    val gateEnabled: Boolean = false,
    val gateToday: GateToday = GateToday(),
    val hasPermission: Boolean = true,
    val loading: Boolean = false,
    val historySince: Instant? = null,
    /** Top apps of the last 7 days, for the filter chips and the table. */
    val apps: List<AppRow> = emptyList(),
    val filter: AppFilter = AppFilter.Gated,
    val snapshot: UsageSnapshot? = null,
)

class UsageViewModel(private val app: Application, private val c: AppContainer) : ViewModel() {
    /** Tonight's bedtime from your settings or the sleep quest; the findings measure "after bedtime" from it. */
    private var bedtime = UsageInsights.DEFAULT_BEDTIME
    private val _state = MutableStateFlow(UsageUiState())
    val state = _state.asStateFlow()

    private val excluded: Set<String> by lazy { excludedPackages() }
    private val gated: Set<String> by lazy {
        c.gateSettings.profiles.filterValues { it != AppProfile.MESSAGING && it != AppProfile.NONE }.keys
    }

    fun refresh() {
        viewModelScope.launch {
            val summary = c.gateDao.summary(c.clock.dayOf(Instant.now()).toString())
            fun count(decision: String, outcome: String? = null) =
                summary.filter { it.decision == decision && (outcome == null || it.outcome == outcome) }.sumOf { it.n }
            val today0 = c.clock.dayOf(Instant.now())
            val settings = c.settingsStore.on(today0)
            val boxesToday = c.gateDao.sessionsBetween(c.clock.startOf(today0).toEpochMilli(), c.clock.endOf(today0).toEpochMilli())
            val leftEarly = if (settings.on(Feature.WORTH_IT)) boxesToday.count { it.toGateBox().leftEarly } else 0
            val gateToday = GateToday(count("PROMPT"), count("PROMPT", "ABANDONED"), count("PROMPT", "PROCEEDED"), count("BLOCK"), leftEarly)
            bedtime = settings.sleepTimes(today0).bedtime
            _state.update { it.copy(gateEnabled = GateStatus.isEnabled(app), gateToday = gateToday) }
            if (!c.importer.hasPermission()) {
                _state.update { it.copy(hasPermission = false) }
                return@launch
            }
            _state.update { it.copy(hasPermission = true, loading = true) }
            runCatching { c.importer.importNew() }

            val now = Instant.now()
            val today = c.clock.dayOf(now)
            val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val previous = (13 downTo 7).map { today.minusDays(it.toLong()) }
            val history = c.usage.historySince()
            // Start 3 h early so a session already running at the window start is not lost.
            val events = c.usage.events(c.clock.startOf(previous.first()).minus(Duration.ofHours(3)), now)
            val (apps, snapshot) = withContext(Dispatchers.Default) {
                val sessions = Sessionizer.sessions(events, now).filter { it.pkg !in excluded }
                val from = c.clock.startOf(days.first())
                val apps = UsageAnalytics.totals(sessions, from, now).take(10).map {
                    AppRow(it.pkg, label(it.pkg), it.minutes / days.size, it.opens.toDouble() / days.size)
                }
                apps to UsageSnapshot(
                    now = now, clock = c.clock, days = days,
                    previousDays = previous.takeIf { history != null && !history.isAfter(c.clock.startOf(previous.first())) },
                    sessions = sessions, gated = gated, filter = _state.value.filter,
                    bedtime = bedtime, gateToday = gateToday,
                    labels = sessions.map { it.pkg }.distinct().associateWith(::label),
                )
            }
            val filter = _state.value.filter.takeUnless { f -> f is AppFilter.One && snapshot.sessions.none { it.pkg == f.pkg } }
                ?: AppFilter.Gated
            _state.update {
                it.copy(loading = false, historySince = history, apps = apps, filter = filter, snapshot = snapshot.copy(filter = filter))
            }
        }
    }

    fun select(filter: AppFilter) {
        _state.update { it.copy(filter = filter, snapshot = it.snapshot?.copy(filter = filter)) }
    }

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
