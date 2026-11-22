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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.data.model.PlantDiagnosisResult
import com.example.platform.ImageCaptureHelper
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
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.FreshGreen
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
fun PlantDoctorScreen(
    defaultCropName: String,
    photoUri: Uri?,
    diagnosisState: AnalysisUiState<PlantDiagnosisResult>,
    onPhotoSelected: (Uri?) -> Unit,
    onAnalyzeClick: (cropName: String, symptomsNote: String) -> Unit,
    onSaveReport: (cropName: String, result: PlantDiagnosisResult) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var cropName by remember(defaultCropName) { mutableStateOf(defaultCropName) }
    var symptomsNote by remember { mutableStateOf("") }
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
            val uri = ImageCaptureHelper.createCameraCaptureUri(context, "leaf")
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            permissionMessage = "कैमरा अनुमति नहीं मिली। आप 'गैलरी से चुनें' बटन से फोटो अपलोड कर सकते हैं।"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        TopScreenHeader(
            titleHindi = "AI पत्ती डॉक्टर",
            subtitleEnglish = "AI Plant Doctor • Disease & Pest Check",
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

            // Photo Upload & Preview Card
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
                                contentDescription = stringResource(R.string.cd_plant_preview),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_plant_doctor),
                                contentDescription = stringResource(R.string.cd_plant_preview),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                alpha = 0.72f
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldDeep.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, FreshGreen.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "प्रभावित पत्ती की साफ फोटो चुनें",
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
                                    val uri = ImageCaptureHelper.createCameraCaptureUri(context, "leaf")
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("plant_camera_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldElevated,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = FreshGreenBright)
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
                                .testTag("plant_gallery_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, FreshGreenBright),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenBright)
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

            // Crop & Symptom Inputs
            item {
                KisanGlassCard {
                    Text(
                        text = "फसल चुनें और लक्षण बताएं",
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
                        value = symptomsNote,
                        onValueChange = { symptomsNote = it },
                        label = { Text("पत्ती में क्या समस्या दिख रही है? (वैकल्पिक)") },
                        placeholder = { Text("जैसे: पत्ते पीले हो रहे हैं, काले धब्बे या कीड़े दिख रहे हैं") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plant_symptoms_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FreshGreenBright,
                            unfocusedBorderColor = EmeraldBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedLabelColor = FreshGreenBright,
                            unfocusedLabelColor = MutedSage
                        ),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlowingEmeraldButton(
                        text = "AI से पत्ती की जांच करें",
                        icon = Icons.Filled.Psychology,
                        onClick = { onAnalyzeClick(cropName, symptomsNote) },
                        enabled = diagnosisState !is AnalysisUiState.Loading,
                        modifier = Modifier.testTag("plant_analyze_button")
                    )
                }
            }

            // Diagnosis Result Section
            item {
                AnimatedVisibility(visible = diagnosisState !is AnalysisUiState.Idle) {
                    when (diagnosisState) {
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
                                        color = FreshGreenBright,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "पत्ती की जांच हो रही है...",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                }
                            }
                        }

                        is AnalysisUiState.Error -> {
                            KisanGlassCard(borderColor = AlertRed, containerColor = AlertRedBg) {
                                Text(
                                    text = diagnosisState.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PureWhite
                                )
                            }
                        }

                        is AnalysisUiState.Success -> {
                            val res = diagnosisState.data
                            KisanGlassCard(borderColor = FreshGreenBright) {
                                Text(
                                    text = res.titleHindi,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = HarvestGoldBright
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "संभावित रोग / कीट / पोषण कमी:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FreshGreenBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.possibleIssues.forEach { issue ->
                                    Text(
                                        text = "• $issue",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "पौधा क्यों नहीं बढ़ रहा है?",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FreshGreenBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.growthStuntExplanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FreshGreenSoft
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "सरल हिन्दी उपचार सुझाव:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FreshGreenBright
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.treatmentStepsHindi.forEach { step ->
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = WarningAmberBg,
                                    border = BorderStroke(1.dp, HarvestGold.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.WarningAmber,
                                            contentDescription = null,
                                            tint = HarvestGoldBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = res.uncertaintyNote,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = HarvestGoldSoft
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = res.expertAdviceNote,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MutedSage
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedButton(
                                    onClick = { onSaveReport(cropName, res) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("save_plant_report_button"),
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

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
