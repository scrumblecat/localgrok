package com.localgrok.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localgrok.data.remote.ServerAddress
import com.localgrok.data.repository.ChatRepository
import com.localgrok.data.repository.SettingsRepository
import com.localgrok.ui.theme.AppTheme
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _appTheme = MutableStateFlow(AppTheme.DARK)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private var originalIp: String = ""
    private var originalPort: String = ""
    private var originalSearxngPort: String = ""

    init {
        loadSettings()
        loadTheme()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val ip = settingsRepository.serverIp.first()
            val port = settingsRepository.serverPort.first()
            val searxngPort = settingsRepository.searxngPort.first()

            originalIp = ip
            originalPort = port.toString()
            originalSearxngPort = searxngPort.toString()

            _uiState.value =
                _uiState.value.copy(
                    serverIp = ip,
                    serverPort = port.toString(),
                    searxngPort = searxngPort.toString(),
                    isDirty = false,
                )
        }
    }

    private fun loadTheme() {
        viewModelScope.launch {
            settingsRepository.appTheme.collect { theme -> _appTheme.value = theme }
        }
    }

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
            _appTheme.value = theme
        }
    }

    fun updateServerIp(ip: String) {
        _uiState.value =
            _uiState.value.copy(
                serverIp = ip,
                isDirty = checkIfDirty(ip, _uiState.value.serverPort, _uiState.value.searxngPort),
            )
    }

    fun updateServerPort(port: String) {
        _uiState.value =
            _uiState.value.copy(
                serverPort = port,
                isDirty = checkIfDirty(_uiState.value.serverIp, port, _uiState.value.searxngPort),
            )
    }

    fun updateSearxngPort(port: String) {
        _uiState.value =
            _uiState.value.copy(
                searxngPort = port,
                isDirty = checkIfDirty(_uiState.value.serverIp, _uiState.value.serverPort, port),
            )
    }

    private fun checkIfDirty(ip: String, port: String, searxngPort: String): Boolean {
        return ip != originalIp || port != originalPort || searxngPort != originalSearxngPort
    }

    fun saveSettings(showToast: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val host = state.serverIp.trim()
            val port = state.serverPort.trim().toIntOrNull()
            val searchPort = state.searxngPort.trim().toIntOrNull()
            if (
                port == null || searchPort == null || port !in 1..65535 || searchPort !in 1..65535
            ) {
                showToast("Enter port numbers between 1 and 65535.")
                return@launch
            }
            try {
                ServerAddress.baseUrl(host, port)
                ServerAddress.baseUrl(host, searchPort)
                settingsRepository.setServerConfig(host, port, searchPort)
                chatRepository.configureServer(host, port)
                chatRepository.configureSearxng(host, searchPort)
                originalIp = host
                originalPort = port.toString()
                originalSearxngPort = searchPort.toString()
                _uiState.value =
                    state.copy(
                        serverIp = host,
                        serverPort = originalPort,
                        searxngPort = originalSearxngPort,
                        isDirty = false,
                    )
                showToast("Settings saved.")
            } catch (error: IllegalArgumentException) {
                showToast(error.message ?: "Check your server address and ports.")
            } catch (error: IOException) {
                showToast("Could not save settings. Please try again.")
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _uiState.value.isDirty
    }
}

data class SettingsUiState(
    val serverIp: String = "",
    val serverPort: String = "11434",
    val searxngPort: String = "8888",
    val isDirty: Boolean = false,
)
