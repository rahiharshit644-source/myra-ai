package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PinkSecondary
import com.example.ui.theme.PinkTertiary
import com.example.ui.theme.PurpleGlow
import com.example.viewmodel.AssistantState
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    audioEnergy: Float,
    assistantState: AssistantState,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveformPhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val effectiveEnergy = when (assistantState) {
            AssistantState.SPEAKING -> (audioEnergy * 0.9f + 0.35f).coerceIn(0.2f, 1f)
            AssistantState.LISTENING -> (audioEnergy * 1.2f + 0.2f).coerceIn(0.15f, 1f)
            AssistantState.THINKING -> 0.4f
            AssistantState.CONNECTING -> 0.25f
            AssistantState.DISCONNECTED -> 0.08f
        }

        // Draw 3 layered harmonic sine waves with Vibrant Palette gradients
        val waveConfigs = listOf(
            Triple(1.0f, 1.0f, PinkPrimary.copy(alpha = 0.85f)),
            Triple(1.6f, 0.65f, PinkSecondary.copy(alpha = 0.65f)),
            Triple(0.7f, 0.45f, PurpleGlow.copy(alpha = 0.5f))
        )

        waveConfigs.forEach { (freqMultiplier, ampMultiplier, waveColor) ->
            val path = Path()
            val points = 80
            val step = width / points

            for (i in 0..points) {
                val x = i * step
                val normalizedX = (x / width) * 2 * Math.PI.toFloat()
                val waveAmplitude = (height * 0.36f) * effectiveEnergy * ampMultiplier

                // Dynamic multi-sine wave function
                val y = centerY + sin(normalizedX * 2.4f * freqMultiplier + phase.value * freqMultiplier) * waveAmplitude *
                        sin(normalizedX * 0.5f) // envelope tapering at edges

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = waveColor,
                style = Stroke(
                    width = 4.5f * ampMultiplier,
                    cap = StrokeCap.Round
                )
            )
        }

        // Draw glowing audio energy bars in the center
        val barCount = 19
        val barSpacing = width * 0.5f / barCount
        val startX = (width - (barCount * barSpacing)) / 2f

        for (i in 0 until barCount) {
            val barX = startX + (i * barSpacing)
            val distFromCenter = Math.abs(i - (barCount / 2)) / (barCount / 2f)
            val barEnvelope = (1f - distFromCenter * 0.65f)

            val barHeight = (height * 0.38f) * effectiveEnergy * barEnvelope *
                    (0.45f + 0.55f * sin(phase.value * 2.2f + i * 0.55f))

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PinkPrimary.copy(alpha = 0.15f),
                        PinkSecondary,
                        Color(0xFFFFF0F6)
                    ),
                    startY = centerY - barHeight / 2f,
                    endY = centerY + barHeight / 2f
                ),
                start = Offset(barX, centerY - barHeight / 2f),
                end = Offset(barX, centerY + barHeight / 2f),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

