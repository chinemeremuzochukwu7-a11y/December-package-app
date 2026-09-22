package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class Snowflake(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float,
    val isGoldSparkle: Boolean
)

@Composable
fun SnowfallEffect(
    modifier: Modifier = Modifier,
    snowflakeCount: Int = 45,
    enabled: Boolean = true
) {
    if (!enabled) return

    val snowflakes = remember(snowflakeCount) {
        List(snowflakeCount) {
            Snowflake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4.5f + 2f,
                speed = Random.nextFloat() * 0.0018f + 0.0008f,
                alpha = Random.nextFloat() * 0.65f + 0.25f,
                swayAmplitude = Random.nextFloat() * 0.035f + 0.015f,
                swayFrequency = Random.nextFloat() * 2f + 1f,
                isGoldSparkle = Random.nextFloat() < 0.2f
            )
        }
    }

    var frameTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled) {
        while (enabled) {
            withFrameNanos { nanos ->
                frameTime = (nanos / 1_000_000L).toFloat() / 1000f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0 || height <= 0) return@Canvas

        snowflakes.forEach { flake ->
            // Advance vertical position
            flake.y += flake.speed
            if (flake.y > 1f) {
                flake.y = -0.05f
                flake.x = Random.nextFloat()
            }

            // Sway horizontal position
            val sway = sin((frameTime * flake.swayFrequency) + (flake.x * 10f)) * flake.swayAmplitude
            val currentX = ((flake.x + sway).coerceIn(0f, 1f)) * width
            val currentY = flake.y * height

            val color = if (flake.isGoldSparkle) {
                Color(0xFFFFD700).copy(alpha = (flake.alpha * 0.85f).coerceIn(0f, 1f))
            } else {
                Color.White.copy(alpha = flake.alpha)
            }

            // Draw soft round snowflake or diamond sparkle
            if (flake.isGoldSparkle) {
                // Draw 4-point sparkle diamond
                val sparkleSize = flake.size * 1.4f
                drawLine(
                    color = color,
                    start = Offset(currentX - sparkleSize, currentY),
                    end = Offset(currentX + sparkleSize, currentY),
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = color,
                    start = Offset(currentX, currentY - sparkleSize),
                    end = Offset(currentX, currentY + sparkleSize),
                    strokeWidth = 1.2f
                )
                drawCircle(
                    color = Color.White.copy(alpha = flake.alpha),
                    radius = flake.size * 0.6f,
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
