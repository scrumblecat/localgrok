package com.localgrok

import android.app.Application
import com.localgrok.data.local.LocalGrokDatabase
import com.localgrok.data.repository.ChatRepository
import com.localgrok.data.repository.SettingsRepository

class LocalGrokApplication : Application() {

    val database: LocalGrokDatabase by lazy { LocalGrokDatabase.getDatabase(this) }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(chatDao = database.chatDao(), messageDao = database.messageDao())
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
