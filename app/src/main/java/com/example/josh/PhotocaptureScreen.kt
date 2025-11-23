package com.example.josh

import androidx.compose.material.icons.filled.Face



import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

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
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoCaptureScreen(
    onTaskComplete: () -> Unit,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Recording State
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var savedAudioPath by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Audio Tools
    var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }
    var audioPlayer by remember { mutableStateOf<AudioPlayer?>(null) }

    var imagePath by remember { mutableStateOf<String?>(null) }

    val imageFile = remember {
        File(context.getExternalFilesDir(null), "photo_${System.currentTimeMillis()}.jpg")
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri = Uri.fromFile(imageFile)
            imagePath = imageFile.absolutePath
        }
    }
    var vibrate = rememberVibrator()


    // Timer Logic
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
                    savedAudioPath = null
                    recordingDuration = 0
                }
            }
        }
    }

    // Cleanup
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
                text = "Photo Capture Task",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ---------------------------------------------------------
            // 1. CAMERA & IMAGE PREVIEW
            // ---------------------------------------------------------
            if (capturedImageUri == null) {
                // NO IMAGE YET
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No photo captured yet", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!cameraPermission.status.isGranted) {
                    Button(
                        onClick = { cameraPermission.launchPermissionRequest() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Grant Camera Permission", color = Color(0xFF667eea))
                    }
                } else {
                    Button(
                        onClick = {
                            try {
                                val authority = "${context.packageName}.fileprovider"
                                val uri = FileProvider.getUriForFile(context, authority, imageFile)
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                errorMessage = "Camera error: ${e.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF667eea))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture Image", color = Color(0xFF667eea), fontSize = 18.sp)
                    }
                }
            } else {
                // IMAGE CAPTURED - SHOW PREVIEW............................
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Describe the photo in your language:",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Image(
                            painter = rememberAsyncImagePainter(capturedImageUri),
                            contentDescription = "Captured Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }


                // 2. AUDIO RECORDING.......................................................................

                if (savedAudioPath == null) {
                    // RECORDING MODE
                    Spacer(modifier = Modifier.height(32.dp))

                    if (!micPermission.status.isGranted) {
                        Button(
                            onClick = { micPermission.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Grant Microphone Permission", color = Color(0xFF667eea))
                        }
                    } else {
                        MicButton(
                            isRecording = isRecording,
                            recordingDuration = recordingDuration,
                            onStartRecording = {
                                vibrate()
                                errorMessage = null
                                recordingDuration = 0
                                savedAudioPath = null
                                audioRecorder = AudioRecorder(context)
                                val path = audioRecorder?.startRecording()
                                if (path != null) isRecording = true
                                else errorMessage = "Failed to start recording"
                            },
                            onStopRecording = {
                                vibrate()
                                if (isRecording) {
                                    val path = audioRecorder?.stopRecording()
                                    audioRecorder = null
                                    isRecording = false

                                    if (recordingDuration < Constants.MIN_RECORDING_DURATION) {
                                        errorMessage = "Recording too short"
                                    } else if (path == null) {
                                        errorMessage = "Failed to save recording"
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

                    // 3. PLAYBACK & SUBMIT...........................................................................

                    Spacer(modifier = Modifier.height(16.dp))

                    AudioPlaybackPlayer(
                        audioPath = savedAudioPath,
                        recordingDuration = recordingDuration,
                        context = context,
                        playerStyle = AudioPlayerStyle.COMPACT,

                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- NEW BUTTON: RETAKE AUDIO ONLY ---
                    OutlinedButton(
                        onClick = {
                            // Logic to reset ONLY audio
                            audioPlayer?.release()
                            audioPlayer = null
                            // This triggers the UI to show MicButton again
                            savedAudioPath = null
                            recordingDuration = 0
                            errorMessage = null
                            // We DO NOT reset capturedImageUri here
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Text("Record Audio Again")
                    }
                    // -------------------------------------

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // RETAKE PHOTO (Resets EVERYTHING)
                        OutlinedButton(
                            onClick = {
                                audioPlayer?.release()
                                audioPlayer = null
                                // Reset Image AND Audio
                                capturedImageUri = null
                                imagePath = null
                                savedAudioPath = null
                                recordingDuration = 0
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Text("Retake Photo")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // SUBMIT
                        Button(
                            onClick = {
                                audioPlayer?.release()
                                audioPlayer = null
                                if (imagePath != null && savedAudioPath != null) {
                                    viewModel.savePhotoCaptureTask(

                                        imagePath = imagePath!!,
                                        audioPath = savedAudioPath!!,
                                        durationSec = recordingDuration
                                    )
                                    onTaskComplete()
                                    Toast.makeText(context, "Task Submit", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Submit", color = Color(0xFF667eea))

                        }
                    }
                }
            }
        }
    }
}