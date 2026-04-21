package com.example.dadn_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dadn_app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(
    onProcessingComplete: () -> Unit = {},
) {
    // TEMP TESTING ONLY:
    // Replace this fake 10-second wait with the real server upload / processing
    // callback when backend image processing is implemented.
    LaunchedEffect(Unit) {
        delay(10_000L)
        onProcessingComplete()
    }
    // END TEMP TESTING ONLY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Image Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OutlineVariant.copy(alpha = 0.5f)), // Placeholder for scaffold image
            contentAlignment = Alignment.BottomStart
        ) {
            // Semi-transparent overlay card
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
                            text = "Analyzing image...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "AI-ENGINE:\nKINETIC_SLATE_V4",
                            fontSize = 10.sp,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                    Text(
                        text = "64%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
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
                text = "PROCESSING THREAD 04",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
                letterSpacing = 1.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Counting scaffolds...",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface,
            letterSpacing = (-1).sp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Neural network isolating structural segments\nand cross-referencing against safety\nparameters.",
            fontSize = 15.sp,
            color = OnSurfaceVariant,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(32.dp))

        // Stepper
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Step 1: SEGMENTATION (Completed)
            StepCard(
                state = StepState.Completed,
                title = "SEGMENTATION",
                description = "Geometric mesh generation complete",
                extraText = null
            )

            // Step 2: QUANTIFICATION (Processing)
            StepCard(
                state = StepState.Processing,
                title = "QUANTIFICATION",
                description = "Counting structural nodes... ",
                extraText = "428 Found"
            )

            // Step 3: VALIDATION (Pending)
            StepCard(
                state = StepState.Pending,
                title = "VALIDATION",
                description = "Pending data integrity check",
                extraText = null
            )
        }

        Spacer(Modifier.height(40.dp))
        
        HorizontalDivider(thickness = 0.5.dp, color = OutlineVariant.copy(alpha = 0.3f))
        
        Spacer(Modifier.height(24.dp))

        // Metrics Bottom Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(label = "LATENCY", value = "24ms")
            MetricItem(label = "PRECISION", value = "99.8%")
            MetricItem(label = "LOAD", value = "Low")
        }
    }
}

enum class StepState {
    Completed, Processing, Pending
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

@Composable
fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProcessingScreenPreview() {
    Dadn_appTheme {
        ProcessingScreen()
    }
}
