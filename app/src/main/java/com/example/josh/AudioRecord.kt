package com.example.josh


import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): String? {
        return try {
            val fileName = "audio_${System.currentTimeMillis()}.m4a"
            outputFile = File(context.getExternalFilesDir(null), fileName)

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            outputFile?.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAmplitude(): Int {
        return recorder?.maxAmplitude ?: 0
    }
}