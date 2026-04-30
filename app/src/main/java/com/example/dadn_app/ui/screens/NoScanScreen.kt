package com.example.dadn_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dadn_app.ui.theme.Background
import com.example.dadn_app.ui.theme.Dadn_appTheme
import com.example.dadn_app.ui.theme.OnSurface
import com.example.dadn_app.ui.theme.OnSurfaceVariant
import com.example.dadn_app.ui.theme.OutlineVariant
import com.example.dadn_app.ui.theme.Primary
import com.example.dadn_app.ui.theme.PrimaryFixed
import com.example.dadn_app.ui.theme.SurfaceContainerHigh
import com.example.dadn_app.ui.theme.SurfaceContainerLow
import com.example.dadn_app.ui.theme.SurfaceContainerLowest

@Composable
fun NoScanScreen(
    onStartScan: () -> Unit = {},
    onBrowseFiles: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryFixed.copy(alpha = 0.72f),
                            SurfaceContainerLow.copy(alpha = 0.9f),
                            Background,
                        ),
                        radius = 760f,
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            NoScanIllustration()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No image is being\nprocessed",
                fontSize = 31.sp,
                fontWeight = FontWeight.Black,
                color = OnSurface,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Start a new scaffold scan by capturing an image with your device or browse local files for a technical blueprint analysis.",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(34.dp))

            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Start New Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBrowseFiles,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.75f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceContainerLowest,
                    contentColor = Primary,
                ),
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Browse Files", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(34.dp))
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun NoScanIllustration() {
    Box(contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.size(210.dp),
            shape = RoundedCornerShape(32.dp),
            color = SurfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = null,
                    tint = OutlineVariant,
                    modifier = Modifier.size(106.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 7.dp, height = 44.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SurfaceContainerHigh)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(width = 98.dp, height = 4.dp)
                        .rotate(45f)
                        .clip(CircleShape)
                        .background(Color(0xFFE57373).copy(alpha = 0.82f))
                )
            }
        }

        Surface(
            modifier = Modifier.offset(y = 18.dp),
            shape = RoundedCornerShape(10.dp),
            color = SurfaceContainerLow,
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.55f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "SYSTEM IDLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,
                    letterSpacing = 0.8.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoScanScreenPreview() {
    Dadn_appTheme {
        NoScanScreen()
    }
}
