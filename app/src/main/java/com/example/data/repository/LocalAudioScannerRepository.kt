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

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val rawTitle = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Pista $id"
                        val rawArtist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() } ?: "Artista desconocido"
                        val rawAlbum = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() } ?: "Álbum desconocido"
                        val durationMs = cursor.getLong(durationColumn)
                        val filePath = cursor.getString(dataColumn)
                        val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"

                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val details = AudioMetadataExtractor.extract(
                            context = context,
                            uri = contentUri,
                            fallbackTitle = rawTitle,
                            fallbackArtist = rawArtist,
                            fallbackAlbum = rawAlbum,
                            fallbackMimeType = mimeType,
                            fallbackDurationMs = durationMs
                        )

                        val track = Track(
                            id = "local_$id",
                            title = details.title,
                            artist = details.artist,
                            album = details.album,
                            durationMs = details.durationMs,
                            contentUriString = contentUri.toString(),
                            artworkResId = 0,
                            artworkByteArray = details.embeddedPicture,
                            audioResId = 0,
                            filePath = filePath,
                            mimeType = details.mimeType,
                            codec = details.codec,
                            sampleRateHz = details.sampleRateHz,
                            bitDepth = details.bitDepth,
                            bitrateBps = details.bitrateBps,
                            isLossless = details.isLossless,
                            qualityFormat = details.qualityBadge,
                            isLocalTrack = true,
                            category = "Audio Local • ${details.codec}",
                            playCount = "Local",
                            primaryAccent = if (details.isLossless) 0xFF00E5FF else 0xFFB056FE,
                            secondaryAccent = if (details.isLossless) 0xFFFFB800 else 0xFF00E5FF,
                            lyrics = emptyList()
                        )

                        tracks.add(track)
                    } catch (rowEx: Exception) {
                        Log.w("LocalAudioScanner", "Skipping problematic local track row: ${rowEx.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocalAudioScanner", "Error querying MediaStore: ${e.message}", e)
        }

        tracks
    }

    private fun parseCodec(mime: String, path: String?): String {
        val lowerMime = mime.lowercase()
        val lowerPath = path?.lowercase() ?: ""
        return when {
            lowerMime.contains("flac") || lowerPath.endsWith(".flac") -> "FLAC"
            lowerMime.contains("wav") || lowerMime.contains("wave") || lowerPath.endsWith(".wav") -> "WAV"
            lowerMime.contains("opus") || lowerPath.endsWith(".opus") -> "Opus"
            lowerMime.contains("ogg") || lowerPath.endsWith(".ogg") || lowerPath.endsWith(".oga") -> "OGG"
            lowerMime.contains("m4a") || lowerPath.endsWith(".m4a") -> "M4A"
            lowerMime.contains("aac") || lowerPath.endsWith(".aac") -> "AAC"
            lowerMime.contains("mp3") || lowerPath.endsWith(".mp3") -> "MP3"
            else -> "Audio"
        }
    }

    suspend fun scanFolderFiles(treeUri: Uri): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        try {
            val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
            if (docFile != null && docFile.isDirectory) {
                scanDocumentFileRecursive(docFile, tracks)
            }
        } catch (e: Exception) {
            Log.e("LocalAudioScanner", "Error scanning folder URI: ${e.message}", e)
        }
        tracks
    }

    private fun scanDocumentFileRecursive(dir: androidx.documentfile.provider.DocumentFile, tracks: MutableList<Track>) {
        val files = dir.listFiles()
        var index = tracks.size
        for (file in files) {
            try {
                if (file.isDirectory) {
                    scanDocumentFileRecursive(file, tracks)
                } else if (file.isFile) {
                    val mimeType = file.type?.lowercase() ?: ""
                    val name = file.name?.lowercase() ?: ""
                    if (mimeType.startsWith("audio/") || name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".opus")) {
                        val uri = file.uri
                        val rawTitle = file.name ?: "Pista"

                        val details = AudioMetadataExtractor.extract(
                            context = context,
                            uri = uri,
                            fallbackTitle = rawTitle.substringBeforeLast('.'),
                            fallbackArtist = "Carpeta Local",
                            fallbackAlbum = dir.name ?: "Álbum",
                            fallbackMimeType = mimeType,
                            fallbackDurationMs = 0L
                        )

                        val track = Track(
                            id = "folder_${file.uri.hashCode()}",
                            title = details.title,
                            artist = details.artist,
                            album = details.album,
                            durationMs = details.durationMs,
                            contentUriString = uri.toString(),
                            artworkResId = 0,
                            artworkByteArray = details.embeddedPicture,
                            audioResId = 0,
                            filePath = file.uri.toString(),
                            mimeType = details.mimeType,
                            codec = details.codec,
                            sampleRateHz = details.sampleRateHz,
                            bitDepth = details.bitDepth,
                            bitrateBps = details.bitrateBps,
                            isLossless = details.isLossless,
                            qualityFormat = details.qualityBadge,
                            isLocalTrack = true,
                            category = "Carpeta • ${details.codec}",
                            playCount = "Local",
                            primaryAccent = if (details.isLossless) 0xFF00E5FF else 0xFFB056FE,
                            secondaryAccent = if (details.isLossless) 0xFFFFB800 else 0xFF00E5FF,
                            lyrics = emptyList()
                        )
                        tracks.add(track)
                    }
                }
            } catch (ignored: Exception) {}
        }
    }
}
