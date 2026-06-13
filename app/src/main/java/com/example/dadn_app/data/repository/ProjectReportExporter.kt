package com.example.dadn_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.dadn_app.data.local.ScanProject
import com.example.dadn_app.data.local.ScanRecord
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectReportExporter {
    private const val YOLO_SOURCE_IMAGE_SIZE = 640f
    private const val IMAGE_SIZE_EMU = 2_857_500

    fun export(context: Context, project: ScanProject, scans: List<ScanRecord>): Uri {
        require(scans.isNotEmpty()) { "No successful scans available to export." }

        val reportDir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeName = project.name.replace(Regex("[^A-Za-z0-9_-]+"), "_").trim('_').ifBlank { "project" }
        val reportFile = File(reportDir, "${safeName}_report_$timestamp.xlsx")

        buildXlsxReport(context, project, scans, reportFile)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", reportFile)
    }

    private fun buildXlsxReport(context: Context, project: ScanProject, scans: List<ScanRecord>, reportFile: File) {
        val rows = scans.mapIndexed { index, scan ->
            val result = ProcessingResultMapper.fromJson(scan.resultJson)
            ReportRow(
                index = index + 1,
                name = scan.name,
                datetime = scan.datetime,
                count = result.totalCount ?: scan.count,
                imageBytes = annotatedImageBytes(context, scan.imageUri, result.detections),
            )
        }

        ZipOutputStream(reportFile.outputStream().buffered()).use { zip ->
            zip.putText("[Content_Types].xml", contentTypes(rows.size))
            zip.putText("_rels/.rels", rootRelationships())
            zip.putText("xl/workbook.xml", workbook())
            zip.putText("xl/_rels/workbook.xml.rels", workbookRelationships())
            zip.putText("xl/styles.xml", styles())
            zip.putText("xl/worksheets/sheet1.xml", sheet(project, rows))
            zip.putText("xl/worksheets/_rels/sheet1.xml.rels", sheetRelationships())
            zip.putText("xl/drawings/drawing1.xml", drawing(rows))
            zip.putText("xl/drawings/_rels/drawing1.xml.rels", drawingRelationships(rows.size))
            rows.forEach { zip.putBytes("xl/media/image${it.index}.jpg", it.imageBytes) }
        }
    }

    private fun ZipOutputStream.putText(path: String, text: String) {
        putNextEntry(ZipEntry(path)); write(text.toByteArray(Charsets.UTF_8)); closeEntry()
    }

    private fun ZipOutputStream.putBytes(path: String, bytes: ByteArray) {
        putNextEntry(ZipEntry(path)); write(bytes); closeEntry()
    }

    private fun contentTypes(imageCount: Int): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>
${(1..imageCount).joinToString("\n") { "  <Override PartName=\"/xl/media/image$it.jpg\" ContentType=\"image/jpeg\"/>" }}
</Types>"""

    private fun rootRelationships(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbook(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="Report" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun workbookRelationships(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun styles(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="3"><font><sz val="11"/><name val="Arial"/></font><font><b/><sz val="18"/><name val="Arial"/></font><font><b/><sz val="20"/><color rgb="FF00647C"/><name val="Arial"/></font></fonts>
  <fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF00647C"/></patternFill></fill></fills>
  <borders count="2"><border/><border><left style="thin"/><right style="thin"/><top style="thin"/><bottom style="thin"/></border></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="5"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0"/><xf numFmtId="0" fontId="0" fillId="2" borderId="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf><xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyAlignment="1"><alignment vertical="center" wrapText="1"/></xf><xf numFmtId="0" fontId="2" fillId="0" borderId="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf></cellXfs>
  <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
</styleSheet>"""

    private fun sheet(project: ScanProject, rows: List<ReportRow>): String {
        val scanRows = rows.joinToString("\n") { row ->
            val r = row.excelRow
            """    <row r="$r" ht="230" customHeight="1"><c r="A$r" s="3"/><c r="B$r" s="3" t="inlineStr"><is><t>${escapeXml(row.name)}</t></is></c><c r="C$r" s="3" t="inlineStr"><is><t>${escapeXml(row.datetime)}</t></is></c><c r="D$r" s="4"><v>${row.count}</v></c></row>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <dimension ref="A1:D${rows.lastOrNull()?.excelRow ?: 4}"/><sheetViews><sheetView workbookViewId="0"/></sheetViews><sheetFormatPr defaultRowHeight="18"/>
  <cols><col min="1" max="1" width="44" customWidth="1"/><col min="2" max="2" width="28" customWidth="1"/><col min="3" max="3" width="24" customWidth="1"/><col min="4" max="4" width="16" customWidth="1"/></cols>
  <sheetData>
    <row r="1" ht="28" customHeight="1"><c r="A1" s="1" t="inlineStr"><is><t>${escapeXml(project.name)}</t></is></c></row>
    <row r="2"><c r="A2" t="inlineStr"><is><t>Scaffold counting report - ${rows.size} successful scan(s)</t></is></c></row>
    <row r="4" ht="24" customHeight="1"><c r="A4" s="2" t="inlineStr"><is><t>Image with bounding boxes</t></is></c><c r="B4" s="2" t="inlineStr"><is><t>Name</t></is></c><c r="C4" s="2" t="inlineStr"><is><t>Date/time</t></is></c><c r="D4" s="2" t="inlineStr"><is><t>Total count</t></is></c></row>
$scanRows
  </sheetData><drawing r:id="rId1"/>
</worksheet>"""
    }

    private fun sheetRelationships(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing" Target="../drawings/drawing1.xml"/>
</Relationships>"""

    private fun drawing(rows: List<ReportRow>): String {
        val anchors = rows.joinToString("\n") { row ->
            val zeroRow = row.excelRow - 1
            """  <xdr:oneCellAnchor><xdr:from><xdr:col>0</xdr:col><xdr:colOff>95250</xdr:colOff><xdr:row>$zeroRow</xdr:row><xdr:rowOff>95250</xdr:rowOff></xdr:from><xdr:ext cx="$IMAGE_SIZE_EMU" cy="$IMAGE_SIZE_EMU"/><xdr:pic><xdr:nvPicPr><xdr:cNvPr id="${row.index}" name="Scan ${row.index}"/><xdr:cNvPicPr><a:picLocks noChangeAspect="1"/></xdr:cNvPicPr></xdr:nvPicPr><xdr:blipFill><a:blip r:embed="rId${row.index}"/><a:stretch><a:fillRect/></a:stretch></xdr:blipFill><xdr:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$IMAGE_SIZE_EMU" cy="$IMAGE_SIZE_EMU"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></xdr:spPr></xdr:pic><xdr:clientData/></xdr:oneCellAnchor>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
$anchors
</xdr:wsDr>"""
    }

    private fun drawingRelationships(imageCount: Int): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
${(1..imageCount).joinToString("\n") { "  <Relationship Id=\"rId$it\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image$it.jpg\"/>" }}
</Relationships>"""

    private fun annotatedImageBytes(context: Context, imageUri: String, detections: List<YoloDetection>): ByteArray {
        val bitmap = decodeBitmap(context, imageUri) ?: blankBitmap()
        val annotated = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        drawDetections(annotated, detections)
        val output = ByteArrayOutputStream()
        resizeForReport(annotated, maxSide = 900).compress(Bitmap.CompressFormat.JPEG, 88, output)
        return output.toByteArray()
    }

    private fun decodeBitmap(context: Context, imageUri: String): Bitmap? = runCatching {
        context.contentResolver.openInputStream(Uri.parse(imageUri))?.use(BitmapFactory::decodeStream)
    }.getOrNull()

    private fun blankBitmap(): Bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }

    private fun drawDetections(bitmap: Bitmap, detections: List<YoloDetection>) {
        if (detections.isEmpty()) return
        val canvas = Canvas(bitmap)
        val scale = minOf(bitmap.width, bitmap.height) / YOLO_SOURCE_IMAGE_SIZE
        val offsetX = (bitmap.width - YOLO_SOURCE_IMAGE_SIZE * scale) / 2f
        val offsetY = (bitmap.height - YOLO_SOURCE_IMAGE_SIZE * scale) / 2f
        val stroke = (bitmap.width.coerceAtMost(bitmap.height) / 220f).coerceAtLeast(3f)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 229, 255); style = Paint.Style.STROKE; strokeWidth = stroke }
        val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(230, 0, 90, 102); style = Paint.Style.FILL }
        val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = (bitmap.width.coerceAtMost(bitmap.height) / 38f).coerceAtLeast(18f); typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD) }

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
            canvas.drawRoundRect(RectF(labelLeft, labelTop, labelLeft + labelWidth, labelTop + labelHeight), 8f, 8f, labelBgPaint)
            canvas.drawText(label, labelLeft + labelTextPaint.textSize * 0.25f, labelTop + labelTextPaint.textSize, labelTextPaint)
        }
    }

    private fun resizeForReport(bitmap: Bitmap, maxSide: Int): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= maxSide) return bitmap
        val scale = maxSide.toFloat() / largestSide
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    }

    private fun escapeXml(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

    private data class ReportRow(val index: Int, val name: String, val datetime: String, val count: Int, val imageBytes: ByteArray) {
        val excelRow: Int = index + 4
    }
}
