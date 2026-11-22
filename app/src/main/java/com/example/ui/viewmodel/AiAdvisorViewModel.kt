package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.FarmRepository
import com.example.data.model.FertilizerAdviceResult
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.ReportType
import com.example.data.model.SoilScanResult
import com.example.data.model.WaterAdviceResult
import com.example.platform.ImageCaptureHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalysisUiState<out T> {
    data object Idle : AnalysisUiState<Nothing>
    data object Loading : AnalysisUiState<Nothing>
    data class Success<T>(val data: T) : AnalysisUiState<T>
    data class Error(val message: String) : AnalysisUiState<Nothing>
}

class AiAdvisorViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository: AiRepository = PlaceholderAiRepository()
    private val farmRepository = FarmRepository(AppDatabase.getInstance(application).farmDao())

    val isLiveAiConnected: Boolean = aiRepository.isLiveAiConnected

    // Plant Doctor state
    private val _plantPhotoUri = MutableStateFlow<Uri?>(null)
    val plantPhotoUri: StateFlow<Uri?> = _plantPhotoUri.asStateFlow()

    private val _plantDiagnosisState = MutableStateFlow<AnalysisUiState<PlantDiagnosisResult>>(AnalysisUiState.Idle)
    val plantDiagnosisState: StateFlow<AnalysisUiState<PlantDiagnosisResult>> = _plantDiagnosisState.asStateFlow()

    // Soil Scanner state
    private val _soilPhotoUri = MutableStateFlow<Uri?>(null)
    val soilPhotoUri: StateFlow<Uri?> = _soilPhotoUri.asStateFlow()

    private val _soilScanState = MutableStateFlow<AnalysisUiState<SoilScanResult>>(AnalysisUiState.Idle)
    val soilScanState: StateFlow<AnalysisUiState<SoilScanResult>> = _soilScanState.asStateFlow()

    // Fertilizer Advisor state
    private val _fertilizerState = MutableStateFlow<AnalysisUiState<FertilizerAdviceResult>>(AnalysisUiState.Idle)
    val fertilizerState: StateFlow<AnalysisUiState<FertilizerAdviceResult>> = _fertilizerState.asStateFlow()

    // Water Advisor state
    private val _waterState = MutableStateFlow<AnalysisUiState<WaterAdviceResult>>(AnalysisUiState.Idle)
    val waterState: StateFlow<AnalysisUiState<WaterAdviceResult>> = _waterState.asStateFlow()

    private val _reportSavedMessage = MutableStateFlow<String?>(null)
    val reportSavedMessage: StateFlow<String?> = _reportSavedMessage.asStateFlow()

    private var cachedPlantBitmap: Bitmap? = null
    private var cachedSoilBitmap: Bitmap? = null

    fun setPlantPhoto(uri: Uri?) {
        _plantPhotoUri.value = uri
        _plantDiagnosisState.value = AnalysisUiState.Idle
        cachedPlantBitmap = null
        if (uri != null) {
            viewModelScope.launch {
                cachedPlantBitmap = ImageCaptureHelper.decodeSampledBitmap(getApplication(), uri)
            }
        }
    }

    fun analyzePlantLeaf(cropName: String, symptomsNote: String) {
        val uri = _plantPhotoUri.value
        if (uri == null && symptomsNote.isBlank()) {
            _plantDiagnosisState.value = AnalysisUiState.Error("कृपया पत्ती की फोटो अपलोड करें या लक्षण लिखें।")
            return
        }
        viewModelScope.launch {
            _plantDiagnosisState.value = AnalysisUiState.Loading
            try {
                if (cachedPlantBitmap == null && uri != null) {
                    cachedPlantBitmap = ImageCaptureHelper.decodeSampledBitmap(getApplication(), uri)
                }
                val result = aiRepository.analyzePlantLeaf(cachedPlantBitmap, cropName, symptomsNote)
                _plantDiagnosisState.value = AnalysisUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _plantDiagnosisState.value = AnalysisUiState.Error(
                    e.localizedMessage ?: "पत्ती विश्लेषण में समस्या आई। कृपया पुनः प्रयास करें।"
                )
            }
        }
    }

    fun savePlantReport(cropName: String, result: PlantDiagnosisResult) {
        viewModelScope.launch {
            val details = buildString {
                appendLine("संभावित कारण:")
                result.possibleIssues.forEach { appendLine("• $it") }
                appendLine("\nपौधा क्यों नहीं बढ़ रहा:")
                appendLine(result.growthStuntExplanation)
                appendLine("\nउपचार सुझाव:")
                result.treatmentStepsHindi.forEach { appendLine(it) }
                appendLine("\n${result.uncertaintyNote}")
                appendLine(result.expertAdviceNote)
            }
            farmRepository.saveReport(
                type = ReportType.PLANT_DOCTOR,
                cropName = cropName.ifBlank { "फसल" },
                titleHindi = result.titleHindi,
                summaryHindi = result.possibleIssues.firstOrNull().orEmpty(),
                detailsHindi = details,
                imageUri = _plantPhotoUri.value?.toString()
            )
            _reportSavedMessage.value = "पत्ती जांच रिपोर्ट सेव हो गई!"
        }
    }

    fun setSoilPhoto(uri: Uri?) {
        _soilPhotoUri.value = uri
        _soilScanState.value = AnalysisUiState.Idle
        cachedSoilBitmap = null
        if (uri != null) {
            viewModelScope.launch {
                cachedSoilBitmap = ImageCaptureHelper.decodeSampledBitmap(getApplication(), uri)
            }
        }
    }

    fun analyzeSoilPhoto(cropName: String, locationNote: String) {
        val uri = _soilPhotoUri.value
        if (uri == null) {
            _soilScanState.value = AnalysisUiState.Error("कृपया पहले मिट्टी की साफ फोटो चुनें या खींचें।")
            return
        }
        viewModelScope.launch {
            _soilScanState.value = AnalysisUiState.Loading
            try {
                if (cachedSoilBitmap == null) {
                    cachedSoilBitmap = ImageCaptureHelper.decodeSampledBitmap(getApplication(), uri)
                }
                val result = aiRepository.analyzeSoilPhoto(cachedSoilBitmap, cropName, locationNote)
                _soilScanState.value = AnalysisUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _soilScanState.value = AnalysisUiState.Error(
                    e.localizedMessage ?: "मिट्टी विश्लेषण विफल रहा।"
                )
            }
        }
    }

    fun saveSoilReport(cropName: String, result: SoilScanResult) {
        viewModelScope.launch {
            val details = buildString {
                appendLine("प्रारंभिक मिट्टी प्रकार: ${result.preliminarySoilType}")
                appendLine("सतही स्थिति: ${result.surfaceConditionNote}")
                appendLine("\nसंभावित पोषक तत्व समस्या:")
                result.possibleNutrientProblems.forEach { appendLine("• $it") }
                appendLine("\nजरूरी लैब टेस्ट:")
                result.recommendedLabTests.forEach { appendLine(it) }
                appendLine("\n${result.photoLimitationDisclaimer}")
            }
            farmRepository.saveReport(
                type = ReportType.SOIL_SCANNER,
                cropName = cropName.ifBlank { "फसल" },
                titleHindi = result.preliminarySoilType,
                summaryHindi = result.surfaceConditionNote,
                detailsHindi = details,
                imageUri = _soilPhotoUri.value?.toString()
            )
            _reportSavedMessage.value = "मिट्टी स्कैन रिपोर्ट सेव हो गई!"
        }
    }

    fun generateFertilizerAdvice(
        cropName: String,
        growthStage: String,
        soilTestSummary: String,
        symptoms: String
    ) {
        viewModelScope.launch {
            _fertilizerState.value = AnalysisUiState.Loading
            try {
                val result = aiRepository.getFertilizerAdvice(cropName, growthStage, soilTestSummary, symptoms)
                _fertilizerState.value = AnalysisUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _fertilizerState.value = AnalysisUiState.Error(e.localizedMessage ?: "खाद सलाह त्रुटि")
            }
        }
    }

    fun saveFertilizerReport(cropName: String, result: FertilizerAdviceResult) {
        viewModelScope.launch {
            val details = buildString {
                appendLine("सुझाए गए उर्वरक:")
                result.recommendedFertilizers.forEach { appendLine("• $it") }
                appendLine("\nउद्देश्य: ${result.purposeHindi}")
                appendLine("\nसुरक्षित मात्रा: ${result.safeDosageHindi}")
                appendLine("\nसमय: ${result.applicationTimingHindi}")
                appendLine("\n${result.excessiveUseWarning}")
            }
            farmRepository.saveReport(
                type = ReportType.FERTILIZER_PLAN,
                cropName = cropName.ifBlank { "फसल" },
                titleHindi = "$cropName — स्मार्ट खाद योजना",
                summaryHindi = result.recommendedFertilizers.firstOrNull().orEmpty(),
                detailsHindi = details
            )
            _reportSavedMessage.value = "खाद योजना रिपोर्ट सेव हो गई!"
        }
    }

    fun generateWaterAdvice(
        cropName: String,
        growthStage: String,
        soilMoisturePercent: Float?,
        weatherCondition: String
    ) {
        viewModelScope.launch {
            _waterState.value = AnalysisUiState.Loading
            try {
                val result = aiRepository.getWaterAdvice(cropName, growthStage, soilMoisturePercent, weatherCondition)
                _waterState.value = AnalysisUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _waterState.value = AnalysisUiState.Error(e.localizedMessage ?: "सिंचाई सलाह त्रुटि")
            }
        }
    }

    fun saveWaterReport(cropName: String, result: WaterAdviceResult) {
        viewModelScope.launch {
            val details = buildString {
                appendLine(result.recommendationHeadline)
                appendLine("\nसिंचाई समय: ${result.timingHindi}")
                appendLine("\nनमी व मौसम: ${result.weatherAndMoistureNote}")
                appendLine("\n${result.waterSavingTip}")
            }
            farmRepository.saveReport(
                type = ReportType.WATER_PLAN,
                cropName = cropName.ifBlank { "फसल" },
                titleHindi = result.recommendationHeadline,
                summaryHindi = result.weatherAndMoistureNote,
                detailsHindi = details
            )
            _reportSavedMessage.value = "सिंचाई सलाह रिपोर्ट सेव हो गई!"
        }
    }

    fun clearSavedMessage() {
        _reportSavedMessage.value = null
    }
}
