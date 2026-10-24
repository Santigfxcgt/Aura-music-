package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_track_metadata")
data class CustomTrackMetadata(
    @PrimaryKey
    val trackId: String,
    val customTitle: String? = null,
    val customArtist: String? = null,
    val customAlbum: String? = null,
    val customMediaUri: String? = null,
    val mediaType: String = "IMAGE", // "IMAGE", "GIF", "VIDEO"
    val videoDurationMs: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis()
)
