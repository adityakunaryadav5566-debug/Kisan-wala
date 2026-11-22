package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CropReportEntity
import com.example.data.local.FarmProfileEntity
import com.example.data.model.BleSensorReading
import com.example.data.model.ReportType
import com.example.ui.components.FirebaseRequiredNoticeBanner
import com.example.ui.components.KisanGlassCard
import com.example.ui.components.ReportDetailDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.HarvestGoldSoft
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    farmProfile: FarmProfileEntity?,
    reports: List<CropReportEntity>,
    sensorReading: BleSensorReading?,
    isWideScreen: Boolean,
    onScanLeafClick: () -> Unit,
    onScanSoilClick: () -> Unit,
    onAskAiClick: () -> Unit,
    onWaterAdviceClick: () -> Unit,
    onFertilizerAdviceClick: () -> Unit,
    onSensorClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDeleteReport: (Long) -> Unit
) {
    var selectedReport by remember { mutableStateOf<CropReportEntity?>(null) }

    val daysSincePlanting = remember(farmProfile?.plantingDateMillis) {
        val planted = farmProfile?.plantingDateMillis ?: System.currentTimeMillis()
        max(0L, (System.currentTimeMillis() - planted) / (24L * 60 * 60 * 1000L))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Futuristic Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, FreshGreen.copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_farm_hero),
                        contentDescription = stringResource(R.string.cd_hero_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        EmeraldDeep.copy(alpha = 0.55f),
                                        EmeraldDeep.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldCard.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, HarvestGoldBright.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Eco,
                                        contentDescription = null,
                                        tint = FreshGreenBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KISAN WALA",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = HarvestGoldBright
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .clickable(onClick = onProfileClick)
                                    .testTag("dashboard_profile_chip"),
                                color = EmeraldElevated.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, EmeraldBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "खेत प्रोफ़ाइल",
                                        tint = FreshGreenBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = farmProfile?.farmerName ?: "प्रोफ़ाइल",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "नमस्ते ${farmProfile?.farmerName ?: "किसान भाई"}!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = FreshGreen.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, FreshGreenBright.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "🌱 ${farmProfile?.cropName ?: "गेहूँ (Wheat)"} • दिन $daysSincePlanting",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = FreshGreenSoft,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Text(
                                    text = farmProfile?.location ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HarvestGoldSoft,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Sample AI Status Notice
        item {
            FirebaseRequiredNoticeBanner()
        }

        // 3. Live Soil Sensor Summary Strip
        item {
            KisanGlassCard(
                modifier = Modifier
                    .clickable(onClick = onSensorClick)
                    .testTag("dashboard_sensor_card"),
                borderColor = if (sensorReading != null) FreshGreen else EmeraldBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BluetoothConnected,
                                contentDescription = "ब्लूटूथ सेंसर",
                                tint = FreshGreenBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "लाइव मिट्टी सेंसर (Bluetooth)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Text(
                                text = if (sensorReading != null) {
                                    val m = sensorReading.moisturePercent?.roundToInt()?.toString() ?: "--"
                                    val t = sensorReading.temperatureCelsius?.let { String.format(Locale.US, "%.1f", it) } ?: "--"
                                    val badge = if (sensorReading.isDemoSimulation) " [डेमो]" else ""
                                    "नमी: $m%  •  तापमान: $t°C$badge"
                                } else {
                                    "सेंसर कनेक्ट करने के लिए टैप करें"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if ((sensorReading?.moisturePercent ?: 50f) < 30f) AlertRed else FreshGreenSoft
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = HarvestGoldBright
                    )
                }
            }
        }

        // 4. Primary Action Grid (Big Farmer-Friendly Cards)
        item {
            Text(
                text = "स्मार्ट कृषि सेवाएं (Smart Tools)",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            val actions = listOf(
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_scan_leaf),
                    subtitle = "AI Plant Doctor",
                    icon = Icons.Filled.LocalHospital,
                    accentColor = FreshGreenBright,
                    tag = "action_scan_leaf",
                    onClick = onScanLeafClick
                ),
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_scan_soil),
                    subtitle = "AI Soil Scanner",
                    icon = Icons.Filled.Landscape,
                    accentColor = HarvestGoldBright,
                    tag = "action_scan_soil",
                    onClick = onScanSoilClick
                ),
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_ask_ai),
                    subtitle = "हिन्दी • भोजपुरी Voice",
                    icon = Icons.Filled.GraphicEq,
                    accentColor = FreshGreenBright,
                    tag = "action_ask_ai",
                    onClick = onAskAiClick
                ),
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_water_advice),
                    subtitle = "Smart Water Advisor",
                    icon = Icons.Filled.WaterDrop,
                    accentColor = Color(0xFF38BDF8),
                    tag = "action_water_advice",
                    onClick = onWaterAdviceClick
                ),
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_fertilizer_advice),
                    subtitle = "Smart Fertilizer",
                    icon = Icons.Filled.Science,
                    accentColor = HarvestGoldBright,
                    tag = "action_fertilizer_advice",
                    onClick = onFertilizerAdviceClick
                ),
                DashboardActionItem(
                    titleHindi = stringResource(R.string.btn_ble_sensor),
                    subtitle = "BLE Moisture & Temp",
                    icon = Icons.Filled.Agriculture,
                    accentColor = FreshGreenBright,
                    tag = "action_ble_sensor",
                    onClick = onSensorClick
                )
            )

            val columns = if (isWideScreen) 3 else 2
            val chunked = actions.chunked(columns)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            DashboardActionCard(
                                item = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < columns) {
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 5. Previous Reports Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "पिछली रिपोर्ट (Previous Reports)",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
                Text(
                    text = "${reports.size} सेव",
                    style = MaterialTheme.typography.labelLarge,
                    color = HarvestGoldBright
                )
            }
        }

        if (reports.isEmpty()) {
            item {
                KisanGlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = MutedSage,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "अभी कोई रिपोर्ट सेव नहीं है",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "पत्ती या मिट्टी की जांच करें, या खाद/सिंचाई सलाह सेव करें — आपकी सभी रिपोर्ट यहीं दिखेंगी।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSage
                        )
                    }
                }
            }
        } else {
            items(reports.take(10), key = { it.id }) { report ->
                val type = ReportType.fromName(report.reportType)
                val dateText = remember(report.createdAtMillis) {
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        .format(Date(report.createdAtMillis))
                }
                KisanGlassCard(
                    modifier = Modifier
                        .clickable { selectedReport = report }
                        .testTag("report_card_${report.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    ReportType.PLANT_DOCTOR -> Icons.Filled.LocalHospital
                                    ReportType.SOIL_SCANNER -> Icons.Filled.Landscape
                                    ReportType.FERTILIZER_PLAN -> Icons.Filled.Science
                                    ReportType.WATER_PLAN -> Icons.Filled.WaterDrop
                                },
                                contentDescription = type.labelHindi,
                                tint = HarvestGoldBright
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${type.labelHindi} • ${report.cropName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = FreshGreenBright
                            )
                            Text(
                                text = report.titleHindi,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSage
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "विवरण देखें",
                            tint = MutedSage
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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

private data class DashboardActionItem(
    val titleHindi: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
private fun DashboardActionCard(
    item: DashboardActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = item.onClick)
            .testTag(item.tag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            EmeraldCard,
                            EmeraldElevated.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(item.accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.titleHindi,
                        tint = item.accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = item.titleHindi,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSage,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
