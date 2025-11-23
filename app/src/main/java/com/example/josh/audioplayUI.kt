package com.example.josh




import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

/**
 * Standalone Audio Player Component
 * Can be used across different screens for audio playback
 */
@Composable
fun AudioPlaybackPlayer(
    audioPath: String?,
    recordingDuration: Int,
    context: Context,
    modifier: Modifier = Modifier,

    playerStyle: AudioPlayerStyle = AudioPlayerStyle.COMPACT
) {
    var isPlaying by remember { mutableStateOf(false) }
    var audioPlayer by remember { mutableStateOf<AudioPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer?.release()
            audioPlayer = null
        }
    }

    when (playerStyle) {

        //in future if i want add more styles i can add them here....


        AudioPlayerStyle.COMPACT -> CompactAudioPlayer(
            audioPath = audioPath,
            recordingDuration = recordingDuration,
            isPlaying = isPlaying,
            onPlayPause = {
                if (isPlaying) {
                    audioPlayer?.stopAudio()
                    audioPlayer = null
                    isPlaying = false
                } else {
                    audioPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            audioPlayer = AudioPlayer(context)
                            audioPlayer?.playAudio(path) {
                                isPlaying = false
                            }
                            isPlaying = true
                        }
                    }
                }
            },
            modifier = modifier
        )


    }
}

enum class AudioPlayerStyle {
       COMPACT
}



// ============================================
// COMPACT STYLE - Clean and Simple
// ============================================
@Composable
private fun CompactAudioPlayer(
    audioPath: String?,
    recordingDuration: Int, // In seconds (e.g., 20)
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    // We track time in Milliseconds internally for smooth updates
    // recordingDuration * 1000 converts seconds to ms
    val totalDurationMs = remember(recordingDuration) { recordingDuration * 1000L }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // Reset if starting over (optional, depends on if you want pause or stop behavior)
            if (progress >= 1f) {
                currentPositionMs = 0L
                progress = 0f
            }

            val updateInterval = 50L // Update every 50ms for smoother UI

            while (isPlaying && currentPositionMs < totalDurationMs) {
                delay(updateInterval)
                currentPositionMs += updateInterval

                // Calculate progress: Current MS / Total MS
                progress = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            }

            // Auto-stop when finished
            if (currentPositionMs >= totalDurationMs) {
                onPlayPause() // Trigger the parent to stop
                currentPositionMs = 0L
                progress = 0f
            }
        } else {
            // If stopped/paused, you might want to reset or keep position.
            // Your original code reset it, so I will keep that behavior:
            currentPositionMs = 0L
            progress = 0f
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play button
            FloatingActionButton(
                onClick = onPlayPause,
                containerColor = Color(0xFF667eea),
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = if (isPlaying)  painterResource(R.drawable.pause) else painterResource(R.drawable.baseline_play_arrow_24),
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info and progress
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    StatusBadge(isPlaying = isPlaying)

                    // Calculate seconds for display
                    val currentSeconds = (currentPositionMs / 1000).toInt()

                    Text(
                        text = if (isPlaying)
                            "$currentSeconds s / ${recordingDuration} s"
                        else
                            "${recordingDuration} s",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Color(0xFF667eea),
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))


        }
    }
}


@Composable
private fun StatusBadge(isPlaying: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isPlaying) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
    ) {
        Text(
            text = if (isPlaying) "Playing" else "Ready",
            fontSize = 12.sp,
            color = if (isPlaying) Color(0xFF4CAF50) else Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}