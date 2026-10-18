package io.github.dangquanghuy277.mindfulhub.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

internal fun formatMinutes(min: Double): String {
    val m = min.roundToInt()
    return if (m >= 60) "${m / 60} h ${m % 60} m" else "$m m"
}

/** "7h19" / "45m": for chart labels where space is tight. */
internal fun formatMinutesShort(min: Double): String {
    val m = min.roundToInt()
    return if (m >= 60) "${m / 60}h%02d".format(m % 60) else "${m}m"
}

/** Locale-aware date formatter that follows configuration changes (lint: NonObservableLocale). */
@Composable
internal fun rememberFormatter(pattern: String): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale, pattern) { DateTimeFormatter.ofPattern(pattern, locale) }
}
