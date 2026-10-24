package com.example.data.repository

import android.content.Context
import com.example.data.local.AuraMusicDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PlaylistRepository(context: Context) {

    private val playlistDao = AuraMusicDatabase.getInstance(context).playlistDao()

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): String {
        val id = UUID.randomUUID().toString()
        val playlist = PlaylistEntity(
            id = id,
            name = name,
            createdAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        return id
    }

    suspend fun deletePlaylist(playlistId: String) {
        playlistDao.clearPlaylistTracks(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String, order: Int = 0) {
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            trackOrder = order
        )
        playlistDao.insertPlaylistTrack(crossRef)
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getPlaylistTrackIds(playlistId: String): Flow<List<String>> {
        return playlistDao.getPlaylistTrackIds(playlistId)
    }
}
