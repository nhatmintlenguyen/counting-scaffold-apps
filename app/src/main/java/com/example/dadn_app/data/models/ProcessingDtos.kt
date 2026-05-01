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
    @SerializedName("result_image_url")
    val resultImageUrl: String? = null,
    val details: ProcessingDetailsDto? = null,
    @SerializedName("total_count")
    val totalCount: Int? = null,
    val error: String? = null,
    val message: String? = null,
)

data class ProcessingDetailsDto(
    @SerializedName("scaffolds_detected")
    val scaffoldsDetected: Int? = null,
    @SerializedName("count_by_class")
    val countByClass: Map<String, Int> = emptyMap(),
    val details: List<DetectionDto> = emptyList(),
)

data class DetectionDto(
    val id: Int? = null,
    @SerializedName("class")
    val classId: String? = null,
    val confidence: Float? = null,
    val bbox: List<Float> = emptyList(),
)
