package com.localgrok.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.ui.theme.AppTheme
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.localgrok.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val focusManager = LocalFocusManager.current
    val portFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    val colors = LocalAppColors.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadSettings() }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    var isBackNavigating by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (!isBackNavigating) {
            if (viewModel.hasUnsavedChanges()) {
                showUnsavedDialog = true
            } else {
                isBackNavigating = true
                onNavigateBack()
            }
        }
    }

    BackHandler { handleBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Save",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = if (uiState.isDirty) colors.accent else colors.textDim,
                        modifier =
                            Modifier.clickable(enabled = uiState.isDirty) {
                                    viewModel.saveSettings { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = colors.darkGrey,
                        contentColor = colors.textPrimary,
                    )
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                SectionHeader(title = "SERVER")

                Spacer(modifier = Modifier.height(16.dp))

                GrokTextField(
                    label = "Server IP Address",
                    value = uiState.serverIp,
                    onValueChange = { viewModel.updateServerIp(it) },
                    placeholder = "127.0.0.1",
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                    onImeAction = { portFocusRequester.requestFocus() },
                )

                Spacer(modifier = Modifier.height(16.dp))

                GrokTextField(
                    label = "Ollama Port",
                    value = uiState.serverPort,
                    onValueChange = { viewModel.updateServerPort(it) },
                    placeholder = "11434",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    onImeAction = {},
                    focusRequester = portFocusRequester,
                )

                Spacer(modifier = Modifier.height(16.dp))

                GrokTextField(
                    label = "SearXNG Port (Web Search)",
                    value = uiState.searxngPort,
                    onValueChange = { viewModel.updateSearxngPort(it) },
                    placeholder = "8080",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "SearXNG provides web search capabilities. Uses same server IP as Ollama.",
                    fontFamily = InterFont,
                    fontSize = 12.sp,
                    color = colors.textSubtle,
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionHeader(title = "APPEARANCE")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Theme",
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ThemeOptionButton(
                        label = "Dark",
                        isSelected = appTheme == AppTheme.DARK,
                        onClick = { viewModel.setAppTheme(AppTheme.DARK) },
                        colors = colors,
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOptionButton(
                        label = "Space",
                        isSelected = appTheme == AppTheme.SPACE,
                        onClick = { viewModel.setAppTheme(AppTheme.SPACE) },
                        colors = colors,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                SectionHeader(title = "ABOUT")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "localgrok",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Privacy-focused AI client for local LLM servers",
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your conversations never leave your local network.",
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    color = colors.textSubtle,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Made with ❤️ by Scrumble",
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    color = colors.textSubtle,
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier.align(Alignment.TopCenter).padding(horizontal = 20.dp, vertical = 8.dp),
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = colors.darkGrey,
                    contentColor = colors.textPrimary,
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }

        if (showUnsavedDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedDialog = false },
                containerColor = colors.darkGrey,
                title = {
                    Text(
                        text = "Unsaved Changes",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                    )
                },
                text = {
                    Text(
                        text = "You have unsaved changes. Do you want to discard them?",
                        fontFamily = InterFont,
                        color = colors.textSecondary,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (!isBackNavigating) {
                                isBackNavigating = true
                                viewModel.loadSettings()
                                showUnsavedDialog = false
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text(text = "Discard", fontFamily = InterFont, color = colors.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(
                            text = "Keep Editing",
                            fontFamily = InterFont,
                            color = colors.textPrimary,
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Text(
        text = title,
        fontFamily = InterFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = colors.textSubtle,
        letterSpacing = 2.5.sp,
        modifier = modifier,
    )
}

@Composable
fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) colors.lightGrey else colors.darkGrey
    val borderColor =
        if (isSelected) {
            Color.White
        } else {
            colors.borderGrey.copy(alpha = 0.5f)
        }
    val textColor = if (isSelected) colors.textPrimary else colors.textSecondary

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = InterFont,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
            color = textColor,
        )
    }
}

@Composable
fun GrokTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier) {
        Text(text = label, fontFamily = InterFont, fontSize = 13.sp, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontFamily = InterFont,
                    fontSize = 15.sp,
                ),
            cursorBrush = SolidColor(colors.textPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            modifier =
                Modifier.fillMaxWidth()
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.darkGrey)
                    .border(
                        width = 1.dp,
                        color =
                            if (value.isNotEmpty()) colors.borderGrey
                            else colors.borderGrey.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = InterFont,
                        fontSize = 15.sp,
                        color = colors.textDim,
                    )
                }
                innerTextField()
            },
        )
    }
}
