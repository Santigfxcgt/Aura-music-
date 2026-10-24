package com.example.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.data.local.AuraMusicDatabase
import com.example.data.local.CustomTrackMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CustomMetadataRepository(private val context: Context) {

    private val dao = AuraMusicDatabase.getInstance(context).customTrackMetadataDao()

    val allCustomMetadata: Flow<List<CustomTrackMetadata>> = dao.getAllCustomMetadata()

    suspend fun getMetadataForTrack(trackId: String): CustomTrackMetadata? = withContext(Dispatchers.IO) {
        dao.getMetadataForTrack(trackId)
    }

    suspend fun saveTrackMetadata(
        trackId: String,
        title: String?,
        artist: String?,
        album: String?,
        mediaUri: Uri?
    ): Result<CustomTrackMetadata> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getMetadataForTrack(trackId)
            var finalMediaUri = existing?.customMediaUri
            var mediaType = existing?.mediaType ?: "IMAGE"
            var videoDuration = existing?.videoDurationMs ?: 0L

            if (mediaUri != null) {
                val uriStr = mediaUri.toString().lowercase()
                val mimeType = try {
                    context.contentResolver.getType(mediaUri)
                        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(mediaUri.toString())
                        ) ?: ""
                } catch (e: Exception) { "" }

                val isVideo = mimeType.startsWith("video/") ||
                        uriStr.endsWith(".mp4") || uriStr.endsWith(".mkv") || uriStr.endsWith(".webm")
                val isGif = mimeType.contains("gif", ignoreCase = true) || uriStr.endsWith(".gif")

                if (isVideo) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        if (mediaUri.scheme == "content" || mediaUri.scheme == "file") {
                            retriever.setDataSource(context, mediaUri)
                        } else {
                            retriever.setDataSource(mediaUri.path ?: mediaUri.toString())
                        }
                        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val durationMs = durationStr?.toLongOrNull() ?: 0L

                        if (durationMs > 10500L) { // Allow slight buffer (up to 10.5s)
                            val durationSec = durationMs / 1000.0
                            return@withContext Result.failure(
                                IllegalArgumentException("El vídeo seleccionado dura %.1f s. El límite para la portada es de 10 segundos.".format(durationSec))
                            )
                        }
                        videoDuration = durationMs
                        mediaType = "VIDEO"
                    } catch (e: IllegalArgumentException) {
                        return@withContext Result.failure(e)
                    } catch (e: Exception) {
                        mediaType = "VIDEO"
                        videoDuration = 0L
                    } finally {
                        try {
                            retriever.release()
                        } catch (ignored: Exception) {}
                    }
                } else if (isGif) {
                    mediaType = "GIF"
                    videoDuration = 0L
                } else {
                    mediaType = "IMAGE"
                    videoDuration = 0L
                }

                // Copy media to internal storage so the app retains persistent access
                val internalDir = File(context.filesDir, "custom_artworks").apply { mkdirs() }
                val ext = when {
                    isVideo -> "mp4"
                    isGif -> "gif"
                    else -> "jpg"
                }
                val destFile = File(internalDir, "cover_${trackId}_${System.currentTimeMillis()}.$ext")

                var copySuccess = false
                try {
                    val inputStream = if (mediaUri.scheme == "content" || mediaUri.scheme == "file") {
                        context.contentResolver.openInputStream(mediaUri)
                    } else {
                        val localFile = File(mediaUri.path ?: mediaUri.toString())
                        if (localFile.exists()) java.io.FileInputStream(localFile) else null
                    }

                    inputStream?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                        copySuccess = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (copySuccess && destFile.exists()) {
                    // Delete previous file if existed and different
                    existing?.customMediaUri?.let { oldPath ->
                        val oldFile = File(oldPath)
                        if (oldFile.exists() && oldFile.absolutePath != destFile.absolutePath) {
                            oldFile.delete()
                        }
                    }
                    finalMediaUri = destFile.absolutePath
                } else {
                    // Fallback to mediaUri string representation if copy fails
                    finalMediaUri = mediaUri.toString()
                }
            }

            val metadata = CustomTrackMetadata(
                trackId = trackId,
                customTitle = title?.takeIf { it.isNotBlank() },
                customArtist = artist?.takeIf { it.isNotBlank() },
                customAlbum = album?.takeIf { it.isNotBlank() },
                customMediaUri = finalMediaUri,
                mediaType = mediaType,
                videoDurationMs = videoDuration,
                lastUpdated = System.currentTimeMillis()
            )

            dao.insertOrUpdate(metadata)
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetToOriginal(trackId: String) = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataForTrack(trackId)
        existing?.customMediaUri?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        dao.deleteForTrack(trackId)
    }
}
