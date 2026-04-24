package com.example.dadn_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    /** Emits the full list every time the table changes, newest first. */
    @Query("SELECT * FROM scans ORDER BY id DESC")
    fun getAll(): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    fun observeById(scanId: Int): Flow<ScanRecord?>

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    suspend fun getById(scanId: Int): ScanRecord?

    @Query("SELECT * FROM scans WHERE status = 'Pending' ORDER BY id DESC LIMIT 1")
    fun observeCurrentActiveScan(): Flow<ScanRecord?>

    @Query("SELECT * FROM scans WHERE status = 'Pending' ORDER BY id DESC")
    suspend fun getPendingScans(): List<ScanRecord>

    @Insert
    suspend fun insert(scan: ScanRecord): Long

    @Query("UPDATE scans SET status = :status, count = :count WHERE id = :scanId")
    suspend fun updateStatusAndCountById(
        scanId: Int,
        status: String,
        count: Int,
    )

    @Query("UPDATE scans SET status = :status, count = :count WHERE imageUri = :imageUri")
    suspend fun updateStatusAndCountByImageUri(
        imageUri: String,
        status: String,
        count: Int,
    )
}
