package io.github.dangquanghuy277.mindfulhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dangquanghuy277.mindfulhub.theme.MindfulTheme
import io.github.dangquanghuy277.mindfulhub.ui.UsageScreen
import io.github.dangquanghuy277.mindfulhub.ui.UsageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The theme is always light (D-019), so the system bars need dark icons even in dark mode.
        val bars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        val container = (application as MindfulApp).container
        setContent {
            MindfulTheme {
                UsageScreen(viewModel { UsageViewModel(application, container) })
            }
        }
    }
}
