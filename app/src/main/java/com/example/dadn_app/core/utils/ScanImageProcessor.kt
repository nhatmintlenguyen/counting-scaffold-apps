package com.example.dadn_app.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class SquareCropSpec(
        val previewWidthPx: Int,
        val previewHeightPx: Int,
        val frameLeftPx: Int,
        val frameTopPx: Int,
        val frameSizePx: Int,
)

object ScanImageProcessor {
    private const val YOLO_IMAGE_SIZE = 640

    fun createOriginalCaptureFile(context: Context): File {
        val dir = File(context.filesDir, "scan_originals").also { it.mkdirs() }
        return File(dir, "original_${System.currentTimeMillis()}.jpg")
    }

    fun cropToOverlaySquareAndResize(
            context: Context,
            originalFile: File,
            cropSpec: SquareCropSpec,
    ): Uri {
        val orientedBitmap = decodeAndApplyExifRotation(originalFile)
        val cropRect = mapCenteredPreviewFrameToBitmap(orientedBitmap, cropSpec)
        val cropped =
                Bitmap.createBitmap(
                        orientedBitmap,
                        cropRect.left,
                        cropRect.top,
                        cropRect.size,
                        cropRect.size,
                )
        val resized = Bitmap.createScaledBitmap(cropped, YOLO_IMAGE_SIZE, YOLO_IMAGE_SIZE, true)
        val outputFile = createProcessedScanFile(context)

        FileOutputStream(outputFile).use { output ->
            resized.compress(Bitmap.CompressFormat.JPEG, 94, output)
        }

        if (cropped !== orientedBitmap) cropped.recycle()
        if (resized !== cropped) resized.recycle()
        orientedBitmap.recycle()

        return Uri.fromFile(outputFile)
    }

    fun cropGalleryImageToSquare(context: Context, sourceUri: Uri): Uri {
        val bitmap =
                decodeUriToBitmap(context, sourceUri)
                        ?: return sourceUri // fallback: trả về URI gốc nếu không decode được

        val size = min(bitmap.width, bitmap.height)
        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2

        val cropped = Bitmap.createBitmap(bitmap, left, top, size, size)
        val resized = Bitmap.createScaledBitmap(cropped, YOLO_IMAGE_SIZE, YOLO_IMAGE_SIZE, true)
        val outputFile = createProcessedScanFile(context)

        FileOutputStream(outputFile).use { out ->
            resized.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        if (cropped !== bitmap) cropped.recycle()
        if (resized !== cropped) resized.recycle()
        bitmap.recycle()

        return Uri.fromFile(outputFile)
    }

    private fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun createProcessedScanFile(context: Context): File {
        val dir = File(context.filesDir, "scan_images").also { it.mkdirs() }
        return File(dir, "scan_${System.currentTimeMillis()}_640.jpg")
    }

    private fun decodeAndApplyExifRotation(file: File): Bitmap {
        val bitmap =
                BitmapFactory.decodeFile(file.absolutePath)
                        ?: error("Unable to decode captured image")

        val orientation =
                ExifInterface(file.absolutePath)
                        .getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL,
                        )
        val rotation =
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

        if (rotation == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotated
    }

    private fun mapCenteredPreviewFrameToBitmap(
            bitmap: Bitmap,
            cropSpec: SquareCropSpec,
    ): BitmapSquareRect {
        if (cropSpec.previewWidthPx <= 0 ||
                        cropSpec.previewHeightPx <= 0 ||
                        cropSpec.frameSizePx <= 0
        ) {
            val fallbackSize = min(bitmap.width, bitmap.height)
            return BitmapSquareRect(
                    left = (bitmap.width - fallbackSize) / 2,
                    top = (bitmap.height - fallbackSize) / 2,
                    size = fallbackSize,
            )
        }

        // PreviewView uses a center-fill style preview. This reverses that transform:
        // bitmap -> scaled preview -> centered square frame -> bitmap crop.
        val scale =
                max(
                        cropSpec.previewWidthPx.toFloat() / bitmap.width.toFloat(),
                        cropSpec.previewHeightPx.toFloat() / bitmap.height.toFloat(),
                )
        val displayedWidth = bitmap.width * scale
        val displayedHeight = bitmap.height * scale
        val displayedLeft = (cropSpec.previewWidthPx - displayedWidth) / 2f
        val displayedTop = (cropSpec.previewHeightPx - displayedHeight) / 2f

        val rawLeft = ((cropSpec.frameLeftPx - displayedLeft) / scale).roundToInt()
        val rawTop = ((cropSpec.frameTopPx - displayedTop) / scale).roundToInt()
        val rawSize = (cropSpec.frameSizePx / scale).roundToInt()

        val size = min(rawSize, min(bitmap.width, bitmap.height)).coerceAtLeast(1)
        val left = rawLeft.coerceIn(0, bitmap.width - size)
        val top = rawTop.coerceIn(0, bitmap.height - size)

        return BitmapSquareRect(left = left, top = top, size = size)
    }
}

private data class BitmapSquareRect(
        val left: Int,
        val top: Int,
        val size: Int,
)
