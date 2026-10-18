package io.github.dangquanghuy277.mindfulhub.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val Ink = Color(0xFF2E2430)

/**
 * Peach and lavender on white (D-019). Always light: you asked for white as the main color.
 * Contrast is checked against white: primary 5.0:1, muted text 5.6:1, container text ≥10:1.
 * It lives in `:gate` because the pause overlay uses it too, and `:app` depends on `:gate`.
 */
private val Scheme = lightColorScheme(
    primary = Color(0xFFB4513A), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3D8), onPrimaryContainer = Color(0xFF5C2416),
    secondary = Color(0xFF6E5DC6), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEAFF), onSecondaryContainer = Color(0xFF2B2265),
    tertiary = Color(0xFF2E7D62), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF4EA), onTertiaryContainer = Color(0xFF0E3B2C),
    background = Color.White, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFF7EDE8), onSurfaceVariant = Color(0xFF6B5E6E),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFFFBF9),
    surfaceContainer = Color(0xFFFFF8F5), surfaceContainerHigh = Color(0xFFFFF5F1),
    surfaceContainerHighest = Color(0xFFFFF1EB),
    outline = Color(0xFFB9A8A2), outlineVariant = Color(0xFFF1E2DC),
)

private val SoftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp), // chips and small buttons read as pills
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MindfulTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, shapes = SoftShapes, content = content)
}

/** Follows the theme, not the system, so charts pick the right palette when the app forces light. */
@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

enum class CardTone { Plain, Peach, Lavender }

/**
 * The app's card. Plain cards are white with a soft outline: chart colors are tuned for white.
 * Tinted cards hold text only (the gate status, the findings).
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.Plain,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val (container, onContainer) = when (tone) {
        CardTone.Plain -> c.surface to c.onSurface
        CardTone.Peach -> c.primaryContainer to c.onPrimaryContainer
        CardTone.Lavender -> c.secondaryContainer to c.onSecondaryContainer
    }
    Card(
        modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        border = if (tone == CardTone.Plain) BorderStroke(1.dp, c.outlineVariant) else null,
        content = content,
    )
}
