package com.localgrok.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys =
        [
            ForeignKey(
                entity = ChatEntity::class,
                parentColumns = ["id"],
                childColumns = ["chatId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["chatId"])],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isThinking: Boolean = false,
    @ColumnInfo(defaultValue = "") val reasoningContent: String = "",
    @ColumnInfo(defaultValue = "0") val toolUsed: Boolean = false,
    @ColumnInfo(defaultValue = "") val toolDisplayName: String = "",
)
