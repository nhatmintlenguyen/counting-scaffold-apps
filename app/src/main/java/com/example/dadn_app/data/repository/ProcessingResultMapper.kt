package com.example.dadn_app.data.repository

import com.example.dadn_app.data.models.DetectionDto
import com.example.dadn_app.data.models.ProcessingResultResponse
import com.google.gson.Gson

object ProcessingResultMapper {
    private val gson = Gson()

    fun toJson(response: ProcessingResultResponse): String =
        gson.toJson(response)

    fun fromJson(json: String?): ProcessingResultData {
        if (json.isNullOrBlank()) return ProcessingResultData()
        return try {
            val response = gson.fromJson(json, ProcessingResultResponse::class.java)
            ProcessingResultData(
                totalCount = response.totalCount ?: response.details?.scaffoldsDetected,
                detections = response.details?.details.orEmpty().toYoloDetections(),
            )
        } catch (_: Exception) {
            ProcessingResultData()
        }
    }

    fun List<DetectionDto>.toYoloDetections(): List<YoloDetection> =
        mapIndexedNotNull { index, detection ->
            val bbox = detection.bbox
            if (bbox.size < 4) return@mapIndexedNotNull null

            YoloDetection(
                id = index + 1,
                label = detection.classId?.toString() ?: "",
                confidence = detection.confidence ?: 0f,
                xMin = bbox[0],
                yMin = bbox[1],
                xMax = bbox[2],
                yMax = bbox[3],
            )
        }
}

data class ProcessingResultData(
    val totalCount: Int? = null,
    val detections: List<YoloDetection> = emptyList(),
)
