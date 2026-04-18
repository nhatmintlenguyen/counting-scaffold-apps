package com.example.dadn_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val datetime: String,
    val count: Int,
    val fileType: String,
    val status: String,
    val imageUri: String
)
