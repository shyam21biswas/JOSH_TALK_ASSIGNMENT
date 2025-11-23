package com.example.josh



import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageDescriptionScreen(
    onTaskComplete: () -> Unit,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val product by viewModel.currentProduct.collectAsState()

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var savedAudioPath by remember { mutableStateOf<String?>(null) }  // ADD THIS
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var audioPlayer by remember { mutableStateOf<AudioPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val vibrate = rememberVibrator()

    LaunchedEffect(Unit) {
        viewModel.fetchProduct()
    }

    LaunchedEffect(product) {
        product?.let {
            imageUrl = if (it.images.isNotEmpty()) {
                it.images.first()
            }
                else  {
                it.thumbnail
            }


            }
        }


    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDuration < Constants.MAX_RECORDING_DURATION) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration >= Constants.MAX_RECORDING_DURATION && isRecording) {
                val path = audioRecorder?.stopRecording()
                audioRecorder = null
                isRecording = false

                if (path != null && recordingDuration >= Constants.MIN_RECORDING_DURATION) {
                    savedAudioPath = path
                    errorMessage = null
                } else {
                    errorMessage = "Failed to save recording"
                    audioPath = null
                    recordingDuration = 0
                }
            }
        }
    }

    // ADD CLEANUP
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder?.stopRecording()
            audioRecorder = null
            audioPlayer?.release()
            audioPlayer = null
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Image Description Task",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Describe what you see in your native language:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(vertical = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            //audio compnent.............................................
            if (savedAudioPath == null) {
                Spacer(modifier = Modifier.height(32.dp))

                if (!micPermission.status.isGranted) {
                    Button(
                        onClick = { micPermission.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Grant Microphone Permission", color = Color(0xFF667eea))
                        Toast.makeText(context, "Grant Microphone Permission", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    MicButton(
                        isRecording = isRecording,
                        recordingDuration = recordingDuration,
                        onStartRecording = {
                            vibrate()
                            errorMessage = null
                            recordingDuration = 0
                            audioRecorder = AudioRecorder(context)
                            val path = audioRecorder?.startRecording()
                            if (path != null) {

                                isRecording = true
                            } else {
                                errorMessage = "Failed to start recording"
                                audioRecorder = null
                            }
                        },
                        onStopRecording = {
                            vibrate()
                            if (isRecording) {
                                val path = audioRecorder?.stopRecording()
                                audioRecorder = null
                                isRecording = false

                                if (recordingDuration < Constants.MIN_RECORDING_DURATION) {
                                    errorMessage = "Recording too short (min ${Constants.MIN_RECORDING_DURATION} s)"
                                    audioPath = null
                                    savedAudioPath = null
                                    recordingDuration = 0
                                } else if (path == null) {
                                    errorMessage = "Failed to save recording"
                                    audioPath = null
                                    savedAudioPath = null
                                    recordingDuration = 0
                                } else {
                                    savedAudioPath = path
                                    errorMessage = null
                                }
                            }
                        }
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFF44336),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            } else {

                // UPDATED SUBMIT UI WITH AUDIO PLAYER
                Spacer(modifier = Modifier.height(16.dp))

                AudioPlaybackPlayer(
                    audioPath = savedAudioPath,
                    recordingDuration = recordingDuration,
                    context = context,
                    playerStyle = AudioPlayerStyle.COMPACT,

                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            audioPlayer?.release()
                            audioPlayer = null
                            isPlaying = false
                            audioPath = null
                            savedAudioPath = null
                            recordingDuration = 0
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Text("Record Again", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            audioPlayer?.release()
                            imageUrl?.let { url ->
                                viewModel.saveImageDescriptionTask(
                                    imageUrl = url,
                                    audioPath = savedAudioPath ?: "",
                                    durationSec = recordingDuration
                                )
                            }
                            onTaskComplete()
                            Toast.makeText(context, "Task Submit", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text("Submit", color = Color(0xFF667eea), fontSize = 16.sp)

                    }
                }
            }
        }
    }
}