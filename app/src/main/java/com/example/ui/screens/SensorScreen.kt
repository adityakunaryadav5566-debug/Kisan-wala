package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BleDeviceCandidate
import com.example.data.model.BleSensorReading
import com.example.ui.components.GlowingEmeraldButton
import com.example.ui.components.KisanGlassCard
import com.example.ui.components.TopScreenHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SensorScreen(
    isScanning: Boolean,
    connectionState: String,
    discoveredDevices: List<BleDeviceCandidate>,
    currentReading: BleSensorReading?,
    statusMessage: String?,
    isSimulationMode: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnect: () -> Unit,
    onToggleSimulation: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val blePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            onStartScan()
        }
    }

    val moisture = currentReading?.moisturePercent
    val temp = currentReading?.temperatureCelsius
    val isLowMoisture = moisture != null && moisture < 30f
    val isHighTemp = temp != null && temp > 36f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        TopScreenHeader(
            titleHindi = "ब्लूटूथ मिट्टी सेंसर",
            subtitleEnglish = "BLE Soil Moisture & Temperature Monitor",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Simulation Mode Prominent Indicator if active
            if (isSimulationMode) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = WarningAmberBg,
                        border = BorderStroke(1.dp, HarvestGoldBright)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = HarvestGoldBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safety / Demo Simulation Mode सक्रिय है — ये वास्तविक हार्डवेयर सेंसर डेटा नहीं हैं।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HarvestGoldSoft
                            )
                        }
                    }
                }
            }

            // Live Gauges Card
            item {
                KisanGlassCard(borderColor = if (currentReading != null) FreshGreenBright else EmeraldBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = connectionState,
                            style = MaterialTheme.typography.titleMedium,
                            color = FreshGreenBright
                        )
                        if (currentReading != null && !isSimulationMode) {
                            OutlinedButton(
                                onClick = onDisconnect,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, EmeraldBorder)
                            ) {
                                Text("डिस्कनेक्ट", color = PureWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SensorArcGauge(
                            label = "मिट्टी नमी",
                            valueText = moisture?.let { "${it.roundToInt()}%" } ?: "--%",
                            progress = ((moisture ?: 0f) / 100f).coerceIn(0f, 1f),
                            accentColor = if (isLowMoisture) AlertRed else InfoBlue,
                            icon = Icons.Filled.WaterDrop
                        )
                        SensorArcGauge(
                            label = "मिट्टी तापमान",
                            valueText = temp?.let { String.format(Locale.US, "%.1f°C", it) } ?: "--°C",
                            progress = ((temp ?: 0f) / 55f).coerceIn(0f, 1f),
                            accentColor = if (isHighTemp) AlertRed else HarvestGoldBright,
                            icon = Icons.Filled.Thermostat
                        )
                    }
                }
            }

            // Alerts Section
            if (isLowMoisture) {
                item {
                    KisanGlassCard(borderColor = AlertRed, containerColor = AlertRedBg) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = AlertRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "कम नमी चेतावनी (Low Moisture Alert!)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                                Text(
                                    text = "मिट्टी में नमी 30% से कम है। पौधों को सूखने से बचाने के लिए सुबह या शाम हल्की सिंचाई करें।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HarvestGoldSoft
                                )
                            }
                        }
                    }
                }
            }

            if (isHighTemp) {
                item {
                    KisanGlassCard(borderColor = HarvestGoldBright, containerColor = WarningAmberBg) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Thermostat, contentDescription = null, tint = HarvestGoldBright)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "उच्च तापमान चेतावनी (High Temp Alert!)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HarvestGoldBright
                                )
                                Text(
                                    text = "तापमान 36°C से अधिक है। जड़ों को ठंडा रखने के लिए मल्चिंग (पुआल) का उपयोग करें।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }

            // Scan & Demo Switch Controls
            item {
                KisanGlassCard {
                    GlowingEmeraldButton(
                        text = if (isScanning) "स्कैन रोकें (Stop BLE Scan)" else "ब्लूटूथ सेंसर खोजें (Scan BLE)",
                        icon = Icons.Filled.BluetoothSearching,
                        onClick = {
                            if (isScanning) {
                                onStopScan()
                            } else {
                                blePermissionLauncher.launch(requiredPermissions)
                            }
                        },
                        modifier = Modifier.testTag("ble_scan_button")
                    )

                    statusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    color = FreshGreenBright,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = FreshGreenSoft)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "सिम्युलेशन डेमो मोड (No Hardware)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Text(
                                text = "बिना BLE सेंसर के रीडिंग और अलर्ट टेस्ट करने के लिए",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSage
                            )
                        }
                        Switch(
                            checked = isSimulationMode,
                            onCheckedChange = onToggleSimulation,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldDeep,
                                checkedTrackColor = HarvestGoldBright
                            ),
                            modifier = Modifier.testTag("ble_simulation_switch")
                        )
                    }
                }
            }

            if (discoveredDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "खोजे गए BLE डिवाइस (${discoveredDevices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                }
                items(discoveredDevices, key = { it.address }) { dev ->
                    KisanGlassCard(
                        modifier = Modifier
                            .clickable { onConnectDevice(dev.address) }
                            .testTag("ble_device_${dev.address}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Sensors, contentDescription = null, tint = FreshGreenBright)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(dev.name, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                                    Text(
                                        "${dev.address} • Signal ${dev.rssi} dBm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSage
                                    )
                                }
                            }
                            Text("कनेक्ट करें", style = MaterialTheme.typography.labelLarge, color = HarvestGoldBright)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun SensorArcGauge(
    label: String,
    valueText: String,
    progress: Float,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                drawArc(
                    color = EmeraldElevated,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = accentColor,
                    startAngle = 135f,
                    sweepAngle = 270f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MutedSage)
    }
}
