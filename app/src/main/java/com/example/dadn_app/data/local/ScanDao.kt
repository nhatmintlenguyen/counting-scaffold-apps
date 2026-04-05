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

    @Insert
    suspend fun insert(scan: ScanRecord)
}
