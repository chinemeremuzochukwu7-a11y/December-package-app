package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CardDecorationStyle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CardHolidayVisualBackground(
    decorationStyle: CardDecorationStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (decorationStyle) {
            CardDecorationStyle.CHRISTMAS_TREE -> drawChristmasTreeDecoration(accentColor)
            CardDecorationStyle.SNOWFLAKES -> drawSnowflakesDecoration(accentColor)
            CardDecorationStyle.STARS -> drawStarsDecoration(accentColor)
            CardDecorationStyle.BELLS -> drawBellsDecoration(accentColor)
            CardDecorationStyle.ORNAMENTS -> drawOrnamentsDecoration(accentColor)
            CardDecorationStyle.GIFT_BOXES -> drawGiftBoxesDecoration(accentColor)
            CardDecorationStyle.SANTA_MAGIC -> drawSantaMagicDecoration(accentColor)
            CardDecorationStyle.FIREWORKS -> drawFireworksDecoration(accentColor)
            CardDecorationStyle.CONFETTI -> drawConfettiDecoration(accentColor)
            CardDecorationStyle.YEAR_2027 -> drawYear2027Decoration(accentColor)
        }
    }
}

// 1. Christmas Tree visual elements
private fun DrawScope.drawChristmasTreeDecoration(accentColor: Color) {
    val alpha = 0.18f
    val color = accentColor.copy(alpha = alpha)

    // Draw stylized Christmas tree silhouette in background
    val treePath = Path().apply {
        val centerX = size.width / 2f
        val topY = size.height * 0.12f
        val baseY = size.height * 0.88f

        // Top tier
        moveTo(centerX, topY)
        lineTo(centerX + size.width * 0.22f, topY + size.height * 0.22f)
        lineTo(centerX + size.width * 0.14f, topY + size.height * 0.22f)

        // Mid tier
        lineTo(centerX + size.width * 0.30f, topY + size.height * 0.44f)
        lineTo(centerX + size.width * 0.20f, topY + size.height * 0.44f)

        // Bottom tier
        lineTo(centerX + size.width * 0.38f, baseY - size.height * 0.12f)
        lineTo(centerX + size.width * 0.08f, baseY - size.height * 0.12f)

        // Trunk right
        lineTo(centerX + size.width * 0.08f, baseY)
        // Trunk left
        lineTo(centerX - size.width * 0.08f, baseY)
        lineTo(centerX - size.width * 0.08f, baseY - size.height * 0.12f)

        // Left bottom tier
        lineTo(centerX - size.width * 0.38f, baseY - size.height * 0.12f)
        lineTo(centerX - size.width * 0.20f, topY + size.height * 0.44f)

        // Left mid tier
        lineTo(centerX - size.width * 0.30f, topY + size.height * 0.44f)
        lineTo(centerX - size.width * 0.14f, topY + size.height * 0.22f)

        // Left top tier
        lineTo(centerX - size.width * 0.22f, topY + size.height * 0.22f)
        close()
    }
    drawPath(treePath, color = color, style = Stroke(width = 2.5f))

    // Little ornaments on tree
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 6f, center = Offset(size.width * 0.35f, size.height * 0.38f))
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 7f, center = Offset(size.width * 0.65f, size.height * 0.42f))
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 5f, center = Offset(size.width * 0.40f, size.height * 0.60f))
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 8f, center = Offset(size.width * 0.60f, size.height * 0.65f))
}

// 2. Snowflakes
private fun DrawScope.drawSnowflakesDecoration(accentColor: Color) {
    val flakeOffsets = listOf(
        Offset(size.width * 0.15f, size.height * 0.18f),
        Offset(size.width * 0.85f, size.height * 0.16f),
        Offset(size.width * 0.20f, size.height * 0.50f),
        Offset(size.width * 0.82f, size.height * 0.52f),
        Offset(size.width * 0.16f, size.height * 0.82f),
        Offset(size.width * 0.84f, size.height * 0.84f),
        Offset(size.width * 0.50f, size.height * 0.10f),
        Offset(size.width * 0.50f, size.height * 0.90f)
    )

    flakeOffsets.forEachIndexed { i, center ->
        val radius = if (i % 2 == 0) 18f else 12f
        val color = accentColor.copy(alpha = if (i % 2 == 0) 0.35f else 0.25f)
        // 6 rays
        for (angle in 0 until 360 step 60) {
            val rad = Math.toRadians(angle.toDouble())
            val end = Offset(
                center.x + (radius * cos(rad)).toFloat(),
                center.y + (radius * sin(rad)).toFloat()
            )
            drawLine(color, center, end, strokeWidth = 2f)
        }
        drawCircle(color, radius = 2.5f, center = center)
    }
}

// 3. Stars
private fun DrawScope.drawStarsDecoration(accentColor: Color) {
    val starCenters = listOf(
        Offset(size.width * 0.15f, size.height * 0.15f),
        Offset(size.width * 0.85f, size.height * 0.14f),
        Offset(size.width * 0.12f, size.height * 0.78f),
        Offset(size.width * 0.88f, size.height * 0.80f),
        Offset(size.width * 0.50f, size.height * 0.08f),
        Offset(size.width * 0.25f, size.height * 0.45f),
        Offset(size.width * 0.75f, size.height * 0.45f)
    )

    starCenters.forEachIndexed { idx, center ->
        val outerRadius = if (idx == 4) 22f else 14f
        val innerRadius = outerRadius * 0.45f
        val color = accentColor.copy(alpha = 0.32f)

        val starPath = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val x = (center.x + r * cos(angle)).toFloat()
            val y = (center.y + r * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
        drawPath(starPath, color = color, style = Fill)
    }
}

// 4. Bells
private fun DrawScope.drawBellsDecoration(accentColor: Color) {
    val color = accentColor.copy(alpha = 0.25f)
    // Draw two decorative bells in top corners
    val leftBellCenter = Offset(size.width * 0.16f, size.height * 0.14f)
    val rightBellCenter = Offset(size.width * 0.84f, size.height * 0.14f)

    listOf(leftBellCenter, rightBellCenter).forEach { center ->
        val bellPath = Path().apply {
            moveTo(center.x - 14f, center.y + 14f)
            cubicTo(center.x - 14f, center.y, center.x - 8f, center.y - 14f, center.x, center.y - 14f)
            cubicTo(center.x + 8f, center.y - 14f, center.x + 14f, center.y, center.x + 14f, center.y + 14f)
            close()
        }
        drawPath(bellPath, color = color, style = Stroke(width = 2.5f))
        drawCircle(color, radius = 4f, center = Offset(center.x, center.y + 17f))
    }
}

// 5. Ornaments
private fun DrawScope.drawOrnamentsDecoration(accentColor: Color) {
    val color = accentColor.copy(alpha = 0.28f)
    val centers = listOf(
        Offset(size.width * 0.15f, size.height * 0.18f),
        Offset(size.width * 0.85f, size.height * 0.20f),
        Offset(size.width * 0.20f, size.height * 0.82f),
        Offset(size.width * 0.80f, size.height * 0.84f)
    )
    centers.forEach { center ->
        // Bauble hanging line
        drawLine(color, Offset(center.x, 0f), Offset(center.x, center.y - 16f), strokeWidth = 1.5f)
        // Cap
        drawRect(color, topLeft = Offset(center.x - 4f, center.y - 18f), size = Size(8f, 4f))
        // Bauble circle
        drawCircle(color, radius = 16f, center = center, style = Stroke(width = 2f))
        // Inner sparkle
        drawCircle(accentColor.copy(alpha = 0.45f), radius = 4f, center = center)
    }
}

// 6. Gift Boxes
private fun DrawScope.drawGiftBoxesDecoration(accentColor: Color) {
    val color = accentColor.copy(alpha = 0.25f)
    val bottomGiftCenter = Offset(size.width * 0.18f, size.height * 0.84f)
    val bottomGiftRight = Offset(size.width * 0.82f, size.height * 0.84f)

    listOf(bottomGiftCenter, bottomGiftRight).forEach { center ->
        // Box
        drawRect(color, topLeft = Offset(center.x - 18f, center.y - 14f), size = Size(36f, 32f), style = Stroke(width = 2f))
        // Ribbon horizontal & vertical
        drawLine(color, Offset(center.x - 18f, center.y + 2f), Offset(center.x + 18f, center.y + 2f), strokeWidth = 2f)
        drawLine(color, Offset(center.x, center.y - 14f), Offset(center.x, center.y + 18f), strokeWidth = 2f)
        // Bow loops
        drawCircle(color, radius = 5f, center = Offset(center.x - 6f, center.y - 18f), style = Stroke(width = 1.5f))
        drawCircle(color, radius = 5f, center = Offset(center.x + 6f, center.y - 18f), style = Stroke(width = 1.5f))
    }
}

// 7. Santa's Cheer
private fun DrawScope.drawSantaMagicDecoration(accentColor: Color) {
    val color = accentColor.copy(alpha = 0.28f)
    // Stylized Santa hat outline in upper corner
    val hatPath = Path().apply {
        val originX = size.width * 0.84f
        val originY = size.height * 0.16f
        moveTo(originX - 22f, originY + 12f)
        lineTo(originX + 22f, originY + 12f)
        lineTo(originX + 8f, originY - 18f)
        close()
    }
    drawPath(hatPath, color = color, style = Stroke(width = 2f))
    drawCircle(accentColor.copy(alpha = 0.4f), radius = 5f, center = Offset(size.width * 0.84f + 8f, size.height * 0.16f - 20f))

    // Little sparkles below
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 3f, center = Offset(size.width * 0.18f, size.height * 0.22f))
    drawCircle(accentColor.copy(alpha = 0.35f), radius = 4f, center = Offset(size.width * 0.25f, size.height * 0.80f))
}

// 8. Fireworks
private fun DrawScope.drawFireworksDecoration(accentColor: Color) {
    val bursts = listOf(
        Offset(size.width * 0.20f, size.height * 0.16f),
        Offset(size.width * 0.80f, size.height * 0.18f),
        Offset(size.width * 0.16f, size.height * 0.78f),
        Offset(size.width * 0.82f, size.height * 0.82f)
    )

    bursts.forEachIndexed { index, center ->
        val radius = if (index % 2 == 0) 24f else 18f
        val color = accentColor.copy(alpha = 0.35f)
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30).toDouble())
            val inner = Offset(
                (center.x + (radius * 0.35f) * cos(angle)).toFloat(),
                (center.y + (radius * 0.35f) * sin(angle)).toFloat()
            )
            val outer = Offset(
                (center.x + radius * cos(angle)).toFloat(),
                (center.y + radius * sin(angle)).toFloat()
            )
            drawLine(color, inner, outer, strokeWidth = 1.8f)
        }
        drawCircle(accentColor.copy(alpha = 0.6f), radius = 2.5f, center = center)
    }
}

// 9. Festive Confetti
private fun DrawScope.drawConfettiDecoration(accentColor: Color) {
    val confettiCoords = listOf(
        Pair(Offset(size.width * 0.12f, size.height * 0.12f), 15f),
        Pair(Offset(size.width * 0.32f, size.height * 0.18f), -30f),
        Pair(Offset(size.width * 0.70f, size.height * 0.14f), 45f),
        Pair(Offset(size.width * 0.88f, size.height * 0.22f), -20f),
        Pair(Offset(size.width * 0.15f, size.height * 0.48f), 60f),
        Pair(Offset(size.width * 0.85f, size.height * 0.52f), -45f),
        Pair(Offset(size.width * 0.22f, size.height * 0.82f), 30f),
        Pair(Offset(size.width * 0.50f, size.height * 0.88f), 10f),
        Pair(Offset(size.width * 0.80f, size.height * 0.84f), -50f)
    )

    confettiCoords.forEachIndexed { i, pair ->
        val (offset, rotation) = pair
        val color = accentColor.copy(alpha = if (i % 2 == 0) 0.4f else 0.25f)
        // Draw confetti rectangle or dot
        if (i % 3 == 0) {
            drawCircle(color, radius = 3.5f, center = offset)
        } else {
            drawRect(
                color = color,
                topLeft = Offset(offset.x - 6f, offset.y - 3f),
                size = Size(12f, 6f)
            )
        }
    }
}

// 10. 2027 Celebration
private fun DrawScope.drawYear2027Decoration(accentColor: Color) {
    val color = accentColor.copy(alpha = 0.25f)
    // Draw celebration rings and radiant sparkles
    val centerTop = Offset(size.width * 0.50f, size.height * 0.12f)
    drawCircle(color, radius = 28f, center = centerTop, style = Stroke(width = 1.5f))
    drawCircle(color, radius = 18f, center = centerTop, style = Stroke(width = 1f))

    // Radial bursts around celebration
    for (i in 0 until 8) {
        val angle = Math.toRadians((i * 45).toDouble())
        val start = Offset(
            (centerTop.x + 32f * cos(angle)).toFloat(),
            (centerTop.y + 32f * sin(angle)).toFloat()
        )
        val end = Offset(
            (centerTop.x + 40f * cos(angle)).toFloat(),
            (centerTop.y + 40f * sin(angle)).toFloat()
        )
        drawLine(accentColor.copy(alpha = 0.35f), start, end, strokeWidth = 2f)
    }

    // Corner decorative stars
    drawCircle(accentColor.copy(alpha = 0.3f), radius = 5f, center = Offset(size.width * 0.15f, size.height * 0.85f))
    drawCircle(accentColor.copy(alpha = 0.3f), radius = 5f, center = Offset(size.width * 0.85f, size.height * 0.85f))
}
