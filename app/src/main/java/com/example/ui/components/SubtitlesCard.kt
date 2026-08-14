package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.PinkTertiary
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AssistantState
import com.example.viewmodel.MahiMood

@Composable
fun SubtitlesCard(
    assistantState: AssistantState,
    mood: MahiMood,
    liveTranscript: String,
    mahiResponse: String,
    onInterrupt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // State and Mood Badge Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x99FFFFFF))
                .border(1.dp, Color(0x33F472B6), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("mahi_status_badge")
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (assistantState) {
                            AssistantState.SPEAKING -> Color(mood.accentColorHex)
                            AssistantState.LISTENING -> PinkPrimary
                            AssistantState.THINKING -> PurpleGlow
                            AssistantState.CONNECTING -> PinkSecondary
                            AssistantState.DISCONNECTED -> Color(0xFF94A3B8)
                        }
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = when (assistantState) {
                    AssistantState.SPEAKING -> "Myra speaking • ${mood.emoji} ${mood.title}"
                    AssistantState.LISTENING -> "Listening to you... 🎤"
                    AssistantState.THINKING -> "Thinking of something witty... ✨"
                    AssistantState.CONNECTING -> "Connecting... 💖"
                    AssistantState.DISCONNECTED -> "Myra is ready • Tap to chat"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            if (assistantState == AssistantState.SPEAKING) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFCE7F3))
                        .clickable(onClick = onInterrupt)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .testTag("interrupt_speech_button")
                ) {
                    Text(
                        text = "Stop ⏹",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vibrant Palette Display Card with Italic Title & Response
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0x1AEC4899),
                    spotColor = Color(0x33DB2777)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF5FFFFFF),
                            Color(0xFAFFF0F6)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x33F472B6),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
                .testTag("mahi_subtitles_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Title from Vibrant Palette Design
                Text(
                    text = "Myra",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = PinkPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // User voice speech transcript (if active)
                AnimatedVisibility(
                    visible = liveTranscript.isNotBlank() && assistantState != AssistantState.DISCONNECTED,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = PinkTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "You:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinkPrimary
                            )
                        }

                        Text(
                            text = "\"$liveTranscript\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .height(1.dp)
                                .background(Color(0x33F472B6))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // Mahi's Sassy Response Text
                Text(
                    text = if (mahiResponse.startsWith("\"") && mahiResponse.endsWith("\"")) {
                        mahiResponse
                    } else {
                        "\"$mahiResponse\""
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (assistantState == AssistantState.SPEAKING) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.testTag("mahi_spoken_text")
                )
            }
        }
    }
}

