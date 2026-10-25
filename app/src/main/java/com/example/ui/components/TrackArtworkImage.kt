package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Track
import com.example.util.AuraImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object TrackArtworkMemoryCache {
    private val cache = LruCache<String, Bitmap>(50)

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
}

@Composable
fun TrackArtworkImage(
    track: Track,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // Case 1: Custom Media from Room (GIF or Image)
    if (!track.customMediaUri.isNullOrBlank() && (track.customMediaType == "GIF" || track.customMediaType == "IMAGE")) {
        val file = remember(track.customMediaUri) { File(track.customMediaUri) }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
            imageLoader = AuraImageLoader.get(context),
            contentDescription = track.title,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    // Case 2: Custom Media is a Video (for thumbnail view in lists / miniplayer)
    if (!track.customMediaUri.isNullOrBlank() && track.customMediaType == "VIDEO") {
        val cacheKey = "video_${track.customMediaUri}"
        var videoFrameBitmap by remember(track.customMediaUri) {
            mutableStateOf<Bitmap?>(TrackArtworkMemoryCache.get(cacheKey))
        }

        LaunchedEffect(track.customMediaUri) {
            if (videoFrameBitmap == null) {
                withContext(Dispatchers.IO) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(track.customMediaUri)
                        val frame = retriever.getFrameAtTime(500000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            ?: retriever.frameAtTime
                        if (frame != null) {
                            TrackArtworkMemoryCache.put(cacheKey, frame)
                            videoFrameBitmap = frame
                        }
                        retriever.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        if (videoFrameBitmap != null) {
            Image(
                bitmap = videoFrameBitmap!!.asImageBitmap(),
                contentDescription = track.title,
                contentScale = contentScale,
                modifier = modifier
            )
            return
        }
    }

    // Case 3: Embedded album art byte array
    val embeddedBitmap = remember(track.id, track.artworkByteArray) {
        track.artworkByteArray?.let { bytes ->
            val cacheKey = "embedded_${track.id}"
            val cached = TrackArtworkMemoryCache.get(cacheKey)
            if (cached != null) {
                cached.asImageBitmap()
            } else {
                try {
                    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (decoded != null) {
                        TrackArtworkMemoryCache.put(cacheKey, decoded)
                        decoded.asImageBitmap()
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    if (embeddedBitmap != null) {
        Image(
            bitmap = embeddedBitmap,
            contentDescription = track.title,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    // Case 4: Default fallback drawable or solid black if none
    if (track.artworkResId != 0) {
        Image(
            painter = painterResource(id = track.artworkResId),
            contentDescription = track.title,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color.Black)
        )
    }
}
