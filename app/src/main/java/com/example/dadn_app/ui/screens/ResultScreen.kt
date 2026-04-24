package com.example.dadn_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dadn_app.ui.theme.*
import kotlin.random.Random

@Composable
fun ResultScreen(
    imageUri: String? = null,
    scaffoldCount: Int? = null,
) {
    // TEMP TESTING ONLY:
    // Replace this random hardcoded scaffold count with the real count returned
    // by the backend processing API when server-side processing is connected.
    val resolvedScaffoldCount = scaffoldCount ?: remember { Random.nextInt(12, 46) }
    // END TEMP TESTING ONLY

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
                    contentDescription = "Processed image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.14f))
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
                            .background(Color(0xFF4CAF50)) // Green success dot
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analysis Complete",
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
                        text = "100%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50), // Green success text
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
                .background(Color(0xFFE8F5E9)) // Light green background
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "PROCESSING COMPLETE",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32), // Dark green text
                letterSpacing = 1.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Count Results",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface,
            letterSpacing = (-1).sp,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Total structural elements identified and verified against safety parameters.",
            fontSize = 15.sp,
            color = OnSurfaceVariant,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0F2F1))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL SCAFFOLDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Detected in current scan",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = resolvedScaffoldCount.toString(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32),
                    lineHeight = 42.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Stepper (All Completed)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StepCard(
                state = StepState.Completed,
                title = "SEGMENTATION",
                description = "Geometric mesh generation complete",
                extraText = null
            )

            StepCard(
                state = StepState.Completed,
                title = "QUANTIFICATION",
                description = "Structural nodes identified",
                extraText = "$resolvedScaffoldCount Found"
            )

            StepCard(
                state = StepState.Completed,
                title = "VALIDATION",
                description = "Data integrity verified",
                extraText = "Safe"
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultScreenPreview() {
    Dadn_appTheme {
        ResultScreen()
    }
}
