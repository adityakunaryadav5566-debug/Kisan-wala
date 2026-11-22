package com.example.platform

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    tts?.setSpeechRate(0.95f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _speakingMessageId.value = null
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _speakingMessageId.value = null
                        }
                    })
                } else {
                    Log.w("VoiceHelper", "TextToSpeech init status=$status")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceHelper", "Failed to initialize TTS", e)
        }
    }

    fun speak(text: String, language: AppLanguage, messageId: String) {
        val engine = tts
        if (!isTtsReady || engine == null) {
            _voiceError.value = "आवाज़ इंजन (TTS) अभी तैयार नहीं है — कृपया डिवाइस की TTS सेटिंग जांचें।"
            return
        }
        val cleanText = text
            .replace("[सैंपल AI उत्तर]", "")
            .replace("[सैंपल AI जवाब]", "")
            .replace("[Sample AI Response]", "")
            .replace("•", "")
            .trim()
        if (cleanText.isBlank()) return

        val desiredLocale = when (language) {
            AppLanguage.HINDI, AppLanguage.BHOJPURI -> Locale("hi", "IN")
            AppLanguage.ENGLISH -> Locale("en", "IN")
        }
        val langResult = engine.setLanguage(desiredLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }
        _speakingMessageId.value = messageId
        _isSpeaking.value = true
        engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    fun startListening(
        language: AppLanguage,
        onFinalResult: (String) -> Unit
    ) {
        _voiceError.value = null
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceError.value = "इस डिवाइस/एमुलेटर पर वॉइस पहचान सेवा उपलब्ध नहीं है। कृपया नीचे टाइप करके पूछें।"
            return
        }

        try {
            stopSpeaking()
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _partialTranscript.value = ""
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _partialTranscript.value = ""
                        _voiceError.value = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ साफ सुनाई नहीं दी — कृपया फिर से बोलें।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कोई आवाज़ नहीं मिली — माइक दबाकर बोलें।"
                            SpeechRecognizer.ERROR_AUDIO -> "माइक्रोफ़ोन ऑडियो त्रुटि हुई।"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "ऑफलाइन वॉइस डेटा उपलब्ध नहीं है — नेटवर्क जांचें या टाइप करें।"
                            else -> "वॉइस पहचान शुरू नहीं हो सकी (Code $error) — कृपया टाइप करके पूछें।"
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull().orEmpty().trim()
                        _partialTranscript.value = ""
                        if (spoken.isNotBlank()) {
                            onFinalResult(spoken)
                        } else {
                            _voiceError.value = "कृपया थोड़ा स्पष्ट बोलें।"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        _partialTranscript.value = partial
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechLocaleTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _voiceError.value = e.localizedMessage ?: "वॉइस इनपुट त्रुटि।"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
        _isListening.value = false
    }

    fun clearError() {
        _voiceError.value = null
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }
}
