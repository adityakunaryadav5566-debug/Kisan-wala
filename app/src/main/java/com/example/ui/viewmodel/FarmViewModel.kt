package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CropReportEntity
import com.example.data.local.FarmProfileEntity
import com.example.data.local.FarmRepository
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FarmRepository(AppDatabase.getInstance(application).farmDao())

    val farmProfile: StateFlow<FarmProfileEntity?> = repository.farmProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    val cropReports: StateFlow<List<CropReportEntity>> = repository.cropReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _profileSavedEvent = MutableStateFlow(false)
    val profileSavedEvent: StateFlow<Boolean> = _profileSavedEvent.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultProfile()
        }
    }

    fun saveProfile(
        farmerName: String,
        cropName: String,
        location: String,
        plantingDateMillis: Long,
        soilType: String,
        language: AppLanguage
    ) {
        viewModelScope.launch {
            repository.saveFarmProfile(
                FarmProfileEntity(
                    id = 1,
                    farmerName = farmerName.trim().ifBlank { "किसान भाई" },
                    cropName = cropName.trim().ifBlank { "गेहूँ (Wheat)" },
                    location = location.trim().ifBlank { "भारत" },
                    plantingDateMillis = plantingDateMillis,
                    soilType = soilType.trim().ifBlank { "दोमट मिट्टी (Loamy)" },
                    preferredLanguage = language.code
                )
            )
            _profileSavedEvent.value = true
        }
    }

    fun consumeProfileSavedEvent() {
        _profileSavedEvent.value = false
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }
}
