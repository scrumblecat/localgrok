package com.localgrok.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.localgrok.ui.theme.AppTheme
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by
    preferencesDataStore(name = "localgrok_settings")

class SettingsRepository(private val context: Context) {

    private val preferences =
        context.dataStore.data.catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    companion object {
        private val SERVER_IP_KEY = stringPreferencesKey("server_ip")
        private val SERVER_PORT_KEY = intPreferencesKey("server_port")
        private val SEARXNG_PORT_KEY = intPreferencesKey("searxng_port")
        private val DEFAULT_MODEL_KEY = stringPreferencesKey("default_model")
        private val APP_THEME_KEY = stringPreferencesKey("app_theme")

        const val DEFAULT_PORT = 11434
        const val DEFAULT_SEARXNG_PORT = 8888
        const val DEFAULT_MODEL = "gemma3:1b-it-qat"
    }

    val serverIp: Flow<String> =
        preferences.map { preferences ->
            val ip = preferences[SERVER_IP_KEY]

            ip.orEmpty().trim()
        }

    val serverPort: Flow<Int> =
        preferences.map { preferences ->
            preferences[SERVER_PORT_KEY]?.takeIf { it in 1..65535 } ?: DEFAULT_PORT
        }

    val searxngPort: Flow<Int> =
        preferences.map { preferences ->
            preferences[SEARXNG_PORT_KEY]?.takeIf { it in 1..65535 } ?: DEFAULT_SEARXNG_PORT
        }

    val defaultModel: Flow<String> =
        preferences.map { preferences -> preferences[DEFAULT_MODEL_KEY] ?: DEFAULT_MODEL }

    val appTheme: Flow<AppTheme> =
        preferences.map { preferences ->
            val themeName = preferences[APP_THEME_KEY] ?: AppTheme.DARK.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.DARK
            }
        }

    suspend fun setServerConfig(host: String, port: Int, searchPort: Int) {
        com.localgrok.data.remote.ServerAddress.baseUrl(host, port)
        com.localgrok.data.remote.ServerAddress.baseUrl(host, searchPort)
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP_KEY] = host.trim()
            preferences[SERVER_PORT_KEY] = port
            preferences[SEARXNG_PORT_KEY] = searchPort
        }
    }

    suspend fun setDefaultModel(model: String) {
        context.dataStore.edit { preferences -> preferences[DEFAULT_MODEL_KEY] = model }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences -> preferences[APP_THEME_KEY] = theme.name }
    }

    suspend fun clearSettings() {
        context.dataStore.edit { preferences -> preferences.clear() }
    }
}
