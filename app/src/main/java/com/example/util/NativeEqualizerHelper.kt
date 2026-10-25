package com.example.util

import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

class NativeEqualizerHelper {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    private var areEffectsSupported: Boolean? = null

    private fun checkEffectsSupport(): Boolean {
        if (areEffectsSupported != null) return areEffectsSupported!!
        areEffectsSupported = try {
            val effects = AudioEffect.queryEffects()
            effects != null && effects.isNotEmpty()
        } catch (e: Exception) {
            false
        }
        return areEffectsSupported!!
    }

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (!checkEffectsSupport()) {
            return
        }

        try {
            release()

            try {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
            } catch (ignored: Exception) {
                equalizer = null
            }

            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = true
                }
            } catch (ignored: Exception) {
                bassBoost = null
            }

            try {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    enabled = true
                }
            } catch (ignored: Exception) {
                virtualizer = null
            }

            try {
                presetReverb = PresetReverb(0, audioSessionId).apply {
                    enabled = true
                }
            } catch (ignored: Exception) {
                presetReverb = null
            }
        } catch (ignored: Exception) {}
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            presetReverb?.enabled = enabled
        } catch (ignored: Exception) {}
    }

    fun setBassBoostStrength(strengthPercent: Int) {
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    val strength = ((strengthPercent / 100f) * 1000).toInt().coerceIn(0, 1000).toShort()
                    it.setStrength(strength)
                }
            }
        } catch (ignored: Exception) {}
    }

    fun setVirtualizerStrength(strengthPercent: Int) {
        try {
            virtualizer?.let {
                if (it.strengthSupported) {
                    val strength = ((strengthPercent / 100f) * 1000).toInt().coerceIn(0, 1000).toShort()
                    it.setStrength(strength)
                }
            }
        } catch (ignored: Exception) {}
    }

    fun setReverbPreset(presetName: String) {
        try {
            presetReverb?.let {
                val presetShort: Short = when (presetName.uppercase()) {
                    "SMALL_ROOM" -> PresetReverb.PRESET_SMALLROOM
                    "MEDIUM_HALL" -> PresetReverb.PRESET_MEDIUMHALL
                    "LARGE_HALL" -> PresetReverb.PRESET_LARGEHALL
                    "PLATE" -> PresetReverb.PRESET_PLATE
                    else -> PresetReverb.PRESET_NONE
                }
                it.preset = presetShort
            }
        } catch (ignored: Exception) {}
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        try {
            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                if (bandIndex in 0 until numBands) {
                    val bandShort = bandIndex.toShort()
                    val range = eq.bandLevelRange ?: shortArrayOf(-1500, 1500)
                    val minMilliBel = range[0]
                    val maxMilliBel = range[1]

                    val targetMilliBel = (gainDb * 100f).toInt().coerceIn(minMilliBel.toInt(), maxMilliBel.toInt()).toShort()
                    eq.setBandLevel(bandShort, targetMilliBel)
                }
            }
        } catch (ignored: Exception) {}
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()
        } catch (ignored: Exception) {} finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
            presetReverb = null
        }
    }
}
