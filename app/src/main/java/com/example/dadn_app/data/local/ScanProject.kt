package com.example.dadn_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_projects")
data class ScanProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)
