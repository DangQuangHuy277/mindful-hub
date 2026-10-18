package io.github.dangquanghuy277.mindfulhub.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dangquanghuy277.mindfulhub.core.DayUse
import io.github.dangquanghuy277.mindfulhub.ui.HeatmapPalette
import io.github.dangquanghuy277.mindfulhub.ui.formatMinutesShort

/**
 * Bars, not shades: length on a shared baseline is read far more accurately than color
 * (Cleveland & McGill 1984), which is why these replaced the heatmap as the default (D-018).
 * The same two colors mean the same thing in every chart: before and after the bedtime target.
 */
data class ChartColors(val before: Color, val after: Color)

@Composable
fun chartColors(): ChartColors = HeatmapPalette.steps(isSystemInDarkTheme()).let { ChartColors(before = it[2], after = it[5]) }

/**
 * One bar per day. The after-bedtime part sits on the baseline so it compares across days; last
 * week's total for the same weekday is an outline behind it.
 */
@Composable
fun WeekTrendChart(
    days: List<DayUse>,
    lastWeek: List<DayUse>?,
    dayLabels: List<String>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = chartColors()
    val ghost = MaterialTheme.colorScheme.outline
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = labelStyle.copy(color = MaterialTheme.colorScheme.onSurface)
    val max = maxOf(days.maxOfOrNull { it.minutes } ?: 0.0, lastWeek?.maxOfOrNull { it.minutes } ?: 0.0, 1.0)
    val description = "Minutes per day: " + days.zip(dayLabels).joinToString { (d, l) -> "$l ${formatMinutesShort(d.minutes)}" }

    Canvas(
        modifier.height(170.dp).semantics { contentDescription = description }.pointerInput(days) {
            detectTapGestures { p -> onSelect((p.x / (size.width / days.size)).toInt().coerceIn(days.indices)) }
        },
    ) {
        val top = 16.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val scale = (bottom - top) / max.toFloat()
        val slot = size.width / days.size
        val barW = minOf(slot * 0.5f, 28.dp.toPx())
        days.forEachIndexed { i, d ->
            val cx = slot * i + slot / 2
            val alpha = if (selected == null || selected == i) 1f else 0.4f
            lastWeek?.getOrNull(i)?.let { g ->
                val gh = g.minutes.toFloat() * scale
                val pad = 4.dp.toPx()
                drawRoundRect(
                    ghost, Offset(cx - barW / 2 - pad, bottom - gh), Size(barW + 2 * pad, gh),
                    CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()), alpha = alpha,
                )
            }
            val total = d.minutes.toFloat() * scale
            val late = d.afterBedtime.toFloat() * scale
            topRoundedBar(cx - barW / 2, bottom - total, barW, total, colors.before, alpha)
            if (late > 0f) {
                drawRect(colors.after, Offset(cx - barW / 2, bottom - late), Size(barW, late), alpha = alpha)
                if (late < total) drawRect(surface, Offset(cx - barW / 2, bottom - late - 1.dp.toPx()), Size(barW, 1.dp.toPx()))
            }
            if (d.minutes >= 1) centeredText(measurer, formatMinutesShort(d.minutes), cx, bottom - total - 14.dp.toPx(), valueStyle)
            centeredText(measurer, dayLabels[i], cx, bottom + 4.dp.toPx(), labelStyle)
        }
    }
}

/** Average minutes per clock hour, in logical-day order. The bedtime band is shaded. */
@Composable
fun HourBarsChart(
    hours: List<Int>,
    averages: List<Double>,
    /** Fractional bar index where the bedtime target falls, e.g. 20.5 for 00:30 with a 04:00 day. */
    bedtimeAt: Float,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = chartColors()
    val band = MaterialTheme.colorScheme.surfaceVariant
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = labelStyle.copy(color = MaterialTheme.colorScheme.onSurface)
    val max = maxOf(averages.maxOrNull() ?: 0.0, 1.0)
    val peak = averages.indices.maxByOrNull { averages[it] }
    val description = "Average minutes per hour; the busiest hour starts at ${peak?.let { hours[it] } ?: "none"}:00"

    Canvas(
        modifier.height(150.dp).semantics { contentDescription = description }.pointerInput(hours) {
            detectTapGestures { p -> onSelect((p.x / (size.width / hours.size)).toInt().coerceIn(hours.indices)) }
        },
    ) {
        val top = 16.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val scale = (bottom - top) / max.toFloat()
        val slot = size.width / hours.size
        drawRect(band, Offset(slot * bedtimeAt, 0f), Size(size.width - slot * bedtimeAt, bottom))
        averages.forEachIndexed { i, v ->
            val h = v.toFloat() * scale
            val color = if (i >= bedtimeAt) colors.after else colors.before
            val alpha = if (selected == null || selected == i) 1f else 0.4f
            if (h > 0f) topRoundedBar(slot * i + 1.dp.toPx(), bottom - h, slot - 2.dp.toPx(), h, color, alpha)
            if (i % 6 == 0) centeredText(measurer, "%02d".format(hours[i]), slot * i, bottom + 4.dp.toPx(), labelStyle)
        }
        if (peak != null && averages[peak] >= 1) {
            val label = formatMinutesShort(averages[peak])
            centeredText(measurer, label, slot * peak + slot / 2, bottom - averages[peak].toFloat() * scale - 14.dp.toPx(), valueStyle)
        }
    }
}

/** Tiny bars for an insight line; no axes, the sentence carries the numbers. */
@Composable
fun Sparkline(values: List<Double>, modifier: Modifier = Modifier) {
    val color = chartColors().after
    val max = maxOf(values.maxOrNull() ?: 0.0, 1.0)
    Canvas(modifier.size(width = 56.dp, height = 24.dp)) {
        val slot = size.width / values.size.coerceAtLeast(1)
        values.forEachIndexed { i, v ->
            val h = (v / max).toFloat() * size.height
            topRoundedBar(slot * i + 1.dp.toPx(), size.height - h, slot - 2.dp.toPx(), h, color, 1f)
        }
    }
}

@Composable
fun ChartLegend(items: List<Pair<String, Color?>>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        val outline = MaterialTheme.colorScheme.outline
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val shape = RoundedCornerShape(2.dp)
                // A null color means "outline", the last-week marker.
                Box(Modifier.size(10.dp).then(if (color != null) Modifier.background(color, shape) else Modifier.border(1.dp, outline, shape)))
                Spacer(Modifier.size(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun DrawScope.topRoundedBar(left: Float, top: Float, width: Float, height: Float, color: Color, alpha: Float) {
    if (height <= 0f) return
    val r = CornerRadius(minOf(4.dp.toPx(), width / 2, height))
    val path = Path().apply {
        addRoundRect(RoundRect(left, top, left + width, top + height, topLeftCornerRadius = r, topRightCornerRadius = r))
    }
    drawPath(path, color, alpha = alpha)
}

private fun DrawScope.centeredText(measurer: TextMeasurer, text: String, cx: Float, top: Float, style: TextStyle) {
    val layout = measurer.measure(text, style)
    val x = (cx - layout.size.width / 2f).coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
    drawText(layout, topLeft = Offset(x, top))
}
