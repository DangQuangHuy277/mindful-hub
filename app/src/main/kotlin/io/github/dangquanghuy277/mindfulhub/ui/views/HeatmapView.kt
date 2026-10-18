package io.github.dangquanghuy277.mindfulhub.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.dangquanghuy277.mindfulhub.core.Heatmap
import io.github.dangquanghuy277.mindfulhub.core.UsageAnalytics
import io.github.dangquanghuy277.mindfulhub.ui.HeatmapPalette
import io.github.dangquanghuy277.mindfulhub.ui.rememberFormatter
import java.time.LocalDate
import kotlin.math.roundToInt

/** The original F09 view: every hour of every day at once. Good for spotting, poor for reading values. */
object HeatmapView : UsageView {
    override val id = "heatmap"
    override val label = "Heatmap"

    @Composable
    override fun Content(data: UsageSnapshot) {
        val map = remember(data) { UsageAnalytics.heatmap(data.sessions, data.clock, data.days, data::includes) }
        HeatmapCard(map)
    }
}

@Composable
private fun HeatmapCard(map: Heatmap) {
    var picked by remember(map) { mutableStateOf<Triple<LocalDate, Int, Double>?>(null) }
    val dayFmt = rememberFormatter("EEE d")
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Minutes per hour", style = MaterialTheme.typography.titleSmall)
            UsageHeatmap(map, Modifier.fillMaxWidth()) { d, h, m -> picked = Triple(d, h, m) }
            Text(
                picked?.let { (d, h, m) ->
                    "${dayFmt.format(d)}, %02d:00–%02d:00 · ${m.roundToInt()} min".format(h, (h + 1) % 24)
                } ?: "Tap a cell for details",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Legend()
        }
    }
}

/** Rows = logical days (oldest on top), columns = clock hours starting at 04:00. */
@Composable
private fun UsageHeatmap(map: Heatmap, modifier: Modifier = Modifier, onTap: (LocalDate, Int, Double) -> Unit) {
    val steps = HeatmapPalette.steps(isSystemInDarkTheme())
    val dayFmt = rememberFormatter("EEE d")
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val labelW = 44.dp
    val headerH = 16.dp
    val rowH = 26.dp
    val gap = 2.dp
    val description = "Heatmap of ${map.total.roundToInt()} minutes over ${map.days.size} days"

    Canvas(
        modifier
            .height(headerH + rowH * map.days.size)
            .semantics { contentDescription = description }
            .pointerInput(map) {
                detectTapGestures { p ->
                    val cellW = (size.width - labelW.toPx()) / 24f
                    val col = ((p.x - labelW.toPx()) / cellW).toInt()
                    val row = ((p.y - headerH.toPx()) / rowH.toPx()).toInt()
                    if (col in 0 until 24 && row in map.days.indices) {
                        val day = map.days[row]
                        val hour = map.hours[col]
                        onTap(day, hour, map[day, hour])
                    }
                }
            },
    ) {
        val cellW = (size.width - labelW.toPx()) / 24f
        map.hours.forEachIndexed { col, hour ->
            if (hour % 4 == 0) {
                drawText(measurer, "%02d".format(hour), Offset(labelW.toPx() + col * cellW, 0f), labelStyle)
            }
        }
        map.days.forEachIndexed { row, day ->
            val top = headerH.toPx() + row * rowH.toPx()
            drawText(measurer, dayFmt.format(day), Offset(0f, top + rowH.toPx() / 2 - 7.sp.toPx()), labelStyle)
            map.hours.forEachIndexed { col, hour ->
                drawRoundRect(
                    color = steps[HeatmapPalette.bucketOf(map[day, hour])],
                    topLeft = Offset(labelW.toPx() + col * cellW + gap.toPx() / 2, top + gap.toPx() / 2),
                    size = Size(cellW - gap.toPx(), rowH.toPx() - gap.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    val steps = HeatmapPalette.steps(isSystemInDarkTheme())
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        HeatmapPalette.bucketLabels.forEachIndexed { i, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(steps[i], RoundedCornerShape(2.dp)))
                Spacer(Modifier.size(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text("min", style = MaterialTheme.typography.labelSmall)
    }
}
