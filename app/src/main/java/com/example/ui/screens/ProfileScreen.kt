package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CropReportEntity
import com.example.data.local.FarmProfileEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ReportType
import com.example.ui.components.CropQuickSelectorRow
import com.example.ui.components.GlowingEmeraldButton
import com.example.ui.components.KisanGlassCard
import com.example.ui.components.ReportDetailDialog
import com.example.ui.components.TopScreenHeader
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    farmProfile: FarmProfileEntity?,
    reports: List<CropReportEntity>,
    onSaveProfile: (
        farmerName: String,
        cropName: String,
        location: String,
        plantingDateMillis: Long,
        soilType: String,
        language: AppLanguage
    ) -> Unit,
    onDeleteReport: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    var farmerName by remember(farmProfile?.farmerName) {
        mutableStateOf(farmProfile?.farmerName ?: "किसान भाई")
    }
    var cropName by remember(farmProfile?.cropName) {
        mutableStateOf(farmProfile?.cropName ?: "गेहूँ (Wheat)")
    }
    var location by remember(farmProfile?.location) {
        mutableStateOf(farmProfile?.location ?: "वाराणसी, उत्तर प्रदेश")
    }
    var soilType by remember(farmProfile?.soilType) {
        mutableStateOf(farmProfile?.soilType ?: "दोमट मिट्टी (Loamy)")
    }
    var plantingDateMillis by remember(farmProfile?.plantingDateMillis) {
        mutableLongStateOf(farmProfile?.plantingDateMillis ?: System.currentTimeMillis())
    }
    var preferredLanguage by remember(farmProfile?.preferredLanguage) {
        mutableStateOf(AppLanguage.fromCode(farmProfile?.preferredLanguage))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<CropReportEntity?>(null) }

    val formattedDate = remember(plantingDateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(plantingDateMillis))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        TopScreenHeader(
            titleHindi = "खेत प्रोफ़ाइल (Farm Profile)",
            subtitleEnglish = "Crop, Location, Planting Date & Saved Reports",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                KisanGlassCard {
                    Text(
                        text = "अपनी खेती की जानकारी भरें",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = farmerName,
                        onValueChange = { farmerName = it },
                        label = { Text("किसान का नाम (Farmer Name)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "मुख्य फसल (Select Crop):",
                        style = MaterialTheme.typography.labelLarge,
                        color = FreshGreenBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CropQuickSelectorRow(
                        selectedCrop = cropName,
                        onCropSelected = { cropName = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("फसल का नाम (Crop Name)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_crop_input"),
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("गाँव / शहर / राज्य (Location)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_location_input"),
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = { soilType = it },
                        label = { Text("मिट्टी का प्रकार (Soil Type)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_soil_input"),
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("profile_date_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, HarvestGoldBright)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = HarvestGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "बुवाई की तारीख (Planting Date): $formattedDate",
                            color = PureWhite,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "पसंदीदा भाषा (Language):",
                        style = MaterialTheme.typography.labelLarge,
                        color = FreshGreenBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = preferredLanguage == lang,
                                onClick = { preferredLanguage = lang },
                                label = { Text(lang.nativeLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = EmeraldCard,
                                    labelColor = PureWhite,
                                    selectedContainerColor = HarvestGoldBright,
                                    selectedLabelColor = EmeraldDeep
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    GlowingEmeraldButton(
                        text = "प्रोफ़ाइल सेव करें (Save Profile)",
                        icon = Icons.Filled.Save,
                        onClick = {
                            onSaveProfile(
                                farmerName,
                                cropName,
                                location,
                                plantingDateMillis,
                                soilType,
                                preferredLanguage
                            )
                        },
                        modifier = Modifier.testTag("profile_save_button")
                    )
                }
            }

            item {
                Text(
                    text = "सेव की गई फसल रिपोर्ट (${reports.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
            }

            if (reports.isEmpty()) {
                item {
                    KisanGlassCard {
                        Text(
                            text = "अभी कोई सेव की गई रिपोर्ट नहीं है। पत्ती डॉक्टर, मिट्टी स्कैनर या खाद/सिंचाई सलाह से रिपोर्ट सेव करें।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSage
                        )
                    }
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    val type = ReportType.fromName(report.reportType)
                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(report.createdAtMillis))
                    KisanGlassCard(
                        modifier = Modifier.clickable { selectedReport = report }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${type.labelHindi} • ${report.cropName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = HarvestGoldBright
                                )
                                Text(
                                    text = report.titleHindi,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSage
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedSage)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = plantingDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { plantingDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("चुनें", color = FreshGreenBright)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("रद्द करें", color = MutedSage)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    selectedReport?.let { report ->
        ReportDetailDialog(
            report = report,
            onDismiss = { selectedReport = null },
            onDelete = {
                onDeleteReport(report.id)
                selectedReport = null
            }
        )
    }
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FreshGreenBright,
    unfocusedBorderColor = EmeraldBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedLabelColor = FreshGreenBright,
    unfocusedLabelColor = MutedSage
)
