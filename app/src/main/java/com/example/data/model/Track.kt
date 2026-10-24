package com.example.data.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUriString: String? = null,
    val artworkResId: Int = 0,
    val artworkByteArray: ByteArray? = null,
    val customMediaUri: String? = null,
    val customMediaType: String? = null, // "IMAGE", "GIF", "VIDEO"
    val audioResId: Int = 0,
    val filePath: String? = null,
    val mimeType: String = "audio/flac",
    val codec: String = "FLAC",
    val sampleRateHz: Int = 96000,
    val bitDepth: Int = 24,
    val bitrateBps: Long = 0L,
    val isLossless: Boolean = true,
    val qualityFormat: String = "Lossless • FLAC • 24-bit • 96.0 kHz",
    val isLocalTrack: Boolean = false,
    val category: String = "Synthwave",
    val playCount: String = "128.4K",
    val primaryAccent: Long = 0xFFB056FE,
    val secondaryAccent: Long = 0xFF00E5FF,
    val lyrics: List<LyricLine> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Track
        if (id != other.id) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (durationMs != other.durationMs) return false
        if (contentUriString != other.contentUriString) return false
        if (artworkResId != other.artworkResId) return false
        if (customMediaUri != other.customMediaUri) return false
        if (customMediaType != other.customMediaType) return false
        if (audioResId != other.audioResId) return false
        if (filePath != other.filePath) return false
        if (mimeType != other.mimeType) return false
        if (codec != other.codec) return false
        if (sampleRateHz != other.sampleRateHz) return false
        if (bitDepth != other.bitDepth) return false
        if (bitrateBps != other.bitrateBps) return false
        if (isLossless != other.isLossless) return false
        if (qualityFormat != other.qualityFormat) return false
        if (isLocalTrack != other.isLocalTrack) return false
        if (category != other.category) return false
        if (playCount != other.playCount) return false
        if (primaryAccent != other.primaryAccent) return false
        if (secondaryAccent != other.secondaryAccent) return false
        if (lyrics != other.lyrics) return false
        if (artworkByteArray != null) {
            if (other.artworkByteArray == null) return false
            if (!artworkByteArray.contentEquals(other.artworkByteArray)) return false
        } else if (other.artworkByteArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + (customMediaUri?.hashCode() ?: 0)
        result = 31 * result + (customMediaType?.hashCode() ?: 0)
        return result
    }
}
