package io.github.dangquanghuy277.mindfulhub

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dangquanghuy277.mindfulhub.theme.MindfulTheme
import io.github.dangquanghuy277.mindfulhub.ui.UsageScreen
import io.github.dangquanghuy277.mindfulhub.ui.UsageViewModel
import io.github.dangquanghuy277.mindfulhub.ui.review.ReviewScreen
import io.github.dangquanghuy277.mindfulhub.ui.review.ReviewViewModel
import io.github.dangquanghuy277.mindfulhub.ui.settings.SettingsScreen
import io.github.dangquanghuy277.mindfulhub.ui.settings.SettingsViewModel
import io.github.dangquanghuy277.mindfulhub.ui.today.TodayScreen
import io.github.dangquanghuy277.mindfulhub.ui.today.TodayViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The theme is always light (D-019), so the system bars need dark icons even in dark mode.
        val bars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        val container = (application as MindfulApp).container
        setContent {
            MindfulTheme { AppRoot(container) }
        }
    }
}

/** The tabs (D-021). Today is home; the usage charts moved to their own tab. */
private enum class Tab(val label: String, val emoji: String) {
    TODAY("Today", "🏡"),
    USAGE("Usage", "📊"),
    REVIEW("Review", "📝"),
    SETTINGS("Settings", "⚙️"),
}

@Composable
private fun AppRoot(container: AppContainer) {
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    val app = (androidx.compose.ui.platform.LocalContext.current.applicationContext as MindfulApp)
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = t == tab,
                        onClick = { tab = t },
                        icon = { Text(t.emoji, fontSize = 20.sp) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (tab) {
                Tab.TODAY -> TodayScreen(viewModel { TodayViewModel(app, container) }, onOpenReview = { tab = Tab.REVIEW })
                Tab.USAGE -> UsageScreen(viewModel { UsageViewModel(app, container) })
                Tab.REVIEW -> ReviewScreen(viewModel { ReviewViewModel(app, container) })
                Tab.SETTINGS -> SettingsScreen(viewModel { SettingsViewModel(container) })
            }
        }
    }
}
