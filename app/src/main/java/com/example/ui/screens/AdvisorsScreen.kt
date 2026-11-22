package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BleSensorReading
import com.example.data.model.FertilizerAdviceResult
import com.example.data.model.WaterAdviceResult
import com.example.ui.components.CropQuickSelectorRow
import com.example.ui.components.FirebaseRequiredNoticeBanner
import com.example.ui.components.GlowingEmeraldButton
import com.example.ui.components.KisanGlassCard
import com.example.ui.components.TopScreenHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldNight
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.HarvestGoldSoft
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.AnalysisUiState
import kotlin.math.roundToInt

@Composable
fun AdvisorsScreen(
    initialTab: Int,
    defaultCropName: String,
    sensorReading: BleSensorReading?,
    fertilizerState: AnalysisUiState<FertilizerAdviceResult>,
    waterState: AnalysisUiState<WaterAdviceResult>,
    onGenerateFertilizerAdvice: (crop: String, stage: String, soilTest: String, symptoms: String) -> Unit,
    onSaveFertilizerReport: (crop: String, result: FertilizerAdviceResult) -> Unit,
    onGenerateWaterAdvice: (crop: String, stage: String, moisture: Float?, weather: String) -> Unit,
    onSaveWaterReport: (crop: String, result: WaterAdviceResult) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var cropName by remember(defaultCropName) { mutableStateOf(defaultCropName) }
    var growthStage by remember { mutableStateOf("वानस्पतिक वृद्धि (Vegetative)") }

    // Fertilizer inputs
    var soilTestSummary by remember { mutableStateOf("मिट्टी जांच रिपोर्ट अभी नहीं है") }
    var symptoms by remember { mutableStateOf("") }

    // Water inputs
    var weatherCondition by remember { mutableStateOf("सामान्य धूप (Normal)") }
    var manualMoistureText by remember(sensorReading?.moisturePercent) {
        mutableStateOf(sensorReading?.moisturePercent?.roundToInt()?.toString() ?: "")
    }

    val stages = listOf(
        "बुवाई / अंकुरण",
        "वानस्पतिक वृद्धि (Vegetative)",
        "फूल आना (Flowering)",
        "फल / दाना बनना"
    )

    val weathers = listOf(
        "सामान्य धूप (Normal)",
        "तेज धूप और गर्मी",
        "बादल / हल्की बारिश",
        "ठंडा मौसम"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        TopScreenHeader(
            titleHindi = if (selectedTab == 0) "स्मार्ट खाद सलाह" else "स्मार्ट सिंचाई सलाह",
            subtitleEnglish = if (selectedTab == 0) "Smart Fertilizer Advisor" else "Smart Watering Advisor",
            onBackClick = onBackClick
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = HarvestGoldBright,
                    activeContentColor = EmeraldDeep,
                    inactiveContainerColor = EmeraldCard,
                    inactiveContentColor = PureWhite
                ),
                modifier = Modifier.testTag("tab_fertilizer")
            ) {
                Text("खाद सलाह (Fertilizer)")
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = FreshGreenBright,
                    activeContentColor = EmeraldDeep,
                    inactiveContainerColor = EmeraldCard,
                    inactiveContentColor = PureWhite
                ),
                modifier = Modifier.testTag("tab_water")
            ) {
                Text("सिंचाई सलाह (Water)")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FirebaseRequiredNoticeBanner()
            }

            item {
                KisanGlassCard {
                    Text(
                        text = "1. फसल और अवस्था चुनें",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CropQuickSelectorRow(
                        selectedCrop = cropName,
                        onCropSelected = { cropName = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "फसल की अवस्था (Growth Stage):",
                        style = MaterialTheme.typography.labelLarge,
                        color = FreshGreenSoft
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stages.forEach { st ->
                            val isSel = growthStage == st
                            FilterChip(
                                selected = isSel,
                                onClick = { growthStage = st },
                                label = { Text(st) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = EmeraldNight,
                                    labelColor = PureWhite,
                                    selectedContainerColor = FreshGreen,
                                    selectedLabelColor = EmeraldDeep
                                )
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Fertilizer Advisor Form & Result
                item {
                    KisanGlassCard {
                        Text(
                            text = "2. मिट्टी जांच व लक्षण विवरण",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = soilTestSummary,
                            onValueChange = { soilTestSummary = it },
                            label = { Text("मिट्टी परीक्षण रिपोर्ट / pH / NPK जानकारी") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fertilizer_soil_input"),
                            colors = advisorTextFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = symptoms,
                            onValueChange = { symptoms = it },
                            label = { Text("पौधे के लक्षण (जैसे: पत्तियों में पीलापन, कम बढ़वार)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fertilizer_symptoms_input"),
                            colors = advisorTextFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GlowingEmeraldButton(
                            text = "सुरक्षित खाद सलाह प्राप्त करें",
                            icon = Icons.Filled.Science,
                            onClick = {
                                onGenerateFertilizerAdvice(cropName, growthStage, soilTestSummary, symptoms)
                            },
                            enabled = fertilizerState !is AnalysisUiState.Loading,
                            isGoldAccent = true,
                            modifier = Modifier.testTag("fertilizer_generate_button")
                        )
                    }
                }

                item {
                    AnimatedVisibility(visible = fertilizerState !is AnalysisUiState.Idle) {
                        when (fertilizerState) {
                            AnalysisUiState.Idle -> Unit
                            AnalysisUiState.Loading -> LoadingCard("खाद योजना तैयार की जा रही है...")
                            is AnalysisUiState.Error -> ErrorCard(fertilizerState.message)
                            is AnalysisUiState.Success -> {
                                val res = fertilizerState.data
                                KisanGlassCard(borderColor = HarvestGoldBright) {
                                    Text(
                                        text = "सुझाए गए उर्वरक (Recommended Fertilizers)",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = HarvestGoldBright
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    res.recommendedFertilizers.forEach { fert ->
                                        Text(
                                            text = "• $fert",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = PureWhite,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    AdvisorInfoBlock("उद्देश्य (Purpose)", res.purposeHindi)
                                    AdvisorInfoBlock("सुरक्षित मात्रा (Safe Dosage)", res.safeDosageHindi)
                                    AdvisorInfoBlock("डालने का सही समय (Timing)", res.applicationTimingHindi)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = WarningAmberBg,
                                        border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.75f))
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            Icon(
                                                imageVector = Icons.Filled.WarningAmber,
                                                contentDescription = null,
                                                tint = HarvestGoldBright
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = res.excessiveUseWarning,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = HarvestGoldSoft
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedButton(
                                        onClick = { onSaveFertilizerReport(cropName, res) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("save_fertilizer_report_button"),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, HarvestGoldBright),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HarvestGoldBright)
                                    ) {
                                        Icon(Icons.Filled.BookmarkAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.btn_save_report))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Water Advisor Form & Result
                item {
                    KisanGlassCard {
                        Text(
                            text = "2. मिट्टी नमी और मौसम स्थिति",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualMoistureText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    manualMoistureText = input
                                }
                            },
                            label = { Text("मिट्टी नमी % (यदि पता हो या सेंसर से जुड़ा हो)") },
                            placeholder = { Text("जैसे: 35") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("water_moisture_input"),
                            colors = advisorTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "मौसम (Weather):",
                            style = MaterialTheme.typography.labelLarge,
                            color = FreshGreenSoft
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            weathers.forEach { w ->
                                val isSel = weatherCondition == w
                                FilterChip(
                                    selected = isSel,
                                    onClick = { weatherCondition = w },
                                    label = { Text(w) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = EmeraldNight,
                                        labelColor = PureWhite,
                                        selectedContainerColor = InfoBlue,
                                        selectedLabelColor = EmeraldDeep
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        GlowingEmeraldButton(
                            text = "सिंचाई सलाह देखें",
                            icon = Icons.Filled.WaterDrop,
                            onClick = {
                                val moisture = manualMoistureText.toFloatOrNull()
                                onGenerateWaterAdvice(cropName, growthStage, moisture, weatherCondition)
                            },
                            enabled = waterState !is AnalysisUiState.Loading,
                            modifier = Modifier.testTag("water_generate_button")
                        )
                    }
                }

                item {
                    AnimatedVisibility(visible = waterState !is AnalysisUiState.Idle) {
                        when (waterState) {
                            AnalysisUiState.Idle -> Unit
                            AnalysisUiState.Loading -> LoadingCard("सिंचाई समय की गणना हो रही है...")
                            is AnalysisUiState.Error -> ErrorCard(waterState.message)
                            is AnalysisUiState.Success -> {
                                val res = waterState.data
                                KisanGlassCard(borderColor = if (res.shouldWaterNow) InfoBlue else FreshGreenBright) {
                                    Text(
                                        text = res.recommendationHeadline,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (res.shouldWaterNow) InfoBlue else FreshGreenBright
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    AdvisorInfoBlock("सही समय (Best Timing)", res.timingHindi)
                                    AdvisorInfoBlock("नमी और मौसम विश्लेषण", res.weatherAndMoistureNote)
                                    AdvisorInfoBlock("जल बचत सुझाव (Water Saving)", res.waterSavingTip)

                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedButton(
                                        onClick = { onSaveWaterReport(cropName, res) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("save_water_report_button"),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, FreshGreenBright),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenBright)
                                    ) {
                                        Icon(Icons.Filled.BookmarkAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.btn_save_report))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun AdvisorInfoBlock(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = FreshGreenBright)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = PureWhite)
    }
}

@Composable
private fun LoadingCard(label: String) {
    KisanGlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = FreshGreenBright, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = PureWhite)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    KisanGlassCard(borderColor = AlertRed, containerColor = AlertRedBg) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
    }
}

@Composable
private fun advisorTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FreshGreenBright,
    unfocusedBorderColor = EmeraldBorder,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedLabelColor = FreshGreenBright,
    unfocusedLabelColor = MutedSage
)
