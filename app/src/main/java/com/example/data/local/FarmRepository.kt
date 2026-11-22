package com.example.data.local

import com.example.data.model.AppLanguage
import com.example.data.model.ReportType
import kotlinx.coroutines.flow.Flow

class FarmRepository(private val farmDao: FarmDao) {

    val farmProfile: Flow<FarmProfileEntity?> = farmDao.observeFarmProfile()
    val cropReports: Flow<List<CropReportEntity>> = farmDao.observeAllReports()

    suspend fun ensureDefaultProfile() {
        if (farmDao.getFarmProfileOnce() == null) {
            farmDao.upsertFarmProfile(
                FarmProfileEntity(
                    id = 1,
                    farmerName = "किसान भाई",
                    cropName = "गेहूँ (Wheat)",
                    location = "वाराणसी, उत्तर प्रदेश",
                    plantingDateMillis = System.currentTimeMillis() - (28L * 24 * 60 * 60 * 1000),
                    soilType = "दोमट मिट्टी (Loamy)",
                    preferredLanguage = AppLanguage.HINDI.code
                )
            )
        }
    }

    suspend fun saveFarmProfile(profile: FarmProfileEntity) {
        farmDao.upsertFarmProfile(profile.copy(id = 1))
    }

    suspend fun saveReport(
        type: ReportType,
        cropName: String,
        titleHindi: String,
        summaryHindi: String,
        detailsHindi: String,
        imageUri: String? = null
    ): Long {
        return farmDao.insertReport(
            CropReportEntity(
                reportType = type.name,
                cropName = cropName,
                titleHindi = titleHindi,
                summaryHindi = summaryHindi,
                detailsHindi = detailsHindi,
                imageUri = imageUri,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteReport(reportId: Long) {
        farmDao.deleteReportById(reportId)
    }
}
