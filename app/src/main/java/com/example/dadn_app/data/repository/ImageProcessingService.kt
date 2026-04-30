package com.example.dadn_app.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.dadn_app.core.network.ProcessingRetrofitClient
import com.example.dadn_app.data.repository.ProcessingResultMapper.toYoloDetections
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

sealed interface ProcessingJobState {
    data object Processing : ProcessingJobState
    data class Success(
        val count: Int,
        val detections: List<YoloDetection>,
        val resultJson: String,
    ) : ProcessingJobState
    data class Error(val message: String) : ProcessingJobState
}

data class ProcessingJob(
    val jobId: String,
    val imageUri: String,
)

interface ImageProcessingService {
    suspend fun startJob(imageUri: String): ProcessingJob
    suspend fun pollJob(jobId: String): ProcessingJobState
}

class RealImageProcessingService(
    private val context: Context,
) : ImageProcessingService {
    private val api = ProcessingRetrofitClient.processingApi

    override suspend fun startJob(imageUri: String): ProcessingJob {
        val uploadFile = imageUriToUploadFile(imageUri)
        val requestBody = uploadFile.file.asRequestBody(uploadFile.mimeType.toMediaTypeOrNull())

        /*
         * BACKEND CONTRACT ASSUMPTION:
         * The backend contract says multipart/form-data upload, but does not
         * specify the part name. "file" is isolated here so it can be changed
         * when the backend teammate confirms the exact field name.
         */
        val imagePart = MultipartBody.Part.createFormData(
            name = FILE_PART_NAME,
            filename = uploadFile.file.name,
            body = requestBody,
        )

        val response = api.uploadImage(imagePart)
        if (!response.isSuccessful) {
            throw IllegalStateException("Upload failed: HTTP ${response.code()}")
        }

        val body = response.body() ?: throw IllegalStateException("Upload failed: empty response")
        val jobId = body.jobId ?: throw IllegalStateException("Upload failed: missing job_id")
        val status = body.status.orEmpty().lowercase(Locale.US)
        if (status in ERROR_STATUSES) {
            throw IllegalStateException(body.error ?: body.message ?: "Upload failed")
        }

        return ProcessingJob(jobId = jobId, imageUri = imageUri)
    }

    override suspend fun pollJob(jobId: String): ProcessingJobState {
        val response = api.getResult(jobId)
        if (!response.isSuccessful) {
            return ProcessingJobState.Error("Result polling failed: HTTP ${response.code()}")
        }

        val body = response.body()
            ?: return ProcessingJobState.Error("Result polling failed: empty response")

        val status = body.status.orEmpty().lowercase(Locale.US)
        val hasFinalResult = body.totalCount != null || body.details != null

        return when {
            status in SUCCESS_STATUSES || (status.isBlank() && hasFinalResult) -> {
                val count = body.totalCount
                    ?: body.details?.scaffoldsDetected
                    ?: return ProcessingJobState.Error("Result completed without total_count")
                ProcessingJobState.Success(
                    count = count,
                    detections = body.details?.details.orEmpty().toYoloDetections(),
                    resultJson = ProcessingResultMapper.toJson(body),
                )
            }

            status in ERROR_STATUSES -> {
                ProcessingJobState.Error(body.error ?: body.message ?: "Processing failed")
            }

            status in PROCESSING_STATUSES || status.isBlank() -> {
                ProcessingJobState.Processing
            }

            else -> {
                // Unknown backend statuses stay in processing until the app timeout policy resolves them.
                ProcessingJobState.Processing
            }
        }
    }

    private fun imageUriToUploadFile(imageUri: String): UploadFile {
        val uri = Uri.parse(imageUri)
        if (uri.scheme == "file") {
            val file = File(requireNotNull(uri.path) { "Invalid file URI: $imageUri" })
            return UploadFile(file = file, mimeType = guessMimeType(uri, file))
        }

        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.lowercase(Locale.US)
            ?: "jpg"
        val tempFile = File(context.cacheDir, "processing_upload_${System.currentTimeMillis()}.$extension")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Unable to open image for upload")

        return UploadFile(file = tempFile, mimeType = mimeType)
    }

    private fun guessMimeType(uri: Uri, file: File): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            .ifBlank { file.extension }
            .lowercase(Locale.US)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
    }

    private data class UploadFile(
        val file: File,
        val mimeType: String,
    )

    companion object {
        private const val FILE_PART_NAME = "file"

        private val PROCESSING_STATUSES = setOf(
            "queued",
            "pending",
            "processing",
            "running",
            "uploaded",
            "in_progress",
        )

        private val SUCCESS_STATUSES = setOf(
            "success",
            "succeeded",
            "complete",
            "completed",
            "done",
            "finished",
        )

        private val ERROR_STATUSES = setOf(
            "error",
            "failed",
            "failure",
            "timeout",
        )
    }
}
