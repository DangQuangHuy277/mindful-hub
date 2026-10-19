package io.github.dangquanghuy277.mindfulhub.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dangquanghuy277.mindfulhub.theme.CardTone
import io.github.dangquanghuy277.mindfulhub.theme.PillButton
import io.github.dangquanghuy277.mindfulhub.theme.PillTone
import io.github.dangquanghuy277.mindfulhub.theme.SoftCard
import io.github.dangquanghuy277.mindfulhub.theme.isDarkTheme
import io.github.dangquanghuy277.mindfulhub.gate.BankLaunchActivity
import io.github.dangquanghuy277.mindfulhub.gate.GateStatus
import io.github.dangquanghuy277.mindfulhub.ui.views.AppFilter
import io.github.dangquanghuy277.mindfulhub.ui.views.UsageView
import io.github.dangquanghuy277.mindfulhub.ui.views.UsageViews
import java.time.LocalTime
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
            // The greeting, gate status and bank shortcuts moved to the Today tab (D-021).
            Text("Your phone, last 7 days", style = MaterialTheme.typography.headlineMedium)
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
internal fun NotificationPermission() {
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

/** Gate status when all is well: a glance, not a card. Today's numbers are in the findings. */
@Composable
internal fun GateOnPill() {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier.background(c.tertiaryContainer, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(c.tertiary, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("Gate on", style = MaterialTheme.typography.labelLarge, color = c.onTertiaryContainer)
    }
}

/** Fail loudly, stay quiet when fine (F16): only "off" earns a full card. */
@Composable
internal fun GateOffCard(onTurnedOn: () -> Unit) {
    val context = LocalContext.current
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Gate is off", style = MaterialTheme.typography.titleMedium)
            if (GateStatus.canSwitch(context)) {
                Text("It only checks which app is in front, and steps aside for banking apps.")
                PillButton("Turn the gate on", onClick = { if (GateStatus.turnOn(context)) onTurnedOn() }, emoji = "🔓")
                return@Column
            }
            Text("Turn on \"Mindful Hub gate\" in Accessibility > Installed apps. It only checks which app is in front.")
            PillButton("Open accessibility settings", onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, emoji = "🔓")
            Text(
                "The gate switches itself off for banking apps. To let it come back on by itself, run once from a computer: " +
                    "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Asks once per bank you use: a bank opened from its own icon checks for the gate before the gate
 * can step aside, and closes itself (D-020). The card goes once each shortcut is on the home screen.
 */
@Composable
internal fun BankShortcutCard(banks: List<BankApp>) {
    val context = LocalContext.current
    SoftCard(tone = CardTone.Lavender) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Open banks with the gate already off", style = MaterialTheme.typography.titleMedium)
            Text(
                "Some banks check for the gate as they start, before it can step aside, and close themselves " +
                    "(Techcombank does). Add their icons here to your home screen and open them from there.",
            )
            banks.forEach { bank ->
                PillButton("Add ${bank.label}", onClick = { BankLaunchActivity.requestPin(context, bank.pkg) }, emoji = "🏦", tone = PillTone.Accent)
            }
        }
    }
}

@Composable
private fun PermissionCard() {
    val context = LocalContext.current
    SoftCard(tone = CardTone.Accent) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Usage access needed", style = MaterialTheme.typography.titleMedium)
            Text("Mindful Hub reads which app was on screen and when. Everything stays on this phone.")
            PillButton("Open usage access settings", onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }, emoji = "📊")
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

/**
 * Top apps as horizontal bars: minutes or opens per day, one measure at a time (never two scales
 * on one chart). Gated apps keep their chart color; the rest are gray. Tapping a row filters to it.
 */
@Composable
private fun AppTable(apps: List<AppRow>, filter: AppFilter, onSelect: (AppFilter) -> Unit) {
    var byOpens by rememberSaveable { mutableStateOf(false) }
    val dark = isDarkTheme()
    val value = { a: AppRow -> if (byOpens) a.opensPerDay else a.minutesPerDay }
    val max = apps.maxOfOrNull(value)?.takeIf { it > 0 } ?: 1.0
    SoftCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Average per day", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                FilterChip(selected = !byOpens, onClick = { byOpens = false }, label = { Text("Time") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = byOpens, onClick = { byOpens = true }, label = { Text("Opens") })
            }
            apps.sortedByDescending(value).forEach { a ->
                val selected = filter == AppFilter.One(a.pkg)
                Row(
                    Modifier.fillMaxWidth().clickable { onSelect(AppFilter.One(a.pkg)) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        a.label, Modifier.width(104.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Box(Modifier.weight(1f).height(14.dp)) {
                        Box(
                            Modifier.fillMaxWidth((value(a) / max).toFloat().coerceAtLeast(0.01f)).fillMaxHeight()
                                .background(AppPalette.fixedOrNeutral(a.pkg, dark), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                        )
                    }
                    Text(
                        if (byOpens) "%.0f".format(a.opensPerDay) else formatMinutes(a.minutesPerDay),
                        Modifier.width(64.dp), textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** A friendly hello that also notices the hour: after midnight it nudges toward sleep (G7). */
internal fun greeting(now: LocalTime): String = when (now.hour) {
    in 4..11 -> "Good morning ☀️"
    in 12..17 -> "Good afternoon 🌤️"
    in 18..23 -> "Good evening 🌙"
    else -> "It's late. Sleep well 😴"
}
