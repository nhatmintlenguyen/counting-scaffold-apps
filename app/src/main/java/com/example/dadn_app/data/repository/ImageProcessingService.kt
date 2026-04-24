package com.example.dadn_app.data.repository

import kotlin.random.Random

sealed interface ProcessingJobState {
    data object Processing : ProcessingJobState
    data class Success(val count: Int) : ProcessingJobState
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

object MockImageProcessingService : ImageProcessingService {
    private sealed interface MockOutcome {
        data class Success(val count: Int) : MockOutcome
        data class Error(val message: String) : MockOutcome
        data object Timeout : MockOutcome
    }

    private data class MockProcessingJob(
        val jobId: String,
        val imageUri: String,
        val startedAtMillis: Long,
        val resolveAfterMillis: Long,
        val outcome: MockOutcome,
    )

    private val jobs = mutableMapOf<String, MockProcessingJob>()

    override suspend fun startJob(imageUri: String): ProcessingJob {
        val job = synchronized(jobs) {
            jobs.getOrPut(imageUri) { createJob(imageUri) }
        }
        return ProcessingJob(jobId = job.jobId, imageUri = imageUri)
    }

    override suspend fun pollJob(jobId: String): ProcessingJobState {
        val job = synchronized(jobs) { jobs[jobId] } ?: return ProcessingJobState.Error("Processing job missing")
        val elapsed = System.currentTimeMillis() - job.startedAtMillis

        return when (val outcome = job.outcome) {
            is MockOutcome.Success ->
                if (elapsed >= job.resolveAfterMillis) ProcessingJobState.Success(outcome.count)
                else ProcessingJobState.Processing

            is MockOutcome.Error ->
                if (elapsed >= job.resolveAfterMillis) ProcessingJobState.Error(outcome.message)
                else ProcessingJobState.Processing

            MockOutcome.Timeout ->
                // Let the app timeout policy own the final timeout decision.
                ProcessingJobState.Processing
        }
    }

    private fun createJob(imageUri: String): MockProcessingJob {
        val roll = Random.nextInt(100)
        val outcome: MockOutcome = when {
            roll < 65 -> MockOutcome.Success(count = Random.nextInt(12, 46))
            roll < 85 -> MockOutcome.Error(message = "Model could not validate the scaffold image.")
            else -> MockOutcome.Timeout
        }

        val resolveAfterMillis = when (outcome) {
            is MockOutcome.Success,
            is MockOutcome.Error -> Random.nextLong(10_000L, 30_001L)
            MockOutcome.Timeout -> 45_000L
        }

        return MockProcessingJob(
            jobId = imageUri,
            imageUri = imageUri,
            startedAtMillis = System.currentTimeMillis(),
            resolveAfterMillis = resolveAfterMillis,
            outcome = outcome,
        )
    }
}
