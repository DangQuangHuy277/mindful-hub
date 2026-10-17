package io.github.dangquanghuy277.mindfulhub.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MindfulTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}

/**
 * Sequential one-hue (blue) ramp for minutes of use. Each mode has its own steps, checked with the
 * dataviz palette validator (ordinal: monotone lightness, visible gaps, end step ≥2:1 against the
 * surface). Zero minutes uses a neutral that recedes into the surface.
 */
object HeatmapPalette {
    /** Upper bounds (minutes) of each bucket after zero: 1–5, 6–15, 16–30, 31–45, 46–60. */
    val bucketMax = listOf(5.0, 15.0, 30.0, 45.0, 60.0)
    val bucketLabels = listOf("0", "1–5", "6–15", "16–30", "31–45", "46–60")

    private val light = listOf(0xFFF0EFEC, 0xFF86B6EF, 0xFF5598E7, 0xFF2A78D6, 0xFF1C5CAB, 0xFF0D366B).map(::Color)
    private val dark = listOf(0xFF383835, 0xFF184F95, 0xFF256ABF, 0xFF3987E5, 0xFF6DA7EC, 0xFFB7D3F6).map(::Color)

    fun steps(darkMode: Boolean): List<Color> = if (darkMode) dark else light

    fun bucketOf(minutes: Double): Int =
        if (minutes < 0.5) 0 else 1 + bucketMax.indexOfFirst { minutes <= it + 0.5 }.let { if (it < 0) bucketMax.lastIndex else it }
}
