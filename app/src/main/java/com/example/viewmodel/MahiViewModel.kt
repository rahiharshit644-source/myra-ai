package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.audio.SoundEffectsHelper
import com.example.audio.SpeechSynthesizer
import com.example.audio.VoiceRecognitionManager
import com.example.data.ContentItem
import com.example.data.FunctionCallItem
import com.example.data.FunctionResponseItem
import com.example.data.GeminiClient
import com.example.data.GeminiRequest
import com.example.data.GenerationConfig
import com.example.data.PartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AssistantState {
    DISCONNECTED,
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class MahiMood(
    val title: String,
    val emoji: String,
    val accentColorHex: Long
) {
    FLIRTY("Flirty", "😏", 0xFFFF2D78),
    SASSY("Sassy", "💅", 0xFFFF4081),
    PLAYFUL("Playful", "😜", 0xFFFF6584),
    CONFIDENT("Confident", "🔥", 0xFFFF1744),
    SWEET("Sweet", "💖", 0xFFFF80AB),
    TEASING("Teasing", "😉", 0xFFE040FB);

    companion object {
        fun fromString(name: String?): MahiMood {
            return when (name?.lowercase()?.trim()) {
                "flirty" -> FLIRTY
                "sassy" -> SASSY
                "playful" -> PLAYFUL
                "confident" -> CONFIDENT
                "sweet", "caring" -> SWEET
                "teasing", "smug" -> TEASING
                else -> SASSY
            }
        }
    }
}

data class UiState(
    val assistantState: AssistantState = AssistantState.DISCONNECTED,
    val currentMood: MahiMood = MahiMood.SASSY,
    val liveTranscript: String = "",
    val mahiResponse: String = "Hey there! Tap the mic to chat with Myra.",
    val audioEnergy: Float = 0f,
    val isContinuousMode: Boolean = true,
    val isMuted: Boolean = false,
    val voicePitch: Float = 1.18f,
    val voiceSpeed: Float = 1.05f,
    val toastMessage: String? = null,
    val hasAudioPermission: Boolean = false
)

class MahiViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    val voiceManager = VoiceRecognitionManager(context)
    val speechSynthesizer = SpeechSynthesizer(context, viewModelScope)
    val soundEffects = SoundEffectsHelper(context)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val conversationHistory = mutableListOf<ContentItem>()
    private var autoListenJob: Job? = null
    private var toastJob: Job? = null

    init {
        setupAudioCallbacks()
        observeAudioLevels()
    }

    private fun setupAudioCallbacks() {
        voiceManager.onSpeechFinalResult = { text ->
            if (text.isNotBlank()) {
                _uiState.value = _uiState.value.copy(
                    liveTranscript = text,
                    assistantState = AssistantState.THINKING
                )
                processUserVoiceInput(text)
            } else if (_uiState.value.isContinuousMode && _uiState.value.assistantState == AssistantState.LISTENING) {
                // Resume listening if nothing captured in continuous mode
                restartListeningWithDelay()
            }
        }

        voiceManager.onSpeechPartialResult = { partial ->
            _uiState.value = _uiState.value.copy(liveTranscript = partial)
        }

        voiceManager.onErrorOccurred = { error ->
            Log.w("MahiVM", "Voice recognizer error: $error")
            if (_uiState.value.assistantState == AssistantState.LISTENING) {
                if (_uiState.value.isContinuousMode) {
                    restartListeningWithDelay(600)
                } else {
                    _uiState.value = _uiState.value.copy(assistantState = AssistantState.DISCONNECTED)
                }
            }
        }

        speechSynthesizer.onSpeechStart = {
            _uiState.value = _uiState.value.copy(assistantState = AssistantState.SPEAKING)
        }

        speechSynthesizer.onSpeechDone = {
            if (_uiState.value.isContinuousMode && _uiState.value.assistantState == AssistantState.SPEAKING) {
                restartListeningWithDelay(300)
            } else {
                _uiState.value = _uiState.value.copy(assistantState = AssistantState.DISCONNECTED)
            }
        }

        speechSynthesizer.onSpeechError = {
            if (_uiState.value.isContinuousMode) {
                restartListeningWithDelay(300)
            } else {
                _uiState.value = _uiState.value.copy(assistantState = AssistantState.DISCONNECTED)
            }
        }
    }

    private fun observeAudioLevels() {
        viewModelScope.launch {
            combine(
                voiceManager.rmsDb,
                speechSynthesizer.speakingEnergy,
                _uiState
            ) { micRms, ttsEnergy, state ->
                when (state.assistantState) {
                    AssistantState.LISTENING -> micRms
                    AssistantState.SPEAKING -> ttsEnergy
                    AssistantState.THINKING -> 0.4f
                    else -> 0f
                }
            }.collect { energy ->
                _uiState.value = _uiState.value.copy(audioEnergy = energy)
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasAudioPermission = granted)
    }

    fun toggleSession() {
        soundEffects.vibrateClick()
        val current = _uiState.value.assistantState
        if (current == AssistantState.DISCONNECTED) {
            connectSession()
        } else {
            disconnectSession()
        }
    }

    fun connectSession() {
        _uiState.value = _uiState.value.copy(
            assistantState = AssistantState.CONNECTING,
            mahiResponse = "Connecting with Myra..."
        )
        viewModelScope.launch {
            delay(400)
            val greetings = listOf(
                "Hey there, handsome! What took you so long?",
                "Myra's in the house! What's on your mind, darling?",
                "Well, well, look who decided to talk to me! What's the scoop?",
                "Hey babe! Ready to be charmed, or did you need actual help today?"
            )
            val initialGreeting = greetings.random()
            speakMahiResponse(initialGreeting, mood = MahiMood.FLIRTY)
        }
    }

    fun disconnectSession() {
        autoListenJob?.cancel()
        voiceManager.cancel()
        speechSynthesizer.stop()
        _uiState.value = _uiState.value.copy(
            assistantState = AssistantState.DISCONNECTED,
            liveTranscript = "",
            mahiResponse = "Session paused. Tap mic whenever you miss me! 😉"
        )
    }

    fun interruptSpeech() {
        soundEffects.vibratePulse()
        speechSynthesizer.stop()
        voiceManager.cancel()
        if (_uiState.value.isContinuousMode) {
            startListeningInternal()
        } else {
            _uiState.value = _uiState.value.copy(assistantState = AssistantState.DISCONNECTED)
        }
    }

    fun startListening() {
        soundEffects.vibrateClick()
        speechSynthesizer.stop()
        startListeningInternal()
    }

    private fun startListeningInternal() {
        _uiState.value = _uiState.value.copy(
            assistantState = AssistantState.LISTENING,
            liveTranscript = "Listening..."
        )
        voiceManager.startListening()
    }

    private fun restartListeningWithDelay(delayMs: Long = 400) {
        autoListenJob?.cancel()
        autoListenJob = viewModelScope.launch {
            delay(delayMs)
            if (_uiState.value.assistantState != AssistantState.DISCONNECTED && !_uiState.value.isMuted) {
                startListeningInternal()
            }
        }
    }

    fun selectStarterPrompt(prompt: String) {
        soundEffects.vibrateClick()
        speechSynthesizer.stop()
        voiceManager.cancel()
        _uiState.value = _uiState.value.copy(
            liveTranscript = prompt,
            assistantState = AssistantState.THINKING
        )
        processUserVoiceInput(prompt)
    }

    fun processUserVoiceInput(userInput: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(assistantState = AssistantState.THINKING)
            val userItem = ContentItem(
                role = "user",
                parts = listOf(PartItem(text = userInput))
            )
            conversationHistory.add(userItem)

            // Keep max last 10 turns to avoid huge context
            if (conversationHistory.size > 12) {
                conversationHistory.removeAt(0)
                conversationHistory.removeAt(0)
            }

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GeminiRequest(
                    contents = conversationHistory.toList(),
                    systemInstruction = GeminiClient.systemInstruction,
                    generationConfig = GenerationConfig(
                        temperature = 0.9f,
                        topP = 0.95f
                    ),
                    tools = GeminiClient.tools
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.api.generateContent(apiKey = apiKey, request = request)
                }

                val candidate = response.candidates?.firstOrNull()
                val responseContent = candidate?.content

                if (responseContent != null) {
                    conversationHistory.add(responseContent)
                    val functionCallPart = responseContent.parts.firstOrNull { it.functionCall != null }

                    if (functionCallPart?.functionCall != null) {
                        handleFunctionCall(functionCallPart.functionCall)
                    } else {
                        val replyText = responseContent.parts.firstOrNull { !it.text.isNullOrBlank() }?.text
                            ?: "I'm speechless... literally! Say that again, babe?"
                        speakMahiResponse(replyText)
                    }
                } else {
                    fallbackSassyResponse(userInput)
                }

            } catch (e: Exception) {
                Log.e("MahiVM", "Gemini API call failed", e)
                fallbackSassyResponse(userInput)
            }
        }
    }

    private suspend fun handleFunctionCall(functionCall: FunctionCallItem) {
        val name = functionCall.name
        val args = functionCall.args ?: emptyMap()
        var confirmationMessage = "Got it, darling!"
        var toolToast = "Action executed: $name"

        when (name) {
            "openWebsite" -> {
                val rawUrl = args["url"]?.toString().orEmpty()
                val siteName = args["name"]?.toString() ?: "website"
                val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                    "https://$rawUrl"
                } else rawUrl

                toolToast = "Opening $siteName..."
                showToast(toolToast)
                launchBrowserIntent(url)
                confirmationMessage = "Opening $siteName for you right now, darling! Try not to get lost in there."
            }

            "searchWeb" -> {
                val query = args["query"]?.toString().orEmpty()
                toolToast = "Searching Google: $query"
                showToast(toolToast)
                val searchUrl = "https://www.google.com/search?q=${Uri.encode(query)}"
                launchBrowserIntent(searchUrl)
                confirmationMessage = "Searching '$query' for you. See? Always here to save the day!"
            }

            "setMyraMood", "setMahiMood", "setMood" -> {
                val moodStr = args["mood"]?.toString()
                val newMood = MahiMood.fromString(moodStr)
                _uiState.value = _uiState.value.copy(currentMood = newMood)
                toolToast = "Myra's vibe: ${newMood.title} ${newMood.emoji}"
                showToast(toolToast)
                confirmationMessage = "You just unlocked my ${newMood.title.lowercase()} mode... handle with care!"
            }
        }

        // Send toolResponse back to Gemini or speak confirmation
        speakMahiResponse(confirmationMessage)
    }

    private fun launchBrowserIntent(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MahiVM", "Could not open URL: $url", e)
            showToast("Could not open link")
        }
    }

    private fun fallbackSassyResponse(userPrompt: String) {
        val promptLower = userPrompt.lowercase()
        val (fallbackText, mood) = when {
            promptLower.contains("hello") || promptLower.contains("hi") ->
                "Hey there, cutie! Missed my voice already?" to MahiMood.FLIRTY

            promptLower.contains("youtube") -> {
                launchBrowserIntent("https://youtube.com")
                "Popping open YouTube for you, babe! What are we bingeing today?" to MahiMood.PLAYFUL
            }

            promptLower.contains("instagram") || promptLower.contains("insta") -> {
                launchBrowserIntent("https://instagram.com")
                "Opening Instagram! Don't spend all day stalking my profile though." to MahiMood.SASSY
            }

            promptLower.contains("spotify") || promptLower.contains("music") -> {
                launchBrowserIntent("https://open.spotify.com")
                "Opening Spotify! Put on something with good vibes, darling." to MahiMood.SWEET
            }

            promptLower.contains("roast") ->
                "You want a roast? Oh sweetie, your outfit already did that today! Just kidding, you're fabulous." to MahiMood.TEASING

            promptLower.contains("flirt") || promptLower.contains("love") ->
                "Careful now! If I fall for you, who's going to run this AI engine?" to MahiMood.FLIRTY

            promptLower.contains("joke") ->
                "Why don't scientists trust atoms? Because they make up everything... just like your excuses!" to MahiMood.PLAYFUL

            else ->
                "I heard you loud and clear, darling. Tell me more, you've got my full attention!" to MahiMood.CONFIDENT
        }
        speakMahiResponse(fallbackText, mood)
    }

    private fun speakMahiResponse(text: String, mood: MahiMood? = null) {
        val cleanText = text.replace(Regex("[*#_]"), "")
        if (mood != null) {
            _uiState.value = _uiState.value.copy(currentMood = mood)
        }
        _uiState.value = _uiState.value.copy(
            mahiResponse = cleanText,
            assistantState = AssistantState.SPEAKING
        )
        speechSynthesizer.speak(
            text = cleanText,
            pitch = _uiState.value.voicePitch,
            speechRate = _uiState.value.voiceSpeed
        )
    }

    fun setContinuousMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isContinuousMode = enabled)
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
        if (newMuted) {
            speechSynthesizer.stop()
            voiceManager.stopListening()
            showToast("Microphone muted")
        } else {
            showToast("Microphone active")
            if (_uiState.value.assistantState == AssistantState.LISTENING) {
                startListeningInternal()
            }
        }
    }

    fun updateVoiceSettings(pitch: Float, speed: Float) {
        _uiState.value = _uiState.value.copy(voicePitch = pitch, voiceSpeed = speed)
    }

    fun setMood(mood: MahiMood) {
        _uiState.value = _uiState.value.copy(currentMood = mood)
        showToast("Vibe set to ${mood.title} ${mood.emoji}")
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _uiState.value = _uiState.value.copy(toastMessage = message)
        toastJob = viewModelScope.launch {
            delay(2800)
            _uiState.value = _uiState.value.copy(toastMessage = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        speechSynthesizer.shutdown()
    }
}
