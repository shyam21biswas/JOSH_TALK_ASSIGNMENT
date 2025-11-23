package com.example.josh


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun TaskSelectionScreen(
    onTextReadingClick: () -> Unit,
    onImageDescriptionClick: () -> Unit,
    onPhotoCaptureClick: () -> Unit,
    onViewHistory: () -> Unit
) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select a Task",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            TaskCard(
                title = "Text Reading Task",
                paint = R.drawable.text_to_speech,
                enabled = true,
                onClick = onTextReadingClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            TaskCard(
                title = "Image Description Task",
                paint = R.drawable.image_search,
                enabled = true,
                onClick = onImageDescriptionClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            TaskCard(
                title = "Photo Capture Task",
                paint = R.drawable.camera,
                enabled = true,
                onClick = onPhotoCaptureClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_history_edu_24),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("View Task History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TaskCard(
    title: String,
    paint: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        ),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = paint),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (enabled) Color(0xFF667eea) else Color.Gray
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color(0xFF667eea) else Color.Gray
            )
        }
    }
}