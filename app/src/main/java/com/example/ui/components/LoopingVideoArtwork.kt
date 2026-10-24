package com.example.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun LoopingVideoArtwork(
    videoPathOrUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val videoExoPlayer = remember(videoPathOrUri) {
        ExoPlayer.Builder(context).build().apply {
            val uri = if (videoPathOrUri.startsWith("content://") || videoPathOrUri.startsWith("file://") || videoPathOrUri.startsWith("http")) {
                Uri.parse(videoPathOrUri)
            } else {
                Uri.fromFile(File(videoPathOrUri))
            }
            setMediaItem(MediaItem.fromUri(uri))
            volume = 0f // Muted
            repeatMode = Player.REPEAT_MODE_ONE // Looping
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(videoExoPlayer) {
        onDispose {
            videoExoPlayer.stop()
            videoExoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = videoExoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
