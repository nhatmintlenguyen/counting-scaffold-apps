package com.example.dadn_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dadn_app.data.local.ScanRecord
import com.example.dadn_app.data.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScanRepository.getInstance(app)

    /** Observed by the UI — updates automatically whenever the DB changes. */
    val scans: StateFlow<List<ScanRecord>> = repo.observeRecentScans().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val currentActiveScan: StateFlow<ScanRecord?> = repo.observeCurrentActiveScan().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    init {
        repo.ensurePendingScansProcessing()
    }

    fun addScanAndStartProcessing(scan: ScanRecord, onInserted: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val scanId = repo.insertAndStartProcessing(scan)
            onInserted(scanId)
        }
    }

    fun observeScan(scanId: Int): Flow<ScanRecord?> = repo.observeScan(scanId)

    fun observeProcessingUiState(scanId: Int): Flow<ProcessingUiState> =
        repo.observeProcessingUiState(scanId)

    fun ensureProcessingStarted(scanId: Int, imagePath: String) {
        viewModelScope.launch {
            repo.ensureProcessingStarted(scanId, imagePath)
        }
    }
}
