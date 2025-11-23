package com.example.josh



import android.content.Context
import android.media.MediaPlayer
import java.io.File
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import android.media.AudioAttributes

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun playAudio(audioPath: String, onCompletion: () -> Unit) {
        try {
            stopAudio() // Stop any existing playback

            onCompletionListener = onCompletion

            val file = File(audioPath)
            if (!file.exists()) {
                onCompletion()
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioPath)
                prepare()
                setOnCompletionListener {
                    stopAudio()
                    onCompletionListener?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopAudio()
            onCompletion()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun release() {
        stopAudio()
        onCompletionListener = null
    }
}




//vibration when it click on button


object VibrationHelper {

    fun vibrate(context: Context, durationMs: Long = 50) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}

/**
 * A Composable hook that returns a lambda to trigger vibration.
 * * Usage:
 * val vibrate = rememberVibrator()
 * Button(onClick = { vibrate() }) { Text("Click") }
 */
@Composable
fun rememberVibrator(): () -> Unit {
    val context = LocalContext.current

    // We remember the lambda so it doesn't get recreated on every recomposition
    val vibrationLambda = remember(context) {
        {
            VibrationHelper.vibrate(context, durationMs = 50)
        }
    }

    return vibrationLambda
}

/**
 * A generic version if you want to customize duration in the UI.
 */
@Composable
fun rememberCustomVibrator(): (Long) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { durationMs ->
            VibrationHelper.vibrate(context, durationMs)
        }
    }
}

/**
 * Extension function: If you are outside a Composable but have Context.
 * Usage: context.vibrateShort()
 */
fun Context.vibrateShort(durationMs: Long = 50) {
    VibrationHelper.vibrate(this, durationMs)
}