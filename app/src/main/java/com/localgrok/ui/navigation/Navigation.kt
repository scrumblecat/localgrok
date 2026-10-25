package com.localgrok.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localgrok.ui.screens.ChatScreen
import com.localgrok.ui.screens.SettingsScreen
import com.localgrok.ui.viewmodel.ChatViewModel
import com.localgrok.ui.viewmodel.SettingsViewModel

object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

@Composable
fun LocalGrokNavHost(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {

    var isNavigationInProgress by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = Routes.CHAT, modifier = modifier) {
        composable(Routes.CHAT) {
            isNavigationInProgress = false

            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = {
                    if (!isNavigationInProgress) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Routes.CHAT) {
                            isNavigationInProgress = true
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        }
                    }
                },
            )
        }

        composable(Routes.SETTINGS) {
            isNavigationInProgress = false

            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    if (!isNavigationInProgress) {

                        val canPop = navController.previousBackStackEntry != null
                        if (canPop) {
                            isNavigationInProgress = true

                            chatViewModel.refreshServerConnection()
                            navController.popBackStack()
                        }
                    }
                },
            )
        }
    }
}
