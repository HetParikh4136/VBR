package com.archy.vbr.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VolumeController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    } catch (e: Exception) {
        null
    }

    fun stepVolume(direction: Int, showUI: Boolean = true, haptic: Boolean = true) {
        val flags = if (showUI) AudioManager.FLAG_SHOW_UI else 0
        audioManager.adjustSuggestedStreamVolume(direction, AudioManager.USE_DEFAULT_STREAM_TYPE, flags)

        if (haptic) {
            triggerTick()
        }
    }

    fun setVolumeFraction(fraction: Float, showUI: Boolean = true) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (fraction.coerceIn(0f, 1f) * max).toInt()
        val flags = if (showUI) AudioManager.FLAG_SHOW_UI else 0
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, flags)
    }

    fun getCurrentVolumeFraction(): Float {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return if (max > 0) current.toFloat() / max else 0f
    }

    private fun triggerTick() {
        try {
            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(10)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors if permission or hardware not available
        }
    }
}
