package com.example.dadn_app.ui.viewmodel

sealed interface ProcessingUiState {
    data object Idle : ProcessingUiState
    data class Processing(val elapsedMillis: Long) : ProcessingUiState
    data class Success(val count: Int) : ProcessingUiState
    data class Error(val message: String, val timedOut: Boolean = false) : ProcessingUiState
}
