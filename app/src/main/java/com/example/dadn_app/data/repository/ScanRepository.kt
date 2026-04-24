package com.example.dadn_app.data.repository

import android.content.Context
import com.example.dadn_app.data.local.AppDatabase
import com.example.dadn_app.data.local.ScanDao
import com.example.dadn_app.data.local.ScanRecord
import com.example.dadn_app.ui.viewmodel.ProcessingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScanRepository private constructor(
    private val dao: ScanDao,
    private val processingService: ImageProcessingService = MockImageProcessingService,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pollingMutex = Mutex()
    private val pollingJobs = mutableMapOf<Int, Job>()
    private val processingStates = MutableStateFlow<Map<Int, ProcessingUiState>>(emptyMap())

    fun observeRecentScans(): Flow<List<ScanRecord>> = dao.getAll()

    fun observeScan(scanId: Int): Flow<ScanRecord?> = dao.observeById(scanId)

    fun observeCurrentActiveScan(): Flow<ScanRecord?> = dao.observeCurrentActiveScan()

    fun observeProcessingUiState(scanId: Int): Flow<ProcessingUiState> =
        processingStates
            .map { states -> states[scanId] ?: ProcessingUiState.Processing(0L) }
            .distinctUntilChanged()

    suspend fun insert(scan: ScanRecord): Int = dao.insert(scan).toInt()

    suspend fun insertAndStartProcessing(scan: ScanRecord): Int {
        val scanId = insert(scan)
        ensureProcessingStarted(scanId = scanId, imagePath = scan.imageUri)
        return scanId
    }

    suspend fun completeScan(scanId: Int, count: Int) {
        dao.updateStatusAndCountById(
            scanId = scanId,
            status = "Success",
            count = count,
        )
        updateUiState(scanId, ProcessingUiState.Success(count))
        removePollingJob(scanId)
    }

    suspend fun failScan(scanId: Int, message: String? = null, timedOut: Boolean = false) {
        dao.updateStatusAndCountById(
            scanId = scanId,
            status = "Error",
            count = 0,
        )
        updateUiState(
            scanId,
            ProcessingUiState.Error(
                message = message ?: "Processing failed.",
                timedOut = timedOut,
            ),
        )
        removePollingJob(scanId)
    }

    fun ensurePendingScansProcessing() {
        repositoryScope.launch {
            dao.getPendingScans().forEach { scan ->
                ensureProcessingStarted(scan.id, scan.imageUri)
            }
        }
    }

    suspend fun ensureProcessingStarted(scanId: Int, imagePath: String) {
        pollingMutex.withLock {
            val existing = pollingJobs[scanId]
            if (existing?.isActive == true) return

            val scan = dao.getById(scanId) ?: return
            if (scan.status != "Pending") {
                pollingJobs.remove(scanId)
                return
            }

            pollingJobs[scanId] = repositoryScope.launch {
                val startedAt = System.currentTimeMillis()
                val job = processingService.startJob(imagePath)
                updateUiState(scanId, ProcessingUiState.Processing(elapsedMillis = 0L))

                try {
                    while (true) {
                        val latestScan = dao.getById(scanId) ?: break
                        if (latestScan.status != "Pending") break

                        val elapsed = System.currentTimeMillis() - startedAt

                        // TEMP TESTING ONLY:
                        // Change to 5 * 60 * 1000L when backend integration is ready.
                        val timeoutMillis = 30_000L
                        // END TEMP TESTING ONLY

                        if (elapsed >= timeoutMillis) {
                            failScan(
                                scanId = scanId,
                                message = "Processing timed out. Please try again.",
                                timedOut = true,
                            )
                            break
                        }

                        when (val state = processingService.pollJob(job.jobId)) {
                            ProcessingJobState.Processing -> {
                                updateUiState(
                                    scanId,
                                    ProcessingUiState.Processing(elapsedMillis = elapsed),
                                )
                            }

                            is ProcessingJobState.Success -> {
                                completeScan(scanId, state.count)
                                break
                            }

                            is ProcessingJobState.Error -> {
                                failScan(scanId, state.message)
                                break
                            }
                        }

                        // TEMP TESTING ONLY:
                        // Poll every 5 seconds with short independent requests.
                        delay(5_000L)
                        // END TEMP TESTING ONLY
                    }
                } finally {
                    removePollingJob(scanId)
                }
            }
        }
    }

    private fun updateUiState(scanId: Int, uiState: ProcessingUiState) {
        processingStates.value = processingStates.value + (scanId to uiState)
    }

    private suspend fun removePollingJob(scanId: Int) {
        pollingMutex.withLock {
            pollingJobs.remove(scanId)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ScanRepository? = null

        fun getInstance(context: Context): ScanRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScanRepository(
                    dao = AppDatabase.getInstance(context).scanDao(),
                ).also { INSTANCE = it }
            }
    }
}
