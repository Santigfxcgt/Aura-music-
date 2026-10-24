package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CustomTrackMetadata::class, PlaylistEntity::class, PlaylistTrackCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class AuraMusicDatabase : RoomDatabase() {

    abstract fun customTrackMetadataDao(): CustomTrackMetadataDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AuraMusicDatabase? = null

        fun getInstance(context: Context): AuraMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraMusicDatabase::class.java,
                    "aura_music_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
