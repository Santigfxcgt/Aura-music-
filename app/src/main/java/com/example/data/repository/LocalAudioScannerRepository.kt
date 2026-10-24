package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.R
import com.example.data.model.Track
import com.example.util.AudioMetadataExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAudioScannerRepository(private val context: Context) {

    private val fallbackArtworks = listOf(
        R.drawable.album_neon_city,
        R.drawable.album_midnight_drive,
        R.drawable.album_cosmic_waves
    )

    suspend fun scanLocalAudioFiles(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                var index = 0
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawTitle = cursor.getString(titleColumn) ?: "Pista $id"
                    val rawArtist = cursor.getString(artistColumn) ?: "Artista desconocido"
                    val rawAlbum = cursor.getString(albumColumn) ?: "Álbum desconocido"
                    val durationMs = cursor.getLong(durationColumn)
                    val filePath = cursor.getString(dataColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Extract embedded album art & technical audio specifications
                    val techDetails = AudioMetadataExtractor.extract(
                        context = context,
                        uri = contentUri,
                        fallbackTitle = rawTitle,
                        fallbackArtist = rawArtist,
                        fallbackAlbum = rawAlbum,
                        fallbackMimeType = mimeType,
                        fallbackDurationMs = durationMs
                    )

                    val fallbackArt = fallbackArtworks[index % fallbackArtworks.size]
                    index++

                    val track = Track(
                        id = "local_$id",
                        title = techDetails.title,
                        artist = techDetails.artist,
                        album = techDetails.album,
                        durationMs = if (techDetails.durationMs > 0) techDetails.durationMs else durationMs,
                        contentUriString = contentUri.toString(),
                        artworkResId = fallbackArt,
                        artworkByteArray = techDetails.embeddedPicture,
                        audioResId = 0,
                        filePath = filePath,
                        mimeType = techDetails.mimeType,
                        codec = techDetails.codec,
                        sampleRateHz = techDetails.sampleRateHz,
                        bitDepth = techDetails.bitDepth,
                        bitrateBps = techDetails.bitrateBps,
                        isLossless = techDetails.isLossless,
                        qualityFormat = techDetails.qualityBadge,
                        isLocalTrack = true,
                        category = "Audio Local • ${techDetails.codec}",
                        playCount = "Local",
                        primaryAccent = if (techDetails.isLossless) 0xFF00E5FF else 0xFFB056FE,
                        secondaryAccent = if (techDetails.isLossless) 0xFFFFB800 else 0xFF00E5FF,
                        lyrics = emptyList()
                    )

                    tracks.add(track)
                }
            }
        } catch (e: Exception) {
            Log.e("LocalAudioScanner", "Error querying MediaStore: ${e.message}", e)
        }

        tracks
    }
}
