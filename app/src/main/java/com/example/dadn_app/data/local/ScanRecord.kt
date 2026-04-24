package com.example.dadn_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single scan job persisted in the local Room database.
 *
 * [count] starts at 0 and is updated once the AI processing pipeline returns a result.
 * [status] lifecycle: "Pending" → "Success" | "Error" | "Archived"
 * [imageUri] stores the content:// or file:// URI so the image can be re-displayed later.
 */
@Entity(tableName = "scans")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val datetime: String,    // "MMM dd, yyyy • HH:mm a"
    val count: Int = 0,
    val fileType: String,    // "JPG" | "PNG" | ""
    val status: String,      // "Pending" | "Success" | "Error" | "Archived"
    val imageUri: String,    // content:// or file:// string
)
