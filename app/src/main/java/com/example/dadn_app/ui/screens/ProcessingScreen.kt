package com.example.dadn_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dadn_app.ui.theme.*
import com.example.dadn_app.ui.viewmodel.ProcessingUiState

@Composable
fun ProcessingScreen(
    imageUri: String? = null,
    uiState: ProcessingUiState = ProcessingUiState.Processing(0L),
    onDismissError: () -> Unit = {},
) {
    val isError = uiState is ProcessingUiState.Error
    val errorState = uiState as? ProcessingUiState.Error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OutlineVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomStart
        ) {
            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Image being processed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                )
            }

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(start = 16.dp, end = 24.dp, top = 20.dp, bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixedDim)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isError) "Analysis failed" else "Analyzing image...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isError) "AI-ENGINE:\nYOLOv11\nERROR STATE" else "AI-ENGINE:\nYLOv11",
                            fontSize = 10.sp,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                    if (isError) {
                        Text(
                            text = "ERR",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Error,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Primary,
                            strokeWidth = 3.dp,
                            trackColor = Primary.copy(alpha = 0.18f),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Title and Description
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isError) "PROCESSING ERROR" else "PROCESSING THREAD 04",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isError) Error else Primary,
                letterSpacing = 1.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isError) "Processing could not complete" else "Counting scaffolds...",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface,
            letterSpacing = (-1).sp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isError) {
                errorState?.message ?: "The image processing job failed."
            } else {
                "Neural network isolating structural segments\nand cross-referencing against safety\nparameters."
            },
            fontSize = 15.sp,
            color = OnSurfaceVariant,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(32.dp))

        if (isError) {
            ErrorStateCard(
                message = errorState?.message ?: "Processing failed.",
                timedOut = errorState?.timedOut == true,
                onDismiss = onDismissError,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StepCard(
                    state = StepState.Completed,
                    title = "SEGMENTATION",
                    description = "Geometric mesh generation complete",
                    extraText = null
                )

                StepCard(
                    state = StepState.Processing,
                    title = "QUANTIFICATION",
                    description = "Polling backend...",
                    extraText = processingEtaLabel(uiState)
                )

                StepCard(
                    state = StepState.Pending,
                    title = "VALIDATION",
                    description = "Pending data integrity check",
                    extraText = null
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

enum class StepState {
    Completed, Processing, Pending
}

@Composable
private fun ErrorStateCard(
    message: String,
    timedOut: Boolean,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ErrorContainer,
        border = BorderStroke(1.dp, Error.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error)
                Column {
                    Text(
                        text = if (timedOut) "Processing timeout" else "Processing error",
                        fontWeight = FontWeight.Bold,
                        color = Error,
                    )
                    Text(message, color = OnErrorContainer, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Back to Current Scan", color = OnError)
            }
        }
    }
}

private fun processingEtaLabel(uiState: ProcessingUiState): String {
    val elapsed = (uiState as? ProcessingUiState.Processing)?.elapsedMillis ?: 0L
    val seconds = (elapsed / 1000L).coerceAtLeast(0L)
    return " ${seconds}s"
}

@Composable
fun StepCard(state: StepState, title: String, description: String, extraText: String?) {
    val containerBg = when (state) {
        StepState.Completed -> Color(0xFFEAF2FF) // Light blue tint
        StepState.Processing -> Color.White
        StepState.Pending -> Color(0xFFF9FAFB) // Very light gray
    }
    
    val borderColor = when (state) {
        StepState.Processing -> Primary
        else -> Color.Transparent
    }

    val iconColor = when (state) {
        StepState.Completed -> PrimaryFixedDim
        StepState.Processing -> Primary
        StepState.Pending -> OutlineVariant
    }

    val iconBgColor = when (state) {
        StepState.Completed -> PrimaryFixedDim.copy(alpha = 0.2f)
        StepState.Processing -> Color.Transparent
        StepState.Pending -> OutlineVariant.copy(alpha = 0.2f)
    }

    val iconVector = when (state) {
        StepState.Completed -> Icons.Filled.Check
        StepState.Processing -> Icons.Filled.Refresh
        StepState.Pending -> Icons.Filled.MoreHoriz
    }

    val titleColor = when (state) {
        StepState.Pending -> OutlineVariant
        else -> OnSurfaceVariant
    }

    val descColor = when (state) {
        StepState.Pending -> OutlineVariant
        else -> OnSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .then(
                if (state == StepState.Processing) {
                    Modifier
                        .border(
                            width = 1.dp,
                            color = OutlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            drawLine(
                                color = Primary,
                                start = Offset(strokeWidth / 2, 0f),
                                end = Offset(strokeWidth / 2, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                } else Modifier
            )
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (state == StepState.Completed) iconColor else iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (state == StepState.Completed) Color.White else iconColor,
                    modifier = Modifier.size(if (state == StepState.Completed) 18.dp else 22.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = descColor,
                    )
                    if (extraText != null) {
                        Text(
                            text = extraText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProcessingScreenPreview() {
    Dadn_appTheme {
        ProcessingScreen()
    }
}
