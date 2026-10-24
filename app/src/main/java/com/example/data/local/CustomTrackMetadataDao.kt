package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTrackMetadataDao {

    @Query("SELECT * FROM custom_track_metadata")
    fun getAllCustomMetadata(): Flow<List<CustomTrackMetadata>>

    @Query("SELECT * FROM custom_track_metadata WHERE trackId = :trackId")
    suspend fun getMetadataForTrack(trackId: String): CustomTrackMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(metadata: CustomTrackMetadata)

    @Query("DELETE FROM custom_track_metadata WHERE trackId = :trackId")
    suspend fun deleteForTrack(trackId: String)
}
