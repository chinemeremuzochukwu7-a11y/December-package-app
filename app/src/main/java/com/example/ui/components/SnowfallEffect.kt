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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class Snowflake(
    val initialX: Float,
    val initialY: Float,
    val size: Float,
    val speedMultiplier: Float,
    val alpha: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val isGoldSparkle: Boolean
)

@Composable
fun SnowfallEffect(
    modifier: Modifier = Modifier,
    snowflakeCount: Int = 35,
    enabled: Boolean = true
) {
    if (!enabled) return

    val snowflakes = remember(snowflakeCount) {
        List(snowflakeCount) {
            Snowflake(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                size = Random.nextFloat() * 4.5f + 2f,
                speedMultiplier = Random.nextFloat() * 0.8f + 0.6f,
                alpha = Random.nextFloat() * 0.65f + 0.25f,
                swayAmplitude = Random.nextFloat() * 0.03f + 0.01f,
                swayFrequency = Random.nextFloat() * 3f + 1.5f,
                isGoldSparkle = Random.nextFloat() < 0.2f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "snowfall_loop")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        snowflakes.forEach { flake ->
            // Calculate smooth continuous falling position based on progress
            val totalTravel = progress * flake.speedMultiplier
            val currentY = ((flake.initialY + totalTravel) % 1.0f) * height

            // Sway horizontal position
            val sway = sin((progress * 6.28318f * flake.swayFrequency) + (flake.initialX * 10f)) * flake.swayAmplitude
            val currentX = ((flake.initialX + sway).coerceIn(0f, 1f)) * width

            val color = if (flake.isGoldSparkle) {
                Color(0xFFFFD700).copy(alpha = (flake.alpha * 0.85f).coerceIn(0f, 1f))
            } else {
                Color.White.copy(alpha = flake.alpha)
            }

            if (flake.isGoldSparkle) {
                val sparkleSize = flake.size * 1.4f
                drawLine(
                    color = color,
                    start = Offset(currentX - sparkleSize, currentY),
                    end = Offset(currentX + sparkleSize, currentY),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = color,
                    start = Offset(currentX, currentY - sparkleSize),
                    end = Offset(currentX, currentY + sparkleSize),
                    strokeWidth = 1.5f
                )
                drawCircle(
                    color = Color.White.copy(alpha = (flake.alpha * 0.9f).coerceIn(0f, 1f)),
                    radius = flake.size * 0.45f,
                    center = Offset(currentX, currentY)
                )
            } else {
                drawCircle(
                    color = color,
                    radius = flake.size,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
