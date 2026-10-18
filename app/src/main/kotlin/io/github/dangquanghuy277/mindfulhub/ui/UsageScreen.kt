package io.github.dangquanghuy277.mindfulhub.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dangquanghuy277.mindfulhub.ui.views.AppFilter
import io.github.dangquanghuy277.mindfulhub.ui.views.UsageView
import io.github.dangquanghuy277.mindfulhub.ui.views.UsageViews
import java.time.ZoneId

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
            Text("Your phone, last 7 days", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Days start at 04:00, so late-night use counts toward the day before.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!state.hasPermission) {
                PermissionCard()
                return@Column
            }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            var viewId by rememberSaveable { mutableStateOf(UsageViews.all.first().id) }
            val view = UsageViews.byId(viewId)
            ViewTabs(view) { viewId = it.id }
            AppChips(state.apps, state.filter, vm::select)
            state.snapshot?.let { view.Content(it) }
            AppTable(state.apps, state.filter, vm::select)
            state.historySince?.let {
                Text(
                    "History stored since ${rememberFormatter("EEE d MMM, HH:mm").format(it.atZone(ZoneId.systemDefault()))}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Lists every registered [UsageView]; scrolls when more are added. */
@Composable
private fun ViewTabs(current: UsageView, onSelect: (UsageView) -> Unit) {
    PrimaryScrollableTabRow(
        selectedTabIndex = UsageViews.all.indexOf(current),
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        UsageViews.all.forEach { v ->
            Tab(selected = v == current, onClick = { onSelect(v) }, text = { Text(v.label) })
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
private fun AppChips(apps: List<AppRow>, filter: AppFilter, onSelect: (AppFilter) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = filter == AppFilter.Gated, onClick = { onSelect(AppFilter.Gated) }, label = { Text("Gated apps") })
        FilterChip(selected = filter == AppFilter.All, onClick = { onSelect(AppFilter.All) }, label = { Text("All apps") })
        apps.forEach { a ->
            val f = AppFilter.One(a.pkg)
            FilterChip(selected = filter == f, onClick = { onSelect(f) }, label = { Text(a.label) })
        }
    }
}

/** The table view of the same data: per-day averages over the 7 days. */
@Composable
private fun AppTable(apps: List<AppRow>, filter: AppFilter, onSelect: (AppFilter) -> Unit) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Average per day", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            apps.forEach { a ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSelect(AppFilter.One(a.pkg)) }.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        a.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (filter == AppFilter.One(a.pkg)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
