package io.github.dangquanghuy277.mindfulhub.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.dangquanghuy277.mindfulhub.core.DayUse
import io.github.dangquanghuy277.mindfulhub.core.Insight
import io.github.dangquanghuy277.mindfulhub.core.InsightInput
import io.github.dangquanghuy277.mindfulhub.core.UsageAnalytics
import io.github.dangquanghuy277.mindfulhub.core.UsageInsights
import io.github.dangquanghuy277.mindfulhub.ui.formatMinutes
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import java.time.Duration
import kotlin.math.abs
import kotlin.math.roundToInt

/** D-018 option A + D + C: findings in words, then the week by day, then the average day by hour. */
object InsightsView : UsageView {
    override val id = "insights"
    override val label = "Insights"

    @Composable
    override fun Content(data: UsageSnapshot) {
        val model = remember(data) { InsightsModel.of(data) }
        InsightsCard(model.insights, data)
        WeekCard(model, data)
        HourCard(model, data)
    }
}

private class InsightsModel(
    val insights: List<Insight>,
    val week: List<DayUse>,
    val lastWeek: List<DayUse>?,
    val hours: List<Int>,
    val hourAverages: List<Double>,
) {
    companion object {
        fun of(d: UsageSnapshot): InsightsModel {
            val included = d.sessions.filter { d.includes(it.pkg) }
            val input = InsightInput(
                d.sessions, d::includes, d.clock, d.days, d.previousDays, d.bedtime, d.now,
                d.gateToday.prompts, d.gateToday.abandoned,
            )
            val heatmap = UsageAnalytics.heatmap(included, d.clock, d.days)
            return InsightsModel(
                insights = UsageInsights.insights(input),
                week = UsageInsights.dayUse(included, d.clock, d.days, d.bedtime),
                lastWeek = d.previousDays?.let { UsageInsights.dayUse(included, d.clock, it, d.bedtime) },
                hours = heatmap.hours,
                hourAverages = heatmap.averageByHour(),
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: List<Insight>, data: UsageSnapshot) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This week", style = MaterialTheme.typography.titleMedium)
            if (insights.isEmpty()) {
                Text(
                    "Not enough data yet. Findings show up after a few days of use.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            insights.forEachIndexed { i, insight ->
                if (i > 0) HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(insightText(insight, data), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    if (insight is Insight.AfterBedtime) Sparkline(insight.nights.map { it.afterBedtime }, Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun insightText(insight: Insight, data: UsageSnapshot): String {
    val time = rememberFormatter("HH:mm")
    val day = rememberFormatter("EEE d")
    return when (insight) {
        is Insight.AfterBedtime ->
            "After your ${time.format(insight.bedtime)} bedtime: ${formatMinutes(insight.averageMinutes)} a night on " +
                "average over the last ${insight.nights.size} nights."
        is Insight.FirstAfterWaking -> {
            val span = if (insight.earliest == insight.latest) time.format(insight.earliest)
            else "${time.format(insight.earliest)}–${time.format(insight.latest)}"
            "${data.label(insight.pkg)} was opened within ${UsageInsights.AFTER_WAKING.toMinutes()} minutes of waking " +
                "on ${insight.mornings} of ${insight.outOf} mornings ($span)."
        }
        is Insight.WeekChange -> {
            val diff = insight.thisWeek - insight.lastWeek
            val pct = (abs(diff) / insight.lastWeek * 100).roundToInt()
            when {
                abs(diff) < 5 -> "About the same as this time last week."
                diff < 0 -> "${formatMinutes(-diff)} less than this time last week (−$pct%)."
                else -> "${formatMinutes(diff)} more than this time last week (+$pct%)."
            }
        }
        is Insight.LongestSession -> {
            val s = insight.session
            "Longest session: ${data.label(s.pkg)}, ${formatMinutes(s.duration.toMillis() / 60_000.0)} from " +
                "${time.format(s.start.atZone(data.clock.zone))} on ${day.format(insight.day)}."
        }
        is Insight.PeakHour ->
            "Your heaviest hour is %02d:00–%02d:00, with ${formatMinutes(insight.minutesPerDay)} a day on average."
                .format(insight.hour, (insight.hour + 1) % 24)
        is Insight.PauseWins ->
            if (insight.abandoned > 0) {
                "Today, ${insight.abandoned} of ${insight.prompts} pauses ended without going in. Each one is a win."
            } else {
                "Today: ${insight.prompts} pauses so far. Closing the app at a pause counts as a win."
            }
    }
}

@Composable
private fun WeekCard(model: InsightsModel, data: UsageSnapshot) {
    var picked by remember(model) { mutableStateOf<Int?>(null) }
    val dayFmt = rememberFormatter("EEE")
    val longFmt = rememberFormatter("EEE d MMM")
    val bed = rememberFormatter("HH:mm").format(data.bedtime)
    val colors = chartColors()
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Per day", style = MaterialTheme.typography.titleSmall)
            ChartLegend(
                listOf("Before $bed" to colors.before, "After $bed" to colors.after) +
                    if (model.lastWeek != null) listOf("Last week" to null) else emptyList(),
            )
            WeekTrendChart(
                model.week, model.lastWeek,
                model.week.map { if (it.day == data.days.last()) "Today" else dayFmt.format(it.day) },
                picked, { picked = if (picked == it) null else it }, Modifier.fillMaxWidth(),
            )
            Text(
                picked?.let { i ->
                    val d = model.week[i]
                    val last = model.lastWeek?.getOrNull(i)?.let { " · last week ${formatMinutes(it.minutes)}" } ?: ""
                    "${longFmt.format(d.day)}: ${formatMinutes(d.minutes)}, ${formatMinutes(d.afterBedtime)} after $bed$last"
                } ?: if (model.lastWeek == null) "Tap a day for details. Last week shows up once 14 days are stored." else "Tap a day for details",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HourCard(model: InsightsModel, data: UsageSnapshot) {
    var picked by remember(model) { mutableStateOf<Int?>(null) }
    val bedtimeAt = Duration.between(
        data.clock.startOf(data.days.first()), data.clock.instantOn(data.days.first(), data.bedtime),
    ).toMinutes() / 60f
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Average day, by hour", style = MaterialTheme.typography.titleSmall)
            HourBarsChart(model.hours, model.hourAverages, bedtimeAt, picked, { picked = if (picked == it) null else it }, Modifier.fillMaxWidth())
            Text(
                picked?.let { i ->
                    val h = model.hours[i]
                    "%02d:00–%02d:00 · ${formatMinutes(model.hourAverages[i])} a day on average".format(h, (h + 1) % 24)
                } ?: "Shaded: after your bedtime. Tap an hour for details.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
