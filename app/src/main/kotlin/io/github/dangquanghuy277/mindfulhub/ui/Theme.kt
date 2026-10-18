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

/**
 * Categorical colors for per-app chart stacks (fixed-order Cove palette, light and dark steps).
 * A color follows the app, never its rank, so isolating or a new week never repaints Facebook.
 */
object AppPalette {
    /** Key used for the "every other app" stack. */
    const val OTHER = ""

    private val light = listOf(0xFF2A78D6, 0xFFEB6834, 0xFF1BAF7A, 0xFFEDA100, 0xFFE87BA4, 0xFF008300, 0xFF6250D6, 0xFFE34948).map(::Color)
    private val dark = listOf(0xFF3987E5, 0xFFD95926, 0xFF199E70, 0xFFC98500, 0xFFD55181, 0xFF008300, 0xFF9085E9, 0xFFE66767).map(::Color)
    private val otherLight = Color(0xFFB4B2A9)
    private val otherDark = Color(0xFF5F5E5A)

    private val fixed = mapOf(
        "com.facebook.katana" to 0,
        "com.twitter.android" to 1,
        "com.google.android.youtube" to 2,
        "com.instagram.barcelona" to 3, // Threads
        "com.instagram.android" to 4,
        "com.hhgame.mlbbvn" to 6,
    )

    /**
     * Colors for [keys] in order. Known apps keep their slot; others take never-fixed slots first,
     * then slots of known apps that aren't shown.
     */
    fun colorsFor(keys: List<String>, darkMode: Boolean): List<Color> {
        val steps = if (darkMode) dark else light
        val used = keys.mapNotNull { fixed[it] }.toSet()
        val free = ((steps.indices - fixed.values.toSet()) + (fixed.values.sorted() - used)).toMutableList()
        return keys.map { key ->
            when {
                key == OTHER -> if (darkMode) otherDark else otherLight
                key in fixed -> steps[fixed.getValue(key)]
                free.isNotEmpty() -> steps[free.removeAt(0)]
                else -> if (darkMode) otherDark else otherLight
            }
        }
    }
}
