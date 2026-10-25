package com.example.ui.components

import android.graphics.Matrix
import android.net.Uri
import android.view.TextureView
import android.view.ViewOutlineProvider
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun LoopingVideoArtwork(
    videoPathOrUri: String,
    modifier: Modifier = Modifier,
    clipShape: Shape? = null
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

    val finalModifier = if (clipShape != null) modifier.clip(clipShape) else modifier

    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
                
                videoExoPlayer.setVideoTextureView(this)

                // Add video size listener for precise center crop scaling
                videoExoPlayer.addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        if (videoSize.width > 0 && videoSize.height > 0 && width > 0 && height > 0) {
                            applyCenterCropMatrix(this@apply, width, height, videoSize.width, videoSize.height)
                        }
                    }
                })

                addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                    val w = right - left
                    val h = bottom - top
                    val videoSize = videoExoPlayer.videoSize
                    if (w > 0 && h > 0 && videoSize.width > 0 && videoSize.height > 0) {
                        applyCenterCropMatrix(v as TextureView, w, h, videoSize.width, videoSize.height)
                    }
                }
            }
        },
        update = { textureView ->
            videoExoPlayer.setVideoTextureView(textureView)
        },
        modifier = finalModifier.fillMaxSize()
    )
}

private fun applyCenterCropMatrix(textureView: TextureView, viewWidth: Int, viewHeight: Int, videoWidth: Int, videoHeight: Int) {
    if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return
    val viewAspect = viewWidth.toFloat() / viewHeight.toFloat()
    val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
    var scaleX = 1f
    var scaleY = 1f
    if (videoAspect > viewAspect) {
        scaleX = videoAspect / viewAspect
    } else {
        scaleY = viewAspect / videoAspect
    }
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}



