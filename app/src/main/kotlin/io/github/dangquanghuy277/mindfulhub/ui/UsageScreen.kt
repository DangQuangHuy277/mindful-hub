package io.github.dangquanghuy277.mindfulhub.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dangquanghuy277.mindfulhub.core.Heatmap
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun UsageScreen(vm: UsageViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    // Also re-checks the permission when coming back from the Settings screen.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotificationPermission()
            GateCard(state.gateEnabled, state.gateToday)
            Text("Usage, last 7 days", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Days start at 04:00, so late-night use counts toward the day before.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!state.hasPermission) {
                PermissionCard()
                return@Column
            }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            val locale = LocalConfiguration.current.locales[0]
            state.historySince?.let {
                Text(
                    "History stored since ${DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", locale).format(it.atZone(ZoneId.systemDefault()))}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppChips(state.apps, state.selected, vm::select)
            state.heatmap?.let { HeatmapCard(it) }
            AppTable(state.apps, state.selected, vm::select)
        }
    }
}

@Composable
private fun NotificationPermission() {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun GateCard(enabled: Boolean, today: GateToday) {
    val context = LocalContext.current
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (enabled) "Gate is on" else "Gate is off", style = MaterialTheme.typography.titleMedium)
            if (enabled) {
                Text(
                    "Today: ${today.prompts} pauses · ${today.abandoned} closed (wins) · ${today.proceeded} went in · ${today.blocked} short videos blocked",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text("Turn on \"Mindful Hub gate\" in Accessibility > Installed apps. It only checks which app is in front.")
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                    Text("Open accessibility settings")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard() {
    val context = LocalContext.current
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Usage access needed", style = MaterialTheme.typography.titleMedium)
            Text("Mindful Hub reads which app was on screen and when. Everything stays on this phone.")
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) {
                Text("Open usage access settings")
            }
        }
    }
}

@Composable
private fun AppChips(apps: List<AppRow>, selected: String?, onSelect: (String?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All apps") })
        apps.forEach { a -> FilterChip(selected = selected == a.pkg, onClick = { onSelect(a.pkg) }, label = { Text(a.label) }) }
    }
}

@Composable
private fun HeatmapCard(map: Heatmap) {
    var picked by remember(map) { mutableStateOf<Triple<LocalDate, Int, Double>?>(null) }
    val dayFmt = dayFormatter()
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

@Composable
private fun dayFormatter(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { DateTimeFormatter.ofPattern("EEE d", locale) }
}

/** Rows = logical days (oldest on top), columns = clock hours starting at 04:00. */
@Composable
fun UsageHeatmap(map: Heatmap, modifier: Modifier = Modifier, onTap: (LocalDate, Int, Double) -> Unit) {
    val dark = isSystemInDarkTheme()
    val steps = HeatmapPalette.steps(dark)
    val dayFmt = dayFormatter()
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

/** The table view of the same data: per-day averages over the 7 days. */
@Composable
private fun AppTable(apps: List<AppRow>, selected: String?, onSelect: (String?) -> Unit) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Average per day", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            apps.forEach { a ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSelect(a.pkg) }.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        a.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (a.pkg == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${formatMinutes(a.minutesPerDay)} · ${"%.0f".format(a.opensPerDay)} opens",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatMinutes(min: Double): String {
    val m = min.roundToInt()
    return if (m >= 60) "${m / 60} h ${m % 60} m" else "$m m"
}
