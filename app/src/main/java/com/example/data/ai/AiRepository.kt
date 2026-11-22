package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.model.AppLanguage
import com.example.data.model.FertilizerAdviceResult
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.SoilScanResult
import com.example.data.model.WaterAdviceResult
import kotlinx.coroutines.flow.Flow

/**
 * Single AI boundary for all vision, advisory, and conversational features.
 * Currently backed by PlaceholderAiRepository until Firebase AI Logic is enabled.
 */
interface AiRepository {
    val isLiveAiConnected: Boolean

    suspend fun analyzePlantLeaf(
        imageBitmap: Bitmap?,
        cropName: String,
        symptomsNote: String
    ): PlantDiagnosisResult

    suspend fun analyzeSoilPhoto(
        imageBitmap: Bitmap?,
        cropName: String,
        locationNote: String
    ): SoilScanResult

    suspend fun getFertilizerAdvice(
        cropName: String,
        growthStage: String,
        soilTestSummary: String,
        symptoms: String
    ): FertilizerAdviceResult

    suspend fun getWaterAdvice(
        cropName: String,
        growthStage: String,
        soilMoisturePercent: Float?,
        weatherCondition: String
    ): WaterAdviceResult

    fun streamChatResponse(
        query: String,
        language: AppLanguage,
        farmContext: String
    ): Flow<String>
}
