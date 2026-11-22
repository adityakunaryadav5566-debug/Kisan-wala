package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.SoilScanResult
import com.example.platform.ImageCaptureHelper
import com.example.ui.components.CropQuickSelectorRow
import com.example.ui.components.FirebaseRequiredNoticeBanner
import com.example.ui.components.GlowingEmeraldButton
import com.example.ui.components.KisanGlassCard
import com.example.ui.components.TopScreenHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.HarvestGoldSoft
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.AnalysisUiState

@Composable
fun SoilScannerScreen(
    defaultCropName: String,
    defaultLocation: String,
    photoUri: Uri?,
    scanState: AnalysisUiState<SoilScanResult>,
    onPhotoSelected: (Uri?) -> Unit,
    onAnalyzeClick: (cropName: String, locationNote: String) -> Unit,
    onSaveReport: (cropName: String, result: SoilScanResult) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var cropName by remember(defaultCropName) { mutableStateOf(defaultCropName) }
    var locationNote by remember(defaultLocation) { mutableStateOf(defaultLocation) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            permissionMessage = null
            onPhotoSelected(uri)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onPhotoSelected(pendingCameraUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ImageCaptureHelper.createCameraCaptureUri(context, "soil")
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            permissionMessage = "कैमरा अनुमति नहीं मिली। आप 'गैलरी से चुनें' से मिट्टी की फोटो अपलोड कर सकते हैं।"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        TopScreenHeader(
            titleHindi = "AI मिट्टी स्कैनर",
            subtitleEnglish = "AI Soil Scanner • Preliminary Condition & Lab Guide",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FirebaseRequiredNoticeBanner()
            }

            item {
                KisanGlassCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(EmeraldDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = stringResource(R.string.cd_soil_preview),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_soil_scanner),
                                contentDescription = stringResource(R.string.cd_soil_preview),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                alpha = 0.72f
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldDeep.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "खेत की मिट्टी की साफ फोटो अपलोड करें",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    val uri = ImageCaptureHelper.createCameraCaptureUri(context, "soil")
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("soil_camera_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldElevated,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = HarvestGoldBright)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("कैमरा", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("soil_gallery_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, HarvestGoldBright),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HarvestGoldBright)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("गैलरी से चुनें", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    permissionMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = HarvestGoldBright)
                    }
                }
            }

            item {
                KisanGlassCard {
                    Text(
                        text = "फसल और क्षेत्र का विवरण",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CropQuickSelectorRow(
                        selectedCrop = cropName,
                        onCropSelected = { cropName = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = locationNote,
                        onValueChange = { locationNote = it },
                        label = { Text("गाँव / जिला (Location)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("soil_location_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarvestGoldBright,
                            unfocusedBorderColor = EmeraldBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedLabelColor = HarvestGoldBright,
                            unfocusedLabelColor = MutedSage
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlowingEmeraldButton(
                        text = "मिट्टी की प्रारंभिक जांच करें",
                        icon = Icons.Filled.Terrain,
                        onClick = { onAnalyzeClick(cropName, locationNote) },
                        enabled = scanState !is AnalysisUiState.Loading,
                        isGoldAccent = true,
                        modifier = Modifier.testTag("soil_analyze_button")
                    )
                }
            }

            item {
                AnimatedVisibility(visible = scanState !is AnalysisUiState.Idle) {
                    when (scanState) {
                        AnalysisUiState.Idle -> Unit
                        AnalysisUiState.Loading -> {
                            KisanGlassCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = HarvestGoldBright,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "मिट्टी का विश्लेषण हो रहा है...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                }
                            }
                        }

                        is AnalysisUiState.Error -> {
                            KisanGlassCard(borderColor = AlertRed, containerColor = AlertRedBg) {
                                Text(
                                    text = scanState.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PureWhite
                                )
                            }
                        }

                        is AnalysisUiState.Success -> {
                            val res = scanState.data
                            KisanGlassCard(borderColor = HarvestGoldBright) {
                                Text(
                                    text = res.preliminarySoilType,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = HarvestGoldBright
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.surfaceConditionNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FreshGreenSoft
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "संभावित पोषक तत्व समस्याएं:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FreshGreenBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.possibleNutrientProblems.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "आवश्यक लैब मिट्टी परीक्षण (Recommended Lab Tests):",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HarvestGoldBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.recommendedLabTests.forEach { test ->
                                    Text(
                                        text = test,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = WarningAmberBg,
                                    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.7f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Science,
                                            contentDescription = null,
                                            tint = HarvestGoldBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = res.photoLimitationDisclaimer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = HarvestGoldSoft
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { onSaveReport(cropName, res) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("save_soil_report_button"),
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

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
