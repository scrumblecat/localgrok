package com.localgrok.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.localgrok.ui.theme.LocalGrokColors

data class ModelOption(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val modelId: String,
)

val MODEL_OPTIONS =
    listOf(
        ModelOption(
            id = "standard",
            displayName = "Flash",
            subtitle = "Instant chat",
            icon = Icons.Outlined.DarkMode,
            modelId = "gemma3:1b-it-qat",
        ),
        ModelOption(
            id = "reasoning",
            displayName = "Think",
            subtitle = "Tools & deep thinking",
            icon = Icons.Default.RocketLaunch,
            modelId = "qwen3:1.7b",
        ),
    )

val DEFAULT_MODEL = MODEL_OPTIONS.first()

fun modelOption(modelId: String): ModelOption =
    MODEL_OPTIONS.find { it.modelId == modelId }
        ?: ModelOption(modelId, modelId, "Ollama model", Icons.Outlined.DarkMode, modelId)

@Composable
fun UnifiedInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit = {},
    selectedModel: ModelOption,
    availableModels: List<String> = emptyList(),
    onModelSelected: (ModelOption) -> Unit,
    isGenerating: Boolean = false,
    enabled: Boolean = true,
    colors: LocalGrokColors = LocalAppColors.current,
    modifier: Modifier = Modifier,
) {
    var showModelDropdown by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.inputBackground)
                    .border(
                        width = 1.dp,
                        color = colors.inputBorder,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .clickable(interactionSource = interactionSource, indication = null) {
                        focusRequester.requestFocus()
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = false,
                maxLines = 6,
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textPrimary,
                        fontFamily = InterFont,
                        fontSize = 16.sp,
                    ),
                cursorBrush =
                    if (value.isEmpty()) SolidColor(Color.Transparent)
                    else SolidColor(colors.textPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions =
                    KeyboardActions(
                        onSend = {
                            if (value.isNotBlank() && enabled) {
                                onSend()
                            }
                        }
                    ),
                modifier =
                    Modifier.weight(1f)
                        .defaultMinSize(minHeight = 24.dp)
                        .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Ask anything",
                                fontFamily = InterFont,
                                fontSize = 16.sp,
                                color = colors.textSubtle,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.width(8.dp))

            val density = LocalDensity.current

            Box {
                Row(
                    modifier =
                        Modifier.height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.mediumGrey)
                            .clickable(enabled = !isGenerating && availableModels.isNotEmpty()) {
                                showModelDropdown = true
                            }
                            .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = selectedModel.icon,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedModel.displayName,
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                if (showModelDropdown) {
                    ModelSelectorDropdown(
                        onDismissRequest = { showModelDropdown = false },
                        selectedModel = selectedModel,
                        models = availableModels.map(::modelOption),
                        onModelSelected = { model ->
                            onModelSelected(model)
                            showModelDropdown = false
                        },
                        density = density,
                        colors = colors,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGenerating) {

                Box(
                    modifier =
                        Modifier.size(36.dp).clip(CircleShape).background(colors.error).clickable {
                            onStop()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop generation",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {

                Box(
                    modifier =
                        Modifier.size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (value.isNotBlank() && enabled) colors.textPrimary
                                else colors.mediumGrey
                            )
                            .clickable(enabled = value.isNotBlank() && enabled) { onSend() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint =
                            if (value.isNotBlank() && enabled) colors.background
                            else colors.textDim,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ModelSelectorDropdown(
    onDismissRequest: () -> Unit,
    selectedModel: ModelOption,
    models: List<ModelOption>,
    onModelSelected: (ModelOption) -> Unit,
    density: androidx.compose.ui.unit.Density,
    colors: LocalGrokColors = LocalAppColors.current,
) {

    val menuHeight = with(density) { (models.size * 56 + 16).coerceAtMost(320).dp.roundToPx() }

    val inputBarOffset = with(density) { 60.dp.roundToPx() }

    Popup(
        onDismissRequest = onDismissRequest,
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = -(menuHeight + inputBarOffset)),
        properties =
            PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                focusable = true,
            ),
    ) {
        Column(
            modifier =
                Modifier.width(260.dp)
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.darkGrey)
                    .padding(vertical = 8.dp)
        ) {
            models.forEachIndexed { index, model ->
                ModelDropdownItem(
                    model = model,
                    isSelected = model.id == selectedModel.id,
                    onClick = { onModelSelected(model) },
                    colors = colors,
                )

                if (index < models.size - 1) {
                    HorizontalDivider(
                        color = colors.borderGrey.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelDropdownItem(
    model: ModelOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: LocalGrokColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = model.icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(22.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.displayName,
                fontFamily = InterFont,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = model.subtitle,
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = colors.textSecondary,
            )
        }
    }
}
