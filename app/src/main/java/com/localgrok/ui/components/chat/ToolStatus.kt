package com.localgrok.ui.components.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.localgrok.ui.theme.LocalAppColors
import com.localgrok.ui.theme.LocalGrokColors

@Composable
fun ToolExecutionIndicator(
    displayText: String,
    isCompleted: Boolean = false,
    colors: LocalGrokColors = LocalAppColors.current,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tool_pulse")

    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "tool_alpha",
        )

    val (icon, label) =
        when {
            displayText.contains("Search", ignoreCase = true) -> {
                val text = if (isCompleted) "Searched the web" else "Searching the web..."
                Pair(Icons.Rounded.Search, text)
            }

            displayText.contains("time", ignoreCase = true) ||
                displayText.contains("date", ignoreCase = true) ||
                displayText.contains("calendar", ignoreCase = true) -> {
                val text = if (isCompleted) "Checked calendar" else "Checking calendar..."
                Pair(Icons.Rounded.Schedule, text)
            }

            else -> {
                val text = if (isCompleted) "Completed" else "Working..."
                Pair(Icons.Rounded.AutoAwesome, text)
            }
        }

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.7f),
            modifier =
                Modifier.size(16.dp).graphicsLayer {
                    if (!isCompleted) {
                        this.alpha = alpha
                    }
                },
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    color = colors.textSecondary.copy(alpha = 0.7f)
                ),
            modifier =
                Modifier.graphicsLayer {
                    if (!isCompleted) {
                        this.alpha = alpha
                    }
                },
        )
    }
}
