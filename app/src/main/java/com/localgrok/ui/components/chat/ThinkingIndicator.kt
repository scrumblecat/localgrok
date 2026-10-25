package com.localgrok.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.localgrok.ui.theme.LocalGrokColors
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

private fun containsToolArtifacts(content: String): Boolean {
    return content.contains("<tool_result>", ignoreCase = true) ||
        content.contains("</tool_result>", ignoreCase = true) ||
        content.contains("<tool_call>", ignoreCase = true) ||
        content.contains("</tool_call>", ignoreCase = true)
}

@Composable
fun CollapsibleThinkingSection(
    isThinking: Boolean,
    reasoningContent: String,
    messageContent: String = "",
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    colors: LocalGrokColors = LocalAppColors.current,
    modifier: Modifier = Modifier,
) {

    if (containsToolArtifacts(reasoningContent) || containsToolArtifacts(messageContent)) {
        return
    }
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_pulse")

    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "thinking_alpha",
        )

    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier.clickable(enabled = reasoningContent.isNotBlank()) { onToggleExpanded() }
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.7f),
                modifier =
                    Modifier.size(16.dp).graphicsLayer {
                        if (isThinking) {
                            this.alpha = alpha
                        }
                    },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text =
                    if (isThinking) "Thinking..."
                    else if (reasoningContent.isNotBlank()) "Thought" else "Thinking",
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = colors.textSecondary.copy(alpha = 0.7f)
                    ),
                modifier =
                    Modifier.graphicsLayer {
                        if (isThinking) {
                            this.alpha = alpha
                        }
                    },
            )

            if (reasoningContent.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        ),
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded && reasoningContent.isNotBlank(),
            enter =
                fadeIn(animationSpec = tween(200)) +
                    scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
            exit =
                fadeOut(animationSpec = tween(150)) +
                    scaleOut(targetScale = 0.95f, animationSpec = tween(150)),
        ) {
            Box(
                modifier =
                    Modifier.padding(start = 8.dp, top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.darkGrey.copy(alpha = 0.5f))
                        .padding(12.dp)
            ) {
                Markdown(
                    content = reasoningContent,
                    colors =
                        markdownColor(
                            text = colors.textSecondary,
                            codeText = colors.textSecondary,
                            codeBackground = colors.darkGrey,
                            linkText = colors.accent,
                            inlineCodeText = colors.textSecondary,
                            inlineCodeBackground = colors.darkGrey,
                            dividerColor = Color.Transparent,
                        ),
                    typography =
                        markdownTypography(
                            text =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = colors.textSecondary,
                                ),
                            code =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                ),
                            paragraph =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = colors.textSecondary,
                                ),
                        ),
                )
            }
        }
    }
}
