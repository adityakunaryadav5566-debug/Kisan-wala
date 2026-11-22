package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.platform.VoiceAssistantHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VoiceChatViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository: AiRepository = PlaceholderAiRepository()
    private val voiceHelper = VoiceAssistantHelper(application)

    private val _selectedLanguage = MutableStateFlow(AppLanguage.HINDI)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val isListening: StateFlow<Boolean> = voiceHelper.isListening
    val isSpeaking: StateFlow<Boolean> = voiceHelper.isSpeaking
    val speakingMessageId: StateFlow<String?> = voiceHelper.speakingMessageId
    val partialTranscript: StateFlow<String> = voiceHelper.partialTranscript
    val voiceError: StateFlow<String?> = voiceHelper.voiceError

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome_1",
                isUser = false,
                text = "नमस्ते किसान भाई! मैं किसान प्लांट AI सहायक हूँ। आप हिन्दी, भोजपुरी या English में बोलकर या लिखकर अपनी फसल, खाद, पानी और बीमारी से जुड़े सवाल पूछ सकते हैं।\n\n(नोट: लाइव Gemini AI उत्तर के लिए Firebase आवश्यक है — अभी यह सैंपल प्रीव्यू मोड में है।)",
                language = AppLanguage.HINDI,
                isSampleResponse = true
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var activeStreamJob: Job? = null

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    fun toggleAutoSpeak() {
        _autoSpeak.value = !_autoSpeak.value
        if (!_autoSpeak.value) {
            voiceHelper.stopSpeaking()
        }
    }

    fun startVoiceInput(farmContext: String) {
        voiceHelper.startListening(_selectedLanguage.value) { spokenText ->
            sendMessage(spokenText, farmContext)
        }
    }

    fun stopVoiceInput() {
        voiceHelper.stopListening()
    }

    fun speakMessage(message: ChatMessage) {
        if (speakingMessageId.value == message.id && isSpeaking.value) {
            voiceHelper.stopSpeaking()
        } else {
            voiceHelper.speak(message.text, message.language, message.id)
        }
    }

    fun clearVoiceError() {
        voiceHelper.clearError()
    }

    fun sendMessage(query: String, farmContext: String) {
        val clean = query.trim()
        if (clean.isBlank() || _isGenerating.value) return

        val lang = _selectedLanguage.value
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = clean,
            language = lang
        )
        val aiMsgId = UUID.randomUUID().toString()
        val initialAiMsg = ChatMessage(
            id = aiMsgId,
            isUser = false,
            text = "...",
            language = lang,
            isSampleResponse = !aiRepository.isLiveAiConnected
        )

        _messages.value = _messages.value + userMsg + initialAiMsg
        _isGenerating.value = true

        activeStreamJob?.cancel()
        activeStreamJob = viewModelScope.launch {
            var latestText = ""
            try {
                aiRepository.streamChatResponse(clean, lang, farmContext).collect { partial ->
                    latestText = partial
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == aiMsgId) msg.copy(text = partial) else msg
                    }
                }
                if (_autoSpeak.value && latestText.isNotBlank()) {
                    voiceHelper.speak(latestText, lang, aiMsgId)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == aiMsgId) {
                        msg.copy(text = "उत्तर बनाने में त्रुटि हुई: ${e.localizedMessage ?: "Unknown error"}")
                    } else msg
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.release()
    }
}
