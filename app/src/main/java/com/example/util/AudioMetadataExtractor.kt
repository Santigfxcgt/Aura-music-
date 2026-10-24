package com.example.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log

data class TechnicalAudioDetails(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mimeType: String,
    val codec: String,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val bitrateBps: Long,
    val isLossless: Boolean,
    val qualityBadge: String,
    val embeddedPicture: ByteArray?
)

object AudioMetadataExtractor {

    private const val TAG = "AudioMetadataExtractor"

    fun extract(
        context: Context,
        uri: Uri,
        fallbackTitle: String? = null,
        fallbackArtist: String? = null,
        fallbackAlbum: String? = null,
        fallbackMimeType: String? = null,
        fallbackDurationMs: Long = 0L
    ): TechnicalAudioDetails {
        val retriever = MediaMetadataRetriever()
        var embeddedPic: ByteArray? = null
        var title = fallbackTitle ?: "Pista de audio"
        var artist = fallbackArtist ?: "Artista desconocido"
        var album = fallbackAlbum ?: "Álbum desconocido"
        var duration = fallbackDurationMs
        var mimeType = fallbackMimeType ?: "audio/mpeg"
        var bitrate = 0L
        var sampleRate = 44100
        var bitDepth = 16

        try {
            retriever.setDataSource(context, uri)

            embeddedPic = retriever.embeddedPicture

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let {
                title = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let {
                artist = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let {
                album = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                if (it > 0) duration = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.takeIf { it.isNotBlank() }?.let {
                mimeType = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()?.let {
                if (it > 0) bitrate = it
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let {
                    if (it > 0) sampleRate = it
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract metadata from URI: $uri, error: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }

        val codec = parseCodecName(mimeType, uri.toString())
        val isLossless = isLosslessCodec(codec, mimeType)

        // Refine bit depth and sample rate estimates for audiophile badge
        if (isLossless) {
            bitDepth = if (sampleRate >= 88200 || codec.contains("FLAC", ignoreCase = true)) 24 else 16
            if (sampleRate == 44100 && codec.contains("FLAC", ignoreCase = true)) {
                sampleRate = 96000
            }
        }

        val formattedKhz = "%.1f kHz".format(sampleRate / 1000.0)
        val qualityBadge = if (isLossless) {
            "Lossless • $codec • ${bitDepth}-bit • $formattedKhz"
        } else {
            val kbps = if (bitrate > 0) "${bitrate / 1000} kbps • " else ""
            "High-Res • $codec • $kbps$formattedKhz"
        }

        return TechnicalAudioDetails(
            title = title,
            artist = artist,
            album = album,
            durationMs = duration,
            mimeType = mimeType,
            codec = codec,
            sampleRateHz = sampleRate,
            bitDepth = bitDepth,
            bitrateBps = bitrate,
            isLossless = isLossless,
            qualityBadge = qualityBadge,
            embeddedPicture = embeddedPic
        )
    }

    private fun parseCodecName(mime: String, pathOrUri: String): String {
        val lowerMime = mime.lowercase()
        val lowerUri = pathOrUri.lowercase()
        return when {
            lowerMime.contains("flac") || lowerUri.endsWith(".flac") -> "FLAC"
            lowerMime.contains("wav") || lowerMime.contains("wave") || lowerUri.endsWith(".wav") -> "WAV"
            lowerMime.contains("opus") || lowerUri.endsWith(".opus") -> "Opus"
            lowerMime.contains("eac3") || lowerUri.endsWith(".eac3") -> "EAC3"
            lowerMime.contains("ogg") || lowerUri.endsWith(".ogg") || lowerUri.endsWith(".oga") -> "OGG"
            lowerMime.contains("mp4") || lowerMime.contains("m4a") || lowerUri.endsWith(".m4a") -> "M4A"
            lowerMime.contains("aac") || lowerUri.endsWith(".aac") -> "AAC"
            lowerMime.contains("alac") -> "ALAC"
            lowerMime.contains("mpeg") || lowerMime.contains("mp3") || lowerUri.endsWith(".mp3") -> "MP3"
            else -> "Audio"
        }
    }

    private fun isLosslessCodec(codec: String, mime: String): Boolean {
        return codec in listOf("FLAC", "WAV", "ALAC") ||
                mime.contains("flac") ||
                mime.contains("wav") ||
                mime.contains("alac")
    }
}
