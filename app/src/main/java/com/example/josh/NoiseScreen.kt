package com.example.josh

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NoiseTestScreen(onTestPassed: () -> Unit) {
    val context = LocalContext.current
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var isTesting by remember { mutableStateOf(false) }
    var currentDb by remember { mutableStateOf(0) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var audioRecorder by remember { mutableStateOf<AudioRecorder?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(isTesting) {
        if (isTesting) {
            audioRecorder = AudioRecorder(context)
            audioRecorder?.startRecording()

            repeat(30) {
                val amplitude = audioRecorder?.getAmplitude() ?: 0
                currentDb = (amplitude / 327.67).toInt().coerceIn(0, 60)
                delay(100)
            }

            audioRecorder?.stopRecording()
            isTesting = false

            testResult = if (currentDb < Constants.NOISE_THRESHOLD) {
                "passed"
            } else {
                "failed"
            }
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Noise Test",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = if (isTesting) size.minDimension / 2 * scale else size.minDimension / 2

                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = radius,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }

                Text(
                    text = "$currentDb dB",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Range: 0 - 60 dB",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            when (testResult) {
                "passed" -> {
                    Text(
                        text = "✅ Good to proceed",
                        fontSize = 20.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onTestPassed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "failed" -> {
                    Text(
                        text = "❌ Please move to a quieter place",
                        fontSize = 18.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            testResult = null
                            currentDb = 0
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text("Test Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                null -> {
                    if (!micPermission.status.isGranted) {
                        Button(
                            onClick = { micPermission.launchPermissionRequest() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Text("Grant Microphone Permission", fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = { isTesting = true },
                            enabled = !isTesting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (isTesting) "Testing..." else "Start Test",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}