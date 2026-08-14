package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun FloatingAmbientParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    val particles = remember {
        val colors = listOf(
            Color(0x40FF85A1),
            Color(0x33FF4081),
            Color(0x2EFFFFFF),
            Color(0x38D946EF),
            Color(0x30FFB6C1)
        )
        List(particleCount) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 28f + 8f,
                speed = Random.nextFloat() * 0.6f + 0.4f,
                alpha = Random.nextFloat() * 0.4f + 0.2f,
                color = colors.random()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val currentY = ((p.initialY - (progress.value * p.speed)) % 1f + 1f) % 1f * height
            val currentX = (p.initialX * width) + (Math.sin((progress.value * 2 * Math.PI + p.initialY * 5)).toFloat() * 20f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        p.color.copy(alpha = p.alpha),
                        p.color.copy(alpha = 0f)
                    ),
                    center = Offset(currentX, currentY),
                    radius = p.radius * 2f
                ),
                radius = p.radius * 2f,
                center = Offset(currentX, currentY)
            )
        }
    }
}
