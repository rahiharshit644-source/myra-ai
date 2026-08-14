package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.PinkTertiary
import com.example.ui.theme.PurpleGlow
import com.example.viewmodel.AssistantState
import com.example.viewmodel.MahiMood
import kotlin.math.sin

@Composable
fun MahiGlassMicOrb(
    assistantState: AssistantState,
    mood: MahiMood,
    audioEnergy: Float,
    isMuted: Boolean,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbAnimations")

    // Pulsing outer halo scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (assistantState != AssistantState.DISCONNECTED) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (assistantState) {
                    AssistantState.LISTENING -> 800
                    AssistantState.SPEAKING -> 1100
                    AssistantState.THINKING -> 600
                    else -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Halo rotation for decorative dashed ring
    val dashedRingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashedRingRotation"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "internalWavePhase"
    )

    // Dynamic accent color based on state & mood
    val accentColor by animateColorAsState(
        targetValue = when {
            isMuted -> Color(0xFF94A3B8)
            assistantState == AssistantState.SPEAKING -> Color(mood.accentColorHex)
            assistantState == AssistantState.LISTENING -> PinkPrimary
            assistantState == AssistantState.THINKING -> PurpleGlow
            assistantState == AssistantState.CONNECTING -> PinkSecondary
            else -> PinkTertiary
        },
        animationSpec = tween(400),
        label = "accentColor"
    )

    val scaleModifier by animateFloatAsState(
        targetValue = when (assistantState) {
            AssistantState.LISTENING -> 1.06f + (audioEnergy * 0.12f)
            AssistantState.SPEAKING -> 1.04f + (audioEnergy * 0.1f)
            AssistantState.THINKING -> 0.98f
            AssistantState.CONNECTING -> 1.02f
            AssistantState.DISCONNECTED -> 1f
        },
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "scaleModifier"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("mahi_mic_orb_container")
    ) {
        // Outer diffuse gradient aura (w-80 h-80 blur-3xl from design)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            if (assistantState != AssistantState.DISCONNECTED) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PinkTertiary.copy(alpha = 0.45f),
                            PurpleGlow.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxRadius
                    ),
                    radius = maxRadius,
                    center = center
                )
            }
        }

        // Dashed outer ring from Vibrant Palette (border-2 border-dashed border-pink-300)
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .rotate(dashedRingRotation)
        ) {
            drawCircle(
                color = PinkTertiary.copy(alpha = 0.45f),
                radius = size.minDimension / 2f - 2.dp.toPx(),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f), 0f)
                )
            )
        }

        // Main Vibrant Palette Orb: w-40 h-40 bg-gradient-to-br from-pink-400 to-fuchsia-500
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(165.dp)
                .scale(scaleModifier)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    ambientColor = PinkSecondary.copy(alpha = 0.6f),
                    spotColor = PinkSecondary
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PinkTertiary, // pink-400
                            PinkSecondary,// pink-500
                            PurpleGlow    // fuchsia-500
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(220f, 220f)
                    )
                )
                .padding(4.dp)
        ) {
            // Inner Core with border-4 border-white/20 and background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PinkSecondary,
                                PinkPrimary
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = onOrbClick
                    )
                    .testTag("mahi_central_glass_button")
            ) {
                // Soundwave bars inside the orb (as shown in HTML design: 5 equalizer bars)
                val baseEnergy = when (assistantState) {
                    AssistantState.SPEAKING -> (audioEnergy * 0.9f + 0.4f).coerceIn(0.25f, 1f)
                    AssistantState.LISTENING -> (audioEnergy * 1.1f + 0.2f).coerceIn(0.15f, 1f)
                    AssistantState.THINKING -> 0.35f
                    AssistantState.CONNECTING -> 0.2f
                    AssistantState.DISCONNECTED -> 0.08f
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(24.dp, 44.dp, 32.dp, 52.dp, 20.dp)
                    val opacities = listOf(0.9f, 1f, 0.85f, 1f, 0.75f)

                    heights.forEachIndexed { i, targetH ->
                        val dynamicMultiplier = 0.4f + 0.6f * (0.5f + 0.5f * sin(wavePhase + i * 0.8f))
                        val currentHeight = if (assistantState != AssistantState.DISCONNECTED) {
                            (targetH.value * (0.3f + baseEnergy * 0.7f * dynamicMultiplier)).coerceIn(8f, 56f).dp
                        } else {
                            8.dp
                        }

                        Box(
                            modifier = Modifier
                                .width(5.5.dp)
                                .height(currentHeight)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = opacities[i]))
                        )
                    }
                }
            }
        }
    }
}

