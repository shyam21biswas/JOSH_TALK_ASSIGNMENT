package com.example.josh


import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TextReadingScreen(
    onTaskComplete: () -> Unit,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val product by viewModel.currentProduct.collectAsState()

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }

    var checkbox1 by remember { mutableStateOf(false) }
    var checkbox2 by remember { mutableStateOf(false) }
    var checkbox3 by remember { mutableStateOf(false) }

    val allChecked = checkbox1 && checkbox2 && checkbox3

    LaunchedEffect(Unit) {
        viewModel.fetchProduct()
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDuration < Constants.MAX_RECORDING_DURATION) {
                delay(1000)
                recordingDuration++
            }
            if (recordingDuration >= Constants.MAX_RECORDING_DURATION) {
                audioRecorder?.stopRecording()
                isRecording = false
                errorMessage = "Recording too long (max ${Constants.MAX_RECORDING_DURATION} s)"
                audioPath = null
                recordingDuration = 0
            }
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
                text = "Text Reading Task",
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
                        text = "Read the passage aloud in your native language:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (product != null) {
                        Text(
                            text = product!!.description,
                            fontSize = 16.sp,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }

            if (audioPath == null) {
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
                            errorMessage = null
                            audioRecorder = AudioRecorder(context)
                            val path = audioRecorder?.startRecording()
                            if (path != null) {
                                isRecording = true
                                recordingDuration = 0
                            }
                        },
                        onStopRecording = {
                            audioRecorder?.stopRecording()
                            isRecording = false

                            if (recordingDuration < Constants.MIN_RECORDING_DURATION) {
                                errorMessage = "Recording too short (min ${Constants.MIN_RECORDING_DURATION} s)"
                                audioPath = null
                                recordingDuration = 0
                            } else {
                                audioPath = audioRecorder?.startRecording()
                                errorMessage = null
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
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recording Complete: ${recordingDuration}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checkbox1, onCheckedChange = { checkbox1 = it })
                            Text("No background noise", fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checkbox2, onCheckedChange = { checkbox2 = it })
                            Text("No mistakes while reading", fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checkbox3, onCheckedChange = { checkbox3 = it })
                            Text("Beech me koi galti nahi hai", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            audioPath = null
                            recordingDuration = 0
                            checkbox1 = false
                            checkbox2 = false
                            checkbox3 = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Text("Record Again")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            product?.let {
                                viewModel.saveTask(
                                    text = it.description,
                                    audioPath = audioPath ?: "",
                                    durationSec = recordingDuration
                                )
                            }
                            onTaskComplete()
                        },
                        enabled = allChecked,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Submit", color = if (allChecked) Color(0xFF667eea) else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MicButton(
    isRecording: Boolean,
    recordingDuration: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (isRecording) Color(0xFFF44336) else Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onStartRecording()
                            tryAwaitRelease()
                            onStopRecording()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Canvas(modifier = Modifier.size(140.dp * scale)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension / 2,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Microphone",
                modifier = Modifier.size(48.dp),
                tint = if (isRecording) Color.White else Color(0xFF667eea)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isRecording) "Recording: ${recordingDuration}s" else "Press & Hold to Record",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        if (!isRecording) {
            Text(
                text = "(10-20 seconds)",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}


// ... (Your existing imports)


