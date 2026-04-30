package com.example.dadn_app.data.models

import com.google.gson.annotations.SerializedName

data class UploadImageResponse(
    @SerializedName("job_id")
    val jobId: String?,
    val status: String?,
    val error: String? = null,
    val message: String? = null,
)

data class ProcessingResultResponse(
    @SerializedName("job_id")
    val jobId: String? = null,
    val status: String?,
    val details: ProcessingDetailsDto? = null,
    @SerializedName("total_count")
    val totalCount: Int? = null,
    val error: String? = null,
    val message: String? = null,
)

data class ProcessingDetailsDto(
    @SerializedName("scaffolds_detected")
    val scaffoldsDetected: Int? = null,
    val details: List<DetectionDto> = emptyList(),
)

data class DetectionDto(
    @SerializedName("class")
    val classId: Int? = null,
    val confidence: Float? = null,
    val bbox: List<Float> = emptyList(),
)
