package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.FloatingAmbientParticles
import com.example.ui.components.MahiGlassMicOrb
import com.example.ui.components.MahiHeader
import com.example.ui.components.MahiSettingsSheet
import com.example.ui.components.QuickStartersRow
import com.example.ui.components.SubtitlesCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.GradientPinkCanvas
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.PinkTertiary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.AssistantState
import com.example.viewmodel.MahiViewModel

@Composable
fun MahiScreen(
    viewModel: MahiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }

    // Request Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setPermissionGranted(isGranted)
        if (isGranted) {
            viewModel.connectSession()
        } else {
            viewModel.showToast("Microphone permission needed for voice chat")
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionGranted(hasPermission)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("mahi_main_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GradientPinkCanvas)
                .padding(innerPadding)
        ) {
            // Ambient Floating Sparkles & Light Bokeh
            FloatingAmbientParticles()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Live Now indicator, Mute, Settings)
                MahiHeader(
                    mood = uiState.currentMood,
                    isMuted = uiState.isMuted,
                    toastMessage = uiState.toastMessage,
                    onToggleMute = { viewModel.toggleMute() },
                    onOpenSettings = { showSettingsSheet = true }
                )

                // Subtitles & Transcription Card (with italic Mahi AI branding)
                SubtitlesCard(
                    assistantState = uiState.assistantState,
                    mood = uiState.currentMood,
                    liveTranscript = uiState.liveTranscript,
                    mahiResponse = uiState.mahiResponse,
                    onInterrupt = { viewModel.interruptSpeech() }
                )

                Spacer(modifier = Modifier.weight(0.3f))

                // Central Vibrant Glowing Orb with internal dynamic soundwave bars
                MahiGlassMicOrb(
                    assistantState = uiState.assistantState,
                    mood = uiState.currentMood,
                    audioEnergy = uiState.audioEnergy,
                    isMuted = uiState.isMuted,
                    onOrbClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPerm) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (uiState.assistantState == AssistantState.SPEAKING) {
                                viewModel.interruptSpeech()
                            } else {
                                viewModel.toggleSession()
                            }
                        }
                    }
                )

                // Real-time Canvas Waveform Visualizer
                WaveformVisualizer(
                    audioEnergy = uiState.audioEnergy,
                    assistantState = uiState.assistantState,
                    primaryColor = Color(uiState.currentMood.accentColorHex)
                )

                Spacer(modifier = Modifier.weight(0.3f))

                // Mode Indicator and Bottom Action Button from Vibrant Palette Design
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mode line indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(1.dp)
                                .background(Color(0xFFCBD5E1))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (uiState.assistantState) {
                                AssistantState.SPEAKING -> "SPEAKING MODE"
                                AssistantState.LISTENING -> "LISTENING MODE"
                                AssistantState.THINKING -> "THINKING MODE"
                                AssistantState.CONNECTING -> "CONNECTING..."
                                AssistantState.DISCONNECTED -> "READY MODE"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(1.dp)
                                .background(Color(0xFFCBD5E1))
                        )
                    }

                    // Bottom Floating Action Button from HTML Design
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = PinkSecondary.copy(alpha = 0.35f),
                                spotColor = PinkPrimary
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0x33F472B6), CircleShape)
                            .clickable {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPerm) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (uiState.assistantState == AssistantState.SPEAKING) {
                                        viewModel.interruptSpeech()
                                    } else {
                                        viewModel.toggleSession()
                                    }
                                }
                            }
                            .testTag("bottom_action_mic_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFF0F5),
                                            Color(0xFFFCE7F3)
                                        )
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = when (uiState.assistantState) {
                                    AssistantState.SPEAKING -> Icons.Default.Stop
                                    AssistantState.DISCONNECTED -> Icons.Default.MicOff
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "Interrupt or Toggle Mic",
                                tint = PinkPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Action subcaption
                    Text(
                        text = when (uiState.assistantState) {
                            AssistantState.SPEAKING -> "TAP TO INTERRUPT"
                            AssistantState.DISCONNECTED -> "TAP TO CONNECT"
                            else -> "TAP TO TALK"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.4.sp,
                        color = PinkTertiary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                    )
                }

                // Text Prompt Input Bar (Allows typing to Myra directly without speech)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                text = "Ask or say anything to Myra...",
                                fontSize = 13.sp,
                                color = TextTertiary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xF5FFFFFF),
                            unfocusedContainerColor = Color(0xD9FFFFFF),
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = Color(0x33F472B6),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PinkPrimary
                        ),
                        textStyle = TextStyle(fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    val prompt = textInput.trim()
                                    textInput = ""
                                    keyboardController?.hide()
                                    viewModel.selectStarterPrompt(prompt)
                                }
                            }
                        ),
                        trailingIcon = {
                            if (textInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val prompt = textInput.trim()
                                        textInput = ""
                                        keyboardController?.hide()
                                        viewModel.selectStarterPrompt(prompt)
                                    },
                                    modifier = Modifier.testTag("send_text_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send message",
                                        tint = PinkPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("custom_prompt_input")
                    )
                }

                // Quick Starter Prompts Row
                QuickStartersRow(
                    onSelectStarter = { prompt ->
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPerm) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            viewModel.selectStarterPrompt(prompt)
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Settings Modal Bottom Sheet
            if (showSettingsSheet) {
                MahiSettingsSheet(
                    currentMood = uiState.currentMood,
                    isContinuousMode = uiState.isContinuousMode,
                    voicePitch = uiState.voicePitch,
                    voiceSpeed = uiState.voiceSpeed,
                    onMoodSelected = { mood ->
                        viewModel.setMood(mood)
                    },
                    onContinuousModeChanged = { enabled ->
                        viewModel.setContinuousMode(enabled)
                    },
                    onVoiceSettingsChanged = { pitch, speed ->
                        viewModel.updateVoiceSettings(pitch, speed)
                    },
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}

