package io.github.dangquanghuy277.mindfulhub.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PillTone { Primary, Accent, Lavender, Mint }

/** Scales down a little while pressed and springs back: playful, and it shows the tap landed. */
@Composable
private fun Modifier.squish(source: MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f, spring(dampingRatio = 0.45f, stiffness = 600f), label = "squish",
    )
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

/**
 * The app's button: a soft pill with an optional emoji. [alignStart] suits full-width choice
 * lists, where the eye scans down the left edge.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    tone: PillTone = PillTone.Primary,
    alignStart: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val c = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        PillTone.Primary -> c.primary to c.onPrimary
        PillTone.Accent -> c.primaryContainer to c.onPrimaryContainer
        PillTone.Lavender -> c.secondaryContainer to c.onSecondaryContainer
        PillTone.Mint -> c.tertiaryContainer to c.onTertiaryContainer
    }
    Button(
        onClick = onClick,
        modifier = modifier.squish(source).heightIn(min = 52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        interactionSource = source,
    ) {
        Row(
            if (alignStart) Modifier.fillMaxWidth() else Modifier,
            horizontalArrangement = if (alignStart) Arrangement.Start else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** A big round emoji button with a caption, for one-tap answers like 👍 😐 👎. */
@Composable
fun EmojiCircleButton(emoji: String, caption: String, onClick: () -> Unit, tone: PillTone = PillTone.Lavender) {
    val source = remember { MutableInteractionSource() }
    val c = MaterialTheme.colorScheme
    val container = when (tone) {
        PillTone.Primary, PillTone.Accent -> c.primaryContainer
        PillTone.Lavender -> c.secondaryContainer
        PillTone.Mint -> c.tertiaryContainer
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.squish(source).size(72.dp),
            shape = CircleShape,
            color = container,
            interactionSource = source,
        ) {
            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 32.sp) }
        }
        Spacer(Modifier.size(6.dp))
        Text(caption, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
    }
}

/** The pause countdown: the number inside a ring that empties smoothly. */
@Composable
fun CountdownRing(secondsLeft: Int, totalSeconds: Int, modifier: Modifier = Modifier) {
    val target = if (totalSeconds > 0) secondsLeft / totalSeconds.toFloat() else 0f
    val progress by animateFloatAsState(target, tween(durationMillis = 1000, easing = LinearEasing), label = "ring")
    val c = MaterialTheme.colorScheme
    Box(modifier.size(132.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(132.dp),
            color = c.primary,
            trackColor = c.primaryContainer,
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round,
        )
        Text("$secondsLeft", style = MaterialTheme.typography.displayMedium, color = c.onSurface)
    }
}
