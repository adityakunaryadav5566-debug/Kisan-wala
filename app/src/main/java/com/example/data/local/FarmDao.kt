package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farm_profile WHERE id = 1")
    fun observeFarmProfile(): Flow<FarmProfileEntity?>

    @Query("SELECT * FROM farm_profile WHERE id = 1")
    suspend fun getFarmProfileOnce(): FarmProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFarmProfile(profile: FarmProfileEntity)

    @Query("SELECT * FROM crop_reports ORDER BY createdAtMillis DESC")
    fun observeAllReports(): Flow<List<CropReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CropReportEntity): Long

    @Query("DELETE FROM crop_reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: Long)
}
