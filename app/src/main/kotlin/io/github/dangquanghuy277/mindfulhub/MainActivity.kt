package io.github.dangquanghuy277.mindfulhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dangquanghuy277.mindfulhub.ui.MindfulTheme
import io.github.dangquanghuy277.mindfulhub.ui.UsageScreen
import io.github.dangquanghuy277.mindfulhub.ui.UsageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MindfulApp).container
        setContent {
            MindfulTheme {
                UsageScreen(viewModel { UsageViewModel(application, container) })
            }
        }
    }
}
