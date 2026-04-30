package com.example.dadn_app.data.repository

data class YoloDetection(
    val id: Int,
    val label: String,
    val confidence: Float,
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float,
)
