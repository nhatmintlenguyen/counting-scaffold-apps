package com.example.dadn_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.dadn_app.data.local.ScanProject
import com.example.dadn_app.data.local.ScanRecord
import com.example.dadn_app.data.repository.ScanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dadn_app.data.repository.ProjectReportExporter

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

    val projects: StateFlow<List<ScanProject>> = repo.observeProjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
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

    fun createProject(name: String, onCreated: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val projectId = repo.createProject(name)
            onCreated(projectId)
        }
    }

    fun exportProjectReport(project: ScanProject, onResult: (Result<Uri>) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val scans = repo.getCompletedScansForProject(project.id)
                    ProjectReportExporter.export(getApplication(), project, scans)
                }
            }
            onResult(result)
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
