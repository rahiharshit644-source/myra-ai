package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class SpeechSynthesizer(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingEnergy = MutableStateFlow(0f)
    val speakingEnergy: StateFlow<Float> = _speakingEnergy.asStateFlow()

    private var waveformSimulationJob: Job? = null

    var onSpeechStart: (() -> Unit)? = null
    var onSpeechDone: (() -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configureVoice()
            } else {
                Log.e("SpeechSynthesizer", "TTS Initialization failed with status $status")
            }
        }
    }

    private fun configureVoice() {
        tts?.let { engine ->
            val langResult = engine.setLanguage(Locale.US)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("SpeechSynthesizer", "US English not supported, falling back to default locale")
                engine.language = Locale.getDefault()
            }

            // Attempt to select a natural female voice
            try {
                val voices = engine.voices
                if (voices != null) {
                    val femaleVoice = voices.firstOrNull { voice ->
                        !voice.isNetworkConnectionRequired &&
                                (voice.name.contains("female", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-sfg", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-tpf", ignoreCase = true) ||
                                        voice.name.contains("en-us-x-iom", ignoreCase = true))
                    } ?: voices.firstOrNull { it.locale.language == Locale.ENGLISH.language }

                    if (femaleVoice != null) {
                        engine.voice = femaleVoice
                    }
                }
            } catch (e: Exception) {
                Log.w("SpeechSynthesizer", "Could not query available voices", e)
            }

            // Myra's signature sound: bright, youthful, confident pitch & lively pace
            engine.setPitch(1.18f)
            engine.setSpeechRate(1.05f)

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startWaveformSimulation()
                    onSpeechStart?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopWaveformSimulation()
                    onSpeechDone?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopWaveformSimulation()
                    onSpeechError?.invoke("TTS playback error")
                }
            })
        }
    }

    fun speak(text: String, pitch: Float = 1.18f, speechRate: Float = 1.05f) {
        if (!isInitialized || tts == null) {
            // Fallback simulation if TTS is not ready
            simulateSpeakingPlayback(text)
            return
        }

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myra_${System.currentTimeMillis()}")
        }

        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "myra_utterance")
        if (result == TextToSpeech.ERROR) {
            simulateSpeakingPlayback(text)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("SpeechSynthesizer", "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
            stopWaveformSimulation()
        }
    }

    private fun startWaveformSimulation() {
        waveformSimulationJob?.cancel()
        waveformSimulationJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive && _isSpeaking.value) {
                phase += 0.25f
                val base = (Math.sin(phase.toDouble()).toFloat() + 1f) * 0.4f
                val noise = Random.nextFloat() * 0.3f
                _speakingEnergy.value = (base + noise).coerceIn(0.15f, 1f)
                delay(50)
            }
            _speakingEnergy.value = 0f
        }
    }

    private fun stopWaveformSimulation() {
        waveformSimulationJob?.cancel()
        waveformSimulationJob = null
        _speakingEnergy.value = 0f
    }

    private fun simulateSpeakingPlayback(text: String) {
        scope.launch {
            _isSpeaking.value = true
            startWaveformSimulation()
            onSpeechStart?.invoke()

            // Estimate duration based on word count (~220 words per minute)
            val wordCount = text.split(" ").size.coerceAtLeast(1)
            val durationMs = ((wordCount / 3.5f) * 1000).toLong().coerceIn(1200L, 8000L)
            delay(durationMs)

            _isSpeaking.value = false
            stopWaveformSimulation()
            onSpeechDone?.invoke()
        }
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e("SpeechSynthesizer", "Error shutting down TTS", e)
        }
    }
}
