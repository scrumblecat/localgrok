package com.localgrok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.localgrok.ui.navigation.LocalGrokNavHost
import com.localgrok.ui.theme.LocalGrokTheme
import com.localgrok.ui.viewmodel.ChatViewModel
import com.localgrok.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val app = application as LocalGrokApplication

        val viewModelFactory = viewModelFactory {
            initializer {
                ChatViewModel(
                    chatRepository = app.chatRepository,
                    settingsRepository = app.settingsRepository,
                )
            }
            initializer {
                SettingsViewModel(
                    settingsRepository = app.settingsRepository,
                    chatRepository = app.chatRepository,
                )
            }
        }

        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            val appTheme by settingsViewModel.appTheme.collectAsState()

            LocalGrokTheme(appTheme = appTheme) {
                LocalGrokNavHost(
                    chatViewModel = chatViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
