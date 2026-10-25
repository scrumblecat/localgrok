package com.localgrok.ui.components.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.data.local.entity.MessageEntity
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

enum class ResponseStatus {
    THINKING,
    STREAMING,
    COMPLETE,
}

@Composable
fun UserMessageBubble(message: MessageEntity, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier =
                Modifier.widthIn(max = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp,
                        )
                    )
                    .background(colors.userBubble)
                    .padding(14.dp)
        ) {
            Text(
                text = message.content,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = InterFont,
                        fontSize = 16.sp,
                    ),
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
fun AssistantMessageBubble(
    message: MessageEntity,
    reasoningContent: String = "",
    isReasoningEnabled: Boolean = true,
    isExecutingTool: Boolean = false,
    toolDisplayName: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current

    val responseStatus by
        remember(message.isStreaming, message.isThinking, message.content) {
            derivedStateOf {
                when {
                    message.isThinking -> ResponseStatus.THINKING
                    message.isStreaming -> ResponseStatus.STREAMING
                    else -> ResponseStatus.COMPLETE
                }
            }
        }

    var previousStatus by remember { mutableStateOf(responseStatus) }
    var showThinkingIndicator by remember {
        mutableStateOf(responseStatus == ResponseStatus.THINKING)
    }

    var isReasoningExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(responseStatus) {
        when {
            responseStatus == ResponseStatus.THINKING -> {
                showThinkingIndicator = true
            }

            previousStatus == ResponseStatus.THINKING &&
                responseStatus != ResponseStatus.THINKING -> {
                showThinkingIndicator = false
            }
        }
        previousStatus = responseStatus
    }

    LaunchedEffect(isExecutingTool) {
        if (isExecutingTool) {
            isReasoningExpanded = false
        }
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Column(modifier = Modifier.widthIn(max = 340.dp)) {
            fun containsToolArtifacts(content: String): Boolean {
                return content.contains("<tool_result>", ignoreCase = true) ||
                    content.contains("</tool_result>", ignoreCase = true) ||
                    content.contains("<tool_call>", ignoreCase = true) ||
                    content.contains("</tool_call>", ignoreCase = true) ||
                    content.contains("Search results:", ignoreCase = true) ||
                    content.contains("Current date and time:", ignoreCase = true)
            }

            val persistedToolUsed = message.toolUsed
            val persistedToolDisplayName = message.toolDisplayName.takeIf { it.isNotBlank() }

            val toolWasUsed =
                remember(
                    message.content,
                    toolDisplayName,
                    persistedToolUsed,
                    persistedToolDisplayName,
                ) {
                    toolDisplayName != null ||
                        persistedToolUsed ||
                        persistedToolDisplayName != null ||
                        containsToolArtifacts(message.content)
                }

            val detectedToolName =
                remember(message.content, toolDisplayName, persistedToolDisplayName) {
                    val displayName = toolDisplayName ?: persistedToolDisplayName
                    when {
                        displayName != null -> displayName
                        toolWasUsed -> "Completed"
                        message.content.contains("Search results:", ignoreCase = true) ||
                            message.content.contains("<tool_result>", ignoreCase = true) &&
                                (message.content.contains("Search", ignoreCase = true) ||
                                    message.content.contains("web", ignoreCase = true)) ->
                            "Searching..."

                        message.content.contains("Current date and time:", ignoreCase = true) ||
                            (message.content.contains("<tool_result>", ignoreCase = true) &&
                                (message.content.contains("time", ignoreCase = true) ||
                                    message.content.contains("date", ignoreCase = true) ||
                                    message.content.contains("calendar", ignoreCase = true))) ->
                            "Checking time..."

                        else -> null
                    }
                }

            if (isExecutingTool && responseStatus != ResponseStatus.STREAMING) {

                ToolExecutionIndicator(
                    displayText = toolDisplayName ?: "Working...",
                    isCompleted = false,
                    colors = colors,
                )

                return@Row
            } else if (isExecutingTool && responseStatus == ResponseStatus.STREAMING) {

                ToolExecutionIndicator(
                    displayText = toolDisplayName ?: "Working...",
                    isCompleted = false,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else if (
                toolWasUsed &&
                    responseStatus == ResponseStatus.STREAMING &&
                    !isExecutingTool &&
                    (toolDisplayName != null || detectedToolName != null)
            ) {

                ToolExecutionIndicator(
                    displayText = toolDisplayName ?: detectedToolName ?: "Completed",
                    isCompleted = true,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else if (
                toolWasUsed && responseStatus == ResponseStatus.COMPLETE && detectedToolName != null
            ) {

                ToolExecutionIndicator(
                    displayText = detectedToolName,
                    isCompleted = true,
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val hasReasoning = reasoningContent.isNotBlank()
            val showThinkingSection =
                when {
                    showThinkingIndicator -> true

                    responseStatus == ResponseStatus.COMPLETE -> hasReasoning && !toolWasUsed

                    else -> false
                }

            if (showThinkingSection) {
                CollapsibleThinkingSection(
                    isThinking = showThinkingIndicator,
                    reasoningContent = reasoningContent,
                    messageContent = message.content,
                    isExpanded = isReasoningExpanded,
                    onToggleExpanded = { isReasoningExpanded = !isReasoningExpanded },
                    colors = colors,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val showLoadingSpinner =
                !isReasoningEnabled &&
                    message.content.isBlank() &&
                    responseStatus == ResponseStatus.STREAMING &&
                    !showThinkingIndicator

            if (showLoadingSpinner) {
                val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
                val scale by
                    infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        label = "pulse_scale",
                    )

                Box(
                    modifier =
                        Modifier.size(12.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(color = colors.textPrimary, shape = CircleShape)
                )
            }

            if (!showThinkingIndicator) {
                Markdown(
                    content = message.content,
                    colors =
                        markdownColor(
                            text = colors.textPrimary,
                            codeText = colors.textPrimary,
                            codeBackground = colors.darkGrey,
                            linkText = colors.accent,
                            inlineCodeText = colors.textPrimary,
                            inlineCodeBackground = colors.darkGrey,
                            dividerColor = Color.Transparent,
                        ),
                    typography =
                        markdownTypography(
                            h1 =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = InterFont,
                                    color = colors.textPrimary,
                                ),
                            h2 =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = InterFont,
                                    color = colors.textPrimary,
                                ),
                            h3 =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = InterFont,
                                    color = colors.textPrimary,
                                ),
                            h4 =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = InterFont,
                                    color = colors.textPrimary,
                                ),
                            h5 =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = InterFont,
                                    color = colors.textPrimary,
                                ),
                            h6 =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                ),
                            text =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                ),
                            code =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary,
                                ),
                            quote =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontStyle = FontStyle.Italic,
                                    color = colors.textSecondary,
                                ),
                            paragraph =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                ),
                            ordered =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                ),
                            bullet =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                ),
                            list =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = InterFont,
                                    fontSize = 16.sp,
                                    color = colors.textPrimary,
                                ),
                        ),
                )
            }

            if (responseStatus == ResponseStatus.COMPLETE && message.content.isNotBlank()) {
                MessageActionToolbar(
                    messageContent = message.content,
                    context = context,
                    colors = colors,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageActionToolbar(
    messageContent: String,
    context: Context,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Response", messageContent)
                clipboardManager.setPrimaryClip(clip)
            },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy message",
                tint = colors.textDim,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = {
                val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, messageContent)
                    }
                context.startActivity(Intent.createChooser(shareIntent, "Share response"))
            },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share message",
                tint = colors.textDim,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
