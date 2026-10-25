package com.localgrok.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.localgrok.data.local.dao.ChatDao
import com.localgrok.data.local.dao.MessageDao
import com.localgrok.data.local.entity.ChatEntity
import com.localgrok.data.local.entity.MessageEntity

@Database(entities = [ChatEntity::class, MessageEntity::class], version = 4, exportSchema = false)
abstract class LocalGrokDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile private var INSTANCE: LocalGrokDatabase? = null

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE messages ADD COLUMN isThinking INTEGER NOT NULL DEFAULT 0"
                    )
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE messages ADD COLUMN reasoningContent TEXT NOT NULL DEFAULT ''"
                    )
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE messages ADD COLUMN toolUsed INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE messages ADD COLUMN toolDisplayName TEXT NOT NULL DEFAULT ''"
                    )
                }
            }

        fun getDatabase(context: Context): LocalGrokDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    INSTANCE?.let {
                        return@synchronized it
                    }
                    val instance =
                        Room.databaseBuilder(
                                context.applicationContext,
                                LocalGrokDatabase::class.java,
                                "localgrok_database",
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .addCallback(
                                object : RoomDatabase.Callback() {
                                    override fun onOpen(db: SupportSQLiteDatabase) {
                                        super.onOpen(db)
                                        db.execSQL(
                                            "UPDATE messages SET isStreaming = 0, isThinking = 0 WHERE isStreaming = 1 OR isThinking = 1"
                                        )
                                    }
                                }
                            )
                            .build()
                    INSTANCE = instance
                    instance
                }
        }
    }
}
