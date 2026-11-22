package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.AdvisorsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlantDoctorScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SensorScreen
import com.example.ui.screens.SoilScannerScreen
import com.example.ui.screens.VoiceChatScreen
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.EmeraldNight
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AiAdvisorViewModel
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.SensorViewModel
import com.example.ui.viewmodel.VoiceChatViewModel

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun KisanApp(
    farmViewModel: FarmViewModel = viewModel(),
    aiAdvisorViewModel: AiAdvisorViewModel = viewModel(),
    voiceChatViewModel: VoiceChatViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val farmProfile by farmViewModel.farmProfile.collectAsStateWithLifecycle()
    val cropReports by farmViewModel.cropReports.collectAsStateWithLifecycle()
    val profileSavedEvent by farmViewModel.profileSavedEvent.collectAsStateWithLifecycle()

    val plantPhotoUri by aiAdvisorViewModel.plantPhotoUri.collectAsStateWithLifecycle()
    val plantDiagnosisState by aiAdvisorViewModel.plantDiagnosisState.collectAsStateWithLifecycle()
    val soilPhotoUri by aiAdvisorViewModel.soilPhotoUri.collectAsStateWithLifecycle()
    val soilScanState by aiAdvisorViewModel.soilScanState.collectAsStateWithLifecycle()
    val fertilizerState by aiAdvisorViewModel.fertilizerState.collectAsStateWithLifecycle()
    val waterState by aiAdvisorViewModel.waterState.collectAsStateWithLifecycle()
    val reportSavedMessage by aiAdvisorViewModel.reportSavedMessage.collectAsStateWithLifecycle()

    val chatMessages by voiceChatViewModel.messages.collectAsStateWithLifecycle()
    val selectedLanguage by voiceChatViewModel.selectedLanguage.collectAsStateWithLifecycle()
    val autoSpeak by voiceChatViewModel.autoSpeak.collectAsStateWithLifecycle()
    val isListening by voiceChatViewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by voiceChatViewModel.isSpeaking.collectAsStateWithLifecycle()
    val speakingMessageId by voiceChatViewModel.speakingMessageId.collectAsStateWithLifecycle()
    val partialTranscript by voiceChatViewModel.partialTranscript.collectAsStateWithLifecycle()
    val voiceError by voiceChatViewModel.voiceError.collectAsStateWithLifecycle()
    val isGeneratingChat by voiceChatViewModel.isGenerating.collectAsStateWithLifecycle()

    val isScanningBle by sensorViewModel.isScanning.collectAsStateWithLifecycle()
    val bleConnectionState by sensorViewModel.connectionState.collectAsStateWithLifecycle()
    val discoveredDevices by sensorViewModel.discoveredDevices.collectAsStateWithLifecycle()
    val currentSensorReading by sensorViewModel.currentReading.collectAsStateWithLifecycle()
    val sensorStatusMessage by sensorViewModel.statusMessage.collectAsStateWithLifecycle()
    val isSimulationMode by sensorViewModel.isSimulationMode.collectAsStateWithLifecycle()

    LaunchedEffect(farmProfile?.preferredLanguage) {
        farmProfile?.preferredLanguage?.let { code ->
            voiceChatViewModel.setLanguage(AppLanguage.fromCode(code))
        }
    }

    LaunchedEffect(reportSavedMessage) {
        reportSavedMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            aiAdvisorViewModel.clearSavedMessage()
        }
    }

    LaunchedEffect(profileSavedEvent) {
        if (profileSavedEvent) {
            snackbarHostState.showSnackbar("खेत प्रोफ़ाइल सेव हो गई!")
            farmViewModel.consumeProfileSavedEvent()
        }
    }

    val navItems = remember {
        listOf(
            BottomNavItem(NavRoutes.DASHBOARD, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            BottomNavItem(NavRoutes.PLANT_DOCTOR, R.string.nav_doctor, Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital, "nav_doctor"),
            BottomNavItem(NavRoutes.SOIL_SCANNER, R.string.nav_soil, Icons.Filled.Landscape, Icons.Outlined.Landscape, "nav_soil"),
            BottomNavItem(NavRoutes.VOICE_CHAT, R.string.nav_chat, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "nav_chat"),
            BottomNavItem(NavRoutes.SENSOR, R.string.nav_sensor, Icons.Filled.Sensors, Icons.Outlined.Sensors, "nav_sensor")
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val defaultCrop = farmProfile?.cropName ?: "गेहूँ (Wheat)"
    val defaultLocation = farmProfile?.location ?: "वाराणसी, उत्तर प्रदेश"
    val farmContextString = remember(farmProfile) {
        farmProfile?.let {
            "फसल: ${it.cropName}, स्थान: ${it.location}, मिट्टी: ${it.soilType}"
        }.orEmpty()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = EmeraldDeep,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = EmeraldNight,
                        contentColor = PureWhite
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(item.labelRes),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldDeep,
                                    selectedTextColor = FreshGreenBright,
                                    indicatorColor = FreshGreenBright,
                                    unselectedIconColor = MutedSage,
                                    unselectedTextColor = MutedSage
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = EmeraldNight,
                        contentColor = PureWhite
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = EmeraldDeep,
                                    selectedTextColor = HarvestGoldBright,
                                    indicatorColor = FreshGreenBright,
                                    unselectedIconColor = MutedSage,
                                    unselectedTextColor = MutedSage
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.DASHBOARD,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(NavRoutes.DASHBOARD) {
                            DashboardScreen(
                                farmProfile = farmProfile,
                                reports = cropReports,
                                sensorReading = currentSensorReading,
                                isWideScreen = isWideScreen,
                                onScanLeafClick = { navigateTopLevel(NavRoutes.PLANT_DOCTOR) },
                                onScanSoilClick = { navigateTopLevel(NavRoutes.SOIL_SCANNER) },
                                onAskAiClick = { navigateTopLevel(NavRoutes.VOICE_CHAT) },
                                onWaterAdviceClick = { navController.navigate(NavRoutes.advisorsRoute(1)) },
                                onFertilizerAdviceClick = { navController.navigate(NavRoutes.advisorsRoute(0)) },
                                onSensorClick = { navigateTopLevel(NavRoutes.SENSOR) },
                                onProfileClick = { navController.navigate(NavRoutes.PROFILE) },
                                onDeleteReport = farmViewModel::deleteReport
                            )
                        }

                        composable(NavRoutes.PLANT_DOCTOR) {
                            PlantDoctorScreen(
                                defaultCropName = defaultCrop,
                                photoUri = plantPhotoUri,
                                diagnosisState = plantDiagnosisState,
                                onPhotoSelected = aiAdvisorViewModel::setPlantPhoto,
                                onAnalyzeClick = aiAdvisorViewModel::analyzePlantLeaf,
                                onSaveReport = aiAdvisorViewModel::savePlantReport,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.SOIL_SCANNER) {
                            SoilScannerScreen(
                                defaultCropName = defaultCrop,
                                defaultLocation = defaultLocation,
                                photoUri = soilPhotoUri,
                                scanState = soilScanState,
                                onPhotoSelected = aiAdvisorViewModel::setSoilPhoto,
                                onAnalyzeClick = aiAdvisorViewModel::analyzeSoilPhoto,
                                onSaveReport = aiAdvisorViewModel::saveSoilReport,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = NavRoutes.ADVISORS_WITH_TAB,
                            arguments = listOf(
                                navArgument("tab") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                }
                            )
                        ) { backStackEntry ->
                            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                            AdvisorsScreen(
                                initialTab = initialTab,
                                defaultCropName = defaultCrop,
                                sensorReading = currentSensorReading,
                                fertilizerState = fertilizerState,
                                waterState = waterState,
                                onGenerateFertilizerAdvice = aiAdvisorViewModel::generateFertilizerAdvice,
                                onSaveFertilizerReport = aiAdvisorViewModel::saveFertilizerReport,
                                onGenerateWaterAdvice = aiAdvisorViewModel::generateWaterAdvice,
                                onSaveWaterReport = aiAdvisorViewModel::saveWaterReport,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.VOICE_CHAT) {
                            VoiceChatScreen(
                                messages = chatMessages,
                                selectedLanguage = selectedLanguage,
                                autoSpeak = autoSpeak,
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                speakingMessageId = speakingMessageId,
                                partialTranscript = partialTranscript,
                                voiceError = voiceError,
                                isGenerating = isGeneratingChat,
                                farmContext = farmContextString,
                                onSelectLanguage = voiceChatViewModel::setLanguage,
                                onToggleAutoSpeak = voiceChatViewModel::toggleAutoSpeak,
                                onStartVoiceInput = { voiceChatViewModel.startVoiceInput(farmContextString) },
                                onStopVoiceInput = voiceChatViewModel::stopVoiceInput,
                                onSendMessage = { q -> voiceChatViewModel.sendMessage(q, farmContextString) },
                                onSpeakMessage = voiceChatViewModel::speakMessage,
                                onClearVoiceError = voiceChatViewModel::clearVoiceError,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.SENSOR) {
                            SensorScreen(
                                isScanning = isScanningBle,
                                connectionState = bleConnectionState,
                                discoveredDevices = discoveredDevices,
                                currentReading = currentSensorReading,
                                statusMessage = sensorStatusMessage,
                                isSimulationMode = isSimulationMode,
                                onStartScan = sensorViewModel::startScan,
                                onStopScan = sensorViewModel::stopScan,
                                onConnectDevice = sensorViewModel::connectToDevice,
                                onDisconnect = sensorViewModel::disconnect,
                                onToggleSimulation = sensorViewModel::toggleSimulation,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.PROFILE) {
                            ProfileScreen(
                                farmProfile = farmProfile,
                                reports = cropReports,
                                onSaveProfile = farmViewModel::saveProfile,
                                onDeleteReport = farmViewModel::deleteReport,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
