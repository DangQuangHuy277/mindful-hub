package io.github.dangquanghuy277.mindfulhub.ui.views

import androidx.compose.runtime.Composable
import io.github.dangquanghuy277.mindfulhub.core.AppSession
import io.github.dangquanghuy277.mindfulhub.core.LogicalDayClock
import io.github.dangquanghuy277.mindfulhub.ui.GateToday
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * One way of looking at the same usage data (D-018). To add a view, implement this and append it
 * to [UsageViews.all]: the tab row on the usage screen lists whatever is registered there.
 */
interface UsageView {
    /** Stable key, kept across restarts of the screen. */
    val id: String
    val label: String

    @Composable
    fun Content(data: UsageSnapshot)
}

object UsageViews {
    val all: List<UsageView> = listOf(InsightsView, HeatmapView)

    fun byId(id: String): UsageView = all.firstOrNull { it.id == id } ?: all.first()
}

/** Which apps the views count. Gated apps are the default: they're what the goals are about (G1). */
sealed interface AppFilter {
    data object Gated : AppFilter
    data object All : AppFilter
    data class One(val pkg: String) : AppFilter
}

/** Everything a view may draw from. Views derive their own models from it, so adding one touches no other code. */
data class UsageSnapshot(
    val now: Instant,
    val clock: LogicalDayClock,
    /** The last 7 logical days, oldest first; the last one is today. */
    val days: List<LocalDate>,
    /** The 7 days before [days], or null when stored history doesn't fully cover them. */
    val previousDays: List<LocalDate>?,
    /** Sessions of every app except the launcher, system UI and this app, covering both weeks. */
    val sessions: List<AppSession>,
    val gated: Set<String>,
    val filter: AppFilter,
    val bedtime: LocalTime,
    val gateToday: GateToday,
    val labels: Map<String, String>,
) {
    fun includes(pkg: String): Boolean = when (filter) {
        AppFilter.Gated -> pkg in gated
        AppFilter.All -> true
        is AppFilter.One -> pkg == filter.pkg
    }

    fun label(pkg: String): String = labels[pkg] ?: pkg
}
