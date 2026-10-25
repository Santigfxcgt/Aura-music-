package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.R
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object NotificationArtworkHelper {

    /**
     * Extracts a high-quality static JPEG ByteArray for a given track,
     * suitable for MediaMetadata.setArtworkData(...) in MediaSession / System Notifications.
     * Handles Video covers (frame extraction), GIF/Image covers, embedded audio art, and resource fallbacks.
     */
    suspend fun extractArtworkByteArray(context: Context, track: Track): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // Case 1: Custom Video Cover Art -> Extract static video frame
            if (!track.customMediaUri.isNullOrBlank() && track.customMediaType == "VIDEO") {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(track.customMediaUri)
                    val frame = retriever.getFrameAtTime(1000000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    if (frame != null) {
                        return@withContext bitmapToByteArray(frame)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try { retriever.release() } catch (ignored: Exception) {}
                }
            }

            // Case 2: Custom Image or GIF Cover Art -> Load file bitmap
            if (!track.customMediaUri.isNullOrBlank() && (track.customMediaType == "GIF" || track.customMediaType == "IMAGE")) {
                try {
                    val file = File(track.customMediaUri)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            return@withContext bitmapToByteArray(bitmap)
                        }
                    } else if (track.customMediaUri.startsWith("content://") || track.customMediaUri.startsWith("file://")) {
                        val inputStream = context.contentResolver.openInputStream(Uri.parse(track.customMediaUri))
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bitmap != null) {
                            return@withContext bitmapToByteArray(bitmap)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Case 3: Embedded Artwork ByteArray
            if (track.artworkByteArray != null && track.artworkByteArray.isNotEmpty()) {
                return@withContext track.artworkByteArray
            }

            // Case 4: Fallback Drawable Resource
            val resId = if (track.artworkResId != 0) track.artworkResId else R.drawable.album_neon_city
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            if (bitmap != null) {
                return@withContext bitmapToByteArray(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        // Resize if bitmap is excessively large to keep MediaMetadata payload light & fast
        val maxDimension = 800
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        return byteArray
    }
}
