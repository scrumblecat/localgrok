package com.localgrok.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.localgrok.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chats ORDER BY updatedAt DESC") fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Long): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatByIdFlow(chatId: Long): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertChat(chat: ChatEntity): Long

    @Update suspend fun updateChat(chat: ChatEntity)

    @Delete suspend fun deleteChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId") suspend fun deleteChatById(chatId: Long)

    @Query("UPDATE chats SET updatedAt = :timestamp WHERE id = :chatId")
    suspend fun updateChatTimestamp(chatId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE chats SET title = :title WHERE id = :chatId")
    suspend fun updateChatTitle(chatId: Long, title: String)

    @Query("DELETE FROM chats") suspend fun deleteAllChats()
}
