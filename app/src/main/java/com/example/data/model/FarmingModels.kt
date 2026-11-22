package com.example.data.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeLabel: String,
    val speechLocaleTag: String
) {
    HINDI("hi", "Hindi", "हिन्दी", "hi-IN"),
    BHOJPURI("bho", "Bhojpuri", "भोजपुरी", "hi-IN"),
    ENGLISH("en", "English", "English", "en-IN");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: HINDI
    }
}

enum class ReportType(val labelHindi: String, val labelEnglish: String) {
    PLANT_DOCTOR("पत्ती डॉक्टर", "Plant Doctor"),
    SOIL_SCANNER("मिट्टी स्कैनर", "Soil Scanner"),
    FERTILIZER_PLAN("खाद सलाह", "Fertilizer Plan"),
    WATER_PLAN("सिंचाई सलाह", "Watering Plan");

    companion object {
        fun fromName(name: String?): ReportType =
            entries.firstOrNull { it.name == name } ?: PLANT_DOCTOR
    }
}

data class PlantDiagnosisResult(
    val titleHindi: String,
    val possibleIssues: List<String>,
    val growthStuntExplanation: String,
    val treatmentStepsHindi: List<String>,
    val uncertaintyNote: String,
    val expertAdviceNote: String,
    val isSamplePreview: Boolean = true
)

data class SoilScanResult(
    val preliminarySoilType: String,
    val surfaceConditionNote: String,
    val possibleNutrientProblems: List<String>,
    val recommendedLabTests: List<String>,
    val photoLimitationDisclaimer: String,
    val isSamplePreview: Boolean = true
)

data class FertilizerAdviceResult(
    val recommendedFertilizers: List<String>,
    val purposeHindi: String,
    val safeDosageHindi: String,
    val applicationTimingHindi: String,
    val excessiveUseWarning: String,
    val isSamplePreview: Boolean = true
)

data class WaterAdviceResult(
    val shouldWaterNow: Boolean,
    val recommendationHeadline: String,
    val timingHindi: String,
    val weatherAndMoistureNote: String,
    val waterSavingTip: String,
    val isSamplePreview: Boolean = true
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val language: AppLanguage,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSampleResponse: Boolean = false
)

data class BleDeviceCandidate(
    val name: String,
    val address: String,
    val rssi: Int
)

data class BleSensorReading(
    val deviceName: String,
    val deviceAddress: String,
    val moisturePercent: Float?,
    val temperatureCelsius: Float?,
    val isDemoSimulation: Boolean,
    val timestampMillis: Long = System.currentTimeMillis()
)
