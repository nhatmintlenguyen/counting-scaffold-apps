package com.example.dadn_app.ui.screens

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dadn_app.data.repository.YoloDetection
import com.example.dadn_app.ui.theme.*
import java.util.Locale

@Composable
fun ResultScreen(
    imageUri: String? = null,
    scaffoldCount: Int? = null,
    processingTimeMillis: Long? = null,
    detections: List<YoloDetection> = emptyList(),
) {
    val resolvedScaffoldCount = scaffoldCount ?: detections.size
    val averageConfidence = remember(detections) {
        detections.takeIf { it.isNotEmpty() }
            ?.map { it.confidence }
            ?.average()
            ?.toFloat()
    }
    val accuracyLabel = formatAccuracy(averageConfidence)
    val processingTimeLabel = formatProcessingTime(processingTimeMillis)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(OutlineVariant.copy(alpha = 0.5f)),
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
                        .background(Color.Black.copy(alpha = 0.08f))
                )
            }

            YoloDetectionOverlay(
                detections = detections,
                sourceImageSize = YOLO_SOURCE_IMAGE_SIZE,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultMetricCard(
                icon = Icons.Default.Verified,
                label = "ACCURACY",
                value = accuracyLabel,
                accent = Color(0xFF28C9F5),
                modifier = Modifier.weight(1f),
            )
            ResultMetricCard(
                icon = Icons.Default.GridView,
                label = "SCAFFOLDS",
                value = resolvedScaffoldCount.toString(),
                accent = Color(0xFF006A78),
                modifier = Modifier.weight(1f),
            )
            ResultMetricCard(
                icon = Icons.Default.Timer,
                label = "PROC. TIME",
                value = processingTimeLabel,
                accent = Color(0xFFB9C0D0),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFD9E5FF),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF006A78),
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "RESULTS AUTOMATICALLY ARCHIVED TO PROJECT CLOUD",
                    color = Color(0xFF006A78),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 18.sp,
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ResultMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(112.dp)
            .border(
                width = 1.dp,
                color = OutlineVariant.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                color = OnSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                color = OnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 22.sp,
            )
        }
    }
}

private const val YOLO_SOURCE_IMAGE_SIZE = 640f

private fun formatAccuracy(confidence: Float?): String {
    if (confidence == null) return "--"
    return String.format(Locale.US, "%.1f%%", confidence * 100f)
}

private fun formatProcessingTime(processingTimeMillis: Long?): String {
    if (processingTimeMillis == null) return "--"
    return String.format(Locale.US, "%.1fs", processingTimeMillis / 1000f)
}

@Composable
private fun YoloDetectionOverlay(
    detections: List<YoloDetection>,
    sourceImageSize: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val scale = size.minDimension / sourceImageSize
        val offsetX = (size.width - sourceImageSize * scale) / 2f
        val offsetY = (size.height - sourceImageSize * scale) / 2f
        val strokeWidth = 1.5.dp.toPx()
        val boxColor = Color(0xFF00E5FF)
        val labelBgColor = Color(0xE6005A66)
        val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 10.sp.toPx()
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val textBounds = Rect()

        detections.forEach { detection ->
            val left = offsetX + detection.xMin * scale
            val top = offsetY + detection.yMin * scale
            val right = offsetX + detection.xMax * scale
            val bottom = offsetY + detection.yMax * scale

            drawRect(
                color = boxColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                style = Stroke(width = strokeWidth),
            )

            val label = detection.id.toString()
            labelTextPaint.getTextBounds(label, 0, label.length, textBounds)
            val labelPaddingX = 4.dp.toPx()
            val labelPaddingY = 2.dp.toPx()
            val labelWidth = textBounds.width() + labelPaddingX * 2
            val labelHeight = textBounds.height() + labelPaddingY * 2
            val labelLeft = left.coerceIn(0f, size.width - labelWidth)
            val labelTop = (top - labelHeight).coerceAtLeast(0f)

            drawRoundRect(
                color = labelBgColor,
                topLeft = androidx.compose.ui.geometry.Offset(labelLeft, labelTop),
                size = androidx.compose.ui.geometry.Size(labelWidth, labelHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
            drawContext.canvas.nativeCanvas.drawText(
                label,
                labelLeft + labelPaddingX,
                labelTop + labelHeight - labelPaddingY,
                labelTextPaint,
            )
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
