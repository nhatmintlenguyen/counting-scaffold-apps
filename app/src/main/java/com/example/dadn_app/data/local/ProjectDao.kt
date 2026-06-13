package com.example.dadn_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM scan_projects ORDER BY id DESC")
    fun getAll(): Flow<List<ScanProject>>

    @Query("SELECT * FROM scan_projects WHERE id = :projectId LIMIT 1")
    suspend fun getById(projectId: Int): ScanProject?

    @Insert
    suspend fun insert(project: ScanProject): Long
}
