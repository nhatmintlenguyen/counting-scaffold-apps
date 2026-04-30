package com.example.dadn_app.ui.viewmodel

import com.example.dadn_app.data.repository.YoloDetection

sealed interface ProcessingUiState {
    data object Idle : ProcessingUiState
    data class Processing(val elapsedMillis: Long) : ProcessingUiState
    data class Success(
        val count: Int,
        val processingTimeMillis: Long,
        val detections: List<YoloDetection>,
        val resultJson: String,
    ) : ProcessingUiState
    data class Error(val message: String, val timedOut: Boolean = false) : ProcessingUiState
}
