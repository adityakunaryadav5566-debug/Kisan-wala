package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farm_profile")
data class FarmProfileEntity(
    @PrimaryKey val id: Int = 1,
    val farmerName: String,
    val cropName: String,
    val location: String,
    val plantingDateMillis: Long,
    val soilType: String,
    val preferredLanguage: String
)

@Entity(tableName = "crop_reports")
data class CropReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val reportType: String,
    val cropName: String,
    val titleHindi: String,
    val summaryHindi: String,
    val detailsHindi: String,
    val imageUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
