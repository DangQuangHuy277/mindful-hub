package io.github.dangquanghuy277.mindfulhub.ui.views

import io.github.dangquanghuy277.mindfulhub.theme.isDarkTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dangquanghuy277.mindfulhub.ui.HeatmapPalette

/**
 * Bars, not shades: length on a shared baseline is read far more accurately than color
 * (Cleveland & McGill 1984), which is why these replaced the heatmap as the default (D-018).
 */
data class ChartColors(val before: Color, val after: Color)

@Composable
fun chartColors(): ChartColors = HeatmapPalette.steps(isDarkTheme()).let { ChartColors(before = it[2], after = it[5]) }

/**
 * One bar per slot (a day or an hour), stacked by app: `values[slot][series]`, bottom series
 * first. [ghost] draws an outline per slot (last week's total), [bandFrom] shades from that
 * fractional slot to the end (after bedtime).
 */
@Composable
fun StackedBarChart(
    values: List<List<Double>>,
    colors: List<Color>,
    xLabel: (Int) -> String?,
    topLabel: (Int) -> String?,
    selected: Int?,
    onSelect: (Int) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    height: Dp = 170.dp,
    barFraction: Float = 0.5f,
    maxBarWidth: Dp = 28.dp,
    ghost: List<Double>? = null,
    bandFrom: Float? = null,
    /** Smaller for dense charts, where every bar carries a number. */
    topLabelSize: TextUnit = 10.sp,
) {
    val ghostColor = MaterialTheme.colorScheme.outline
    val gapColor = MaterialTheme.colorScheme.surface
    val bandColor = MaterialTheme.colorScheme.surfaceVariant
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = labelStyle.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = topLabelSize)
    val max = maxOf(values.maxOfOrNull { it.sum() } ?: 0.0, ghost?.maxOrNull() ?: 0.0, 1.0)

    Canvas(
        modifier.height(height).semantics { contentDescription = description }.pointerInput(values.size) {
            detectTapGestures { p -> onSelect((p.x / (size.width / values.size)).toInt().coerceIn(values.indices)) }
        },
    ) {
        val top = 16.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val scale = (bottom - top) / max.toFloat()
        val slot = size.width / values.size
        val barW = minOf(slot * barFraction, maxBarWidth.toPx())
        val gap = 1.dp.toPx()
        bandFrom?.let { drawRect(bandColor, Offset(slot * it, 0f), Size(size.width - slot * it, bottom)) }
        values.forEachIndexed { i, stack ->
            val cx = slot * i + slot / 2
            val left = cx - barW / 2
            val alpha = if (selected == null || selected == i) 1f else 0.4f
            ghost?.getOrNull(i)?.let { g ->
                val gh = g.toFloat() * scale
                val pad = 4.dp.toPx()
                drawRoundRect(
                    ghostColor, Offset(left - pad, bottom - gh), Size(barW + 2 * pad, gh),
                    CornerRadius(4.dp.toPx()), style = Stroke(1.dp.toPx()), alpha = alpha,
                )
            }
            val total = stack.sum().toFloat() * scale
            // The whole bar gets the rounded top in the top segment's color; segments are painted over it.
            val topIndex = stack.indices.lastOrNull { stack[it] > 0 }
            if (topIndex != null) topRoundedBar(left, bottom - total, barW, total, colors[topIndex], alpha)
            var y = bottom
            stack.forEachIndexed { s, v ->
                val h = v.toFloat() * scale
                if (h <= 0f || s == topIndex) return@forEachIndexed
                drawRect(colors[s], Offset(left, y - h), Size(barW, h), alpha = alpha)
                drawRect(gapColor, Offset(left, y - h - gap), Size(barW, gap))
                y -= h
            }
            topLabel(i)?.let { centeredText(measurer, it, cx, bottom - total - topLabelSize.toPx() - 3.dp.toPx(), valueStyle) }
            xLabel(i)?.let { centeredText(measurer, it, cx, bottom + 4.dp.toPx(), labelStyle) }
        }
    }
}

/** Tappable legend: tapping an app isolates it in the charts; tapping it again shows all. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppLegend(labels: List<String>, colors: List<Color>, isolated: Int?, onTap: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { i, label ->
            val dim = isolated != null && isolated != i
            Row(
                Modifier.clip(RoundedCornerShape(8.dp)).clickable { onTap(i) }.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(10.dp).background(colors[i].copy(alpha = if (dim) 0.3f else 1f), RoundedCornerShape(2.dp)))
                Spacer(Modifier.size(4.dp))
                Text(
                    label, style = MaterialTheme.typography.labelMedium,
                    color = if (dim) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
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
