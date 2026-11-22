package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.ui.components.FirebaseRequiredNoticeBanner
import com.example.ui.components.TopScreenHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldElevated
import com.example.ui.theme.EmeraldNight
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenBright
import com.example.ui.theme.FreshGreenSoft
import com.example.ui.theme.HarvestGoldBright
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite

@Composable
fun VoiceChatScreen(
    messages: List<ChatMessage>,
    selectedLanguage: AppLanguage,
    autoSpeak: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    speakingMessageId: String?,
    partialTranscript: String,
    voiceError: String?,
    isGenerating: Boolean,
    farmContext: String,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopVoiceInput: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSpeakMessage: (ChatMessage) -> Unit,
    onClearVoiceError: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var draftText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartVoiceInput()
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val quickPrompts = remember(selectedLanguage) {
        when (selectedLanguage) {
            AppLanguage.HINDI -> listOf(
                "गेहूँ की पत्ती पीली क्यों पड़ रही है?",
                "टमाटर में कब पानी देना चाहिए?",
                "खाद की सही मात्रा कैसे जानें?"
            )
            AppLanguage.BHOJPURI -> listOf(
                "हमार धान के पत्ता पीयर होत बा, का करीं?",
                "सब्जी में पानी कब देबे के चाहीं?",
                "यूरिया बेसी डालला से का नुकसान होला?"
            )
            AppLanguage.ENGLISH -> listOf(
                "Why are my wheat leaves turning yellow?",
                "When should I water tomatoes in summer?",
                "Which soil lab tests are essential?"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
            .imePadding()
    ) {
        TopScreenHeader(
            titleHindi = "AI वॉइस किसान चैट",
            subtitleEnglish = "Hindi • भोजपुरी • English Voice Assistant",
            onBackClick = onBackClick,
            trailingContent = {
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onToggleAutoSpeak)
                        .testTag("auto_speak_toggle"),
                    color = if (autoSpeak) FreshGreen.copy(alpha = 0.22f) else EmeraldCard,
                    border = BorderStroke(1.dp, if (autoSpeak) FreshGreenBright else EmeraldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (autoSpeak) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Auto Speak",
                            tint = if (autoSpeak) FreshGreenBright else MutedSage,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (autoSpeak) "आवाज़ ON" else "म्यूट",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                    }
                }
            }
        )

        // Language Switcher + Sample Notice
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val selected = lang == selectedLanguage
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang.nativeLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = EmeraldCard,
                            labelColor = PureWhite,
                            selectedContainerColor = HarvestGoldBright,
                            selectedLabelColor = EmeraldDeep
                        ),
                        modifier = Modifier.testTag("lang_chip_${lang.code}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            FirebaseRequiredNoticeBanner()
        }

        // Chat Messages List (ChatGPT-style)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    isThisSpeaking = isSpeaking && speakingMessageId == msg.id,
                    onSpeakClick = { onSpeakMessage(msg) }
                )
            }
        }

        // Voice Listening Status / Error Banner
        AnimatedVisibility(visible = isListening || voiceError != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (voiceError != null) AlertRed.copy(alpha = 0.2f) else EmeraldElevated,
                border = BorderStroke(1.dp, if (voiceError != null) AlertRed else FreshGreenBright)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = if (voiceError != null) AlertRed else FreshGreenBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = voiceError ?: if (partialTranscript.isNotBlank()) partialTranscript else "सुन रहा हूँ... बोलिए",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    if (voiceError != null) {
                        Text(
                            text = "ठीक है",
                            style = MaterialTheme.typography.labelLarge,
                            color = HarvestGoldBright,
                            modifier = Modifier
                                .clickable(onClick = onClearVoiceError)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // Starter Prompt Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSendMessage(prompt) },
                    color = EmeraldNight,
                    border = BorderStroke(1.dp, EmeraldBorder)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FreshGreenSoft,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Bottom Composer with Glowing Mic & Send
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = EmeraldNight,
            border = BorderStroke(1.dp, EmeraldBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
                val scale by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.16f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(650),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "mic_scale"
                )

                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .clickable {
                            if (isListening) {
                                onStopVoiceInput()
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    onStartVoiceInput()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("voice_mic_button"),
                    color = if (isListening) AlertRed else FreshGreen,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = stringResource(R.string.cd_mic_button),
                            tint = EmeraldDeep,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.HINDI -> "अपना कृषि सवाल लिखें..."
                                AppLanguage.BHOJPURI -> "रउआ सवाल लिखीं..."
                                AppLanguage.ENGLISH -> "Ask your farming question..."
                            },
                            color = MutedSage
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FreshGreenBright,
                        unfocusedBorderColor = EmeraldBorder,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard
                    ),
                    maxLines = 3
                )

                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(enabled = draftText.isNotBlank() && !isGenerating) {
                            val toSend = draftText
                            draftText = ""
                            onSendMessage(toSend)
                        }
                        .testTag("chat_send_button"),
                    color = if (draftText.isNotBlank() && !isGenerating) HarvestGoldBright else EmeraldElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "सवाल भेजें",
                            tint = if (draftText.isNotBlank() && !isGenerating) EmeraldDeep else MutedSage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    isThisSpeaking: Boolean,
    onSpeakClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 20.dp
            ),
            color = if (message.isUser) FreshGreen else EmeraldCard,
            border = if (message.isUser) null else BorderStroke(1.dp, EmeraldBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.isUser) EmeraldDeep else PureWhite
                )
                if (!message.isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (message.isSampleResponse) "Sample Preview • ${message.language.nativeLabel}" else message.language.nativeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGoldBright
                        )
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("speaker_btn_${message.id}")
                        ) {
                            Icon(
                                imageVector = if (isThisSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = stringResource(R.string.cd_speaker_button),
                                tint = if (isThisSpeaking) HarvestGoldBright else FreshGreenBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
