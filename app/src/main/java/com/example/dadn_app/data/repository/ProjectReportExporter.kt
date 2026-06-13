package com.example.dadn_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.dadn_app.data.local.ScanProject
import com.example.dadn_app.data.local.ScanRecord
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectReportExporter {
    private const val YOLO_SOURCE_IMAGE_SIZE = 640f

    fun export(context: Context, project: ScanProject, scans: List<ScanRecord>): Uri {
        require(scans.isNotEmpty()) { "No successful scans available to export." }

        val reportDir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeName = project.name.replace(Regex("[^A-Za-z0-9_-]+"), "_").trim('_').ifBlank { "project" }
        val reportFile = File(reportDir, "${safeName}_report_$timestamp.xls")

        reportFile.writeText(buildHtmlReport(context, project, scans), Charsets.UTF_8)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            reportFile,
        )
    }

    private fun buildHtmlReport(
        context: Context,
        project: ScanProject,
        scans: List<ScanRecord>,
    ): String {
        val rows = scans.joinToString(separator = "\n") { scan ->
            val result = ProcessingResultMapper.fromJson(scan.resultJson)
            val image = annotatedImageDataUri(context, scan.imageUri, result.detections)
            val count = result.totalCount ?: scan.count
            """
            <tr>
              <td class="image-cell"><img src="$image" /></td>
              <td class="count-cell">$count</td>
            </tr>
            """.trimIndent()
        }

        return """
        <html>
        <head>
          <meta charset="utf-8" />
          <style>
            body { font-family: Arial, sans-serif; color: #111C2D; }
            h1 { font-size: 22px; margin-bottom: 4px; }
            .subtitle { color: #414754; margin-bottom: 18px; }
            table { border-collapse: collapse; width: 100%; }
            th { background: #00647C; color: white; padding: 10px; text-align: left; }
            td { border: 1px solid #C1C6D7; padding: 10px; vertical-align: middle; }
            .image-cell { width: 460px; }
            .image-cell img { width: 420px; height: 420px; object-fit: contain; }
            .count-cell { text-align: center; font-size: 42px; font-weight: bold; color: #00647C; }
          </style>
        </head>
        <body>
          <h1>${escapeHtml(project.name)}</h1>
          <div class="subtitle">Scaffold counting report - ${scans.size} successful scan(s)</div>
          <table>
            <tr>
              <th>Image with bounding boxes</th>
              <th>Total count</th>
            </tr>
            $rows
          </table>
        </body>
        </html>
        """.trimIndent()
    }

    private fun annotatedImageDataUri(
        context: Context,
        imageUri: String,
        detections: List<YoloDetection>,
    ): String {
        val bitmap = decodeBitmap(context, imageUri)
            ?: return "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw=="
        val annotated = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        drawDetections(annotated, detections)
        val exportBitmap = resizeForReport(annotated, maxSide = 900)

        val output = ByteArrayOutputStream()
        exportBitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
        val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$encoded"
    }

    private fun decodeBitmap(context: Context, imageUri: String): Bitmap? =
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()

    private fun drawDetections(bitmap: Bitmap, detections: List<YoloDetection>) {
        if (detections.isEmpty()) return

        val canvas = Canvas(bitmap)
        val scale = minOf(bitmap.width, bitmap.height) / YOLO_SOURCE_IMAGE_SIZE
        val offsetX = (bitmap.width - YOLO_SOURCE_IMAGE_SIZE * scale) / 2f
        val offsetY = (bitmap.height - YOLO_SOURCE_IMAGE_SIZE * scale) / 2f
        val stroke = (bitmap.width.coerceAtMost(bitmap.height) / 220f).coerceAtLeast(3f)

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 229, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 0, 90, 102)
            style = Paint.Style.FILL
        }
        val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (bitmap.width.coerceAtMost(bitmap.height) / 38f).coerceAtLeast(18f)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        detections.forEach { detection ->
            val left = offsetX + detection.xMin * scale
            val top = offsetY + detection.yMin * scale
            val right = offsetX + detection.xMax * scale
            val bottom = offsetY + detection.yMax * scale
            canvas.drawRect(left, top, right, bottom, boxPaint)

            val label = detection.id.toString()
            val textWidth = labelTextPaint.measureText(label)
            val labelHeight = labelTextPaint.textSize * 1.25f
            val labelWidth = textWidth + labelTextPaint.textSize * 0.7f
            val labelLeft = left.coerceIn(0f, bitmap.width - labelWidth)
            val labelTop = (top - labelHeight).coerceAtLeast(0f)
            canvas.drawRoundRect(
                RectF(labelLeft, labelTop, labelLeft + labelWidth, labelTop + labelHeight),
                8f,
                8f,
                labelBgPaint,
            )
            canvas.drawText(
                label,
                labelLeft + labelTextPaint.textSize * 0.25f,
                labelTop + labelTextPaint.textSize,
                labelTextPaint,
            )
        }
    }

    private fun resizeForReport(bitmap: Bitmap, maxSide: Int): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= maxSide) return bitmap
        val scale = maxSide.toFloat() / largestSide
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true,
        )
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
