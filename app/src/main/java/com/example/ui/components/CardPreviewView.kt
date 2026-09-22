package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CardDecorationStyle
import com.example.model.CardTemplate
import com.example.ui.theme.GreatVibesFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily

@Composable
fun CardPreviewView(
    template: CardTemplate,
    customTitle: String? = null,
    recipientName: String = "",
    customMessage: String? = null,
    senderName: String = "",
    textSizeSp: Float = 15f,
    textAlign: String = "Center",
    showRecipient: Boolean = true,
    showSender: Boolean = true,
    decorationStyle: CardDecorationStyle? = null,
    fontStyle: String = "Classic",
    aspectRatio: String = "Standard",
    stickers: List<String> = emptyList(),
    photoUri: String? = null,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(template.primaryColorHex)
    val secondaryColor = Color(template.secondaryColorHex)
    val accentColor = Color(template.accentColorHex)

    val displayTitle = customTitle?.takeIf { it.isNotBlank() } ?: template.title
    val displayMessage = customMessage?.takeIf { it.isNotBlank() } ?: template.defaultMessage
    val activeStyle = decorationStyle ?: template.decorationStyle

    val titleFontFamily = when (fontStyle) {
        "Script" -> GreatVibesFontFamily
        "Serif" -> PlayfairDisplayFontFamily
        "Modern" -> FontFamily.SansSerif
        else -> PlayfairDisplayFontFamily
    }

    val messageFontFamily = when (fontStyle) {
        "Script" -> GreatVibesFontFamily
        "Serif" -> PlayfairDisplayFontFamily
        "Modern" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }

    val composeTextAlign = when (textAlign) {
        "Left" -> TextAlign.Start
        "Right" -> TextAlign.End
        else -> TextAlign.Center
    }

    val columnAlignment = when (textAlign) {
        "Left" -> Alignment.Start
        "Right" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val cardModifier = when (aspectRatio) {
        "Square" -> modifier.fillMaxWidth().aspectRatio(1f)
        "Story" -> modifier.fillMaxWidth().aspectRatio(9f / 16f)
        "Landscape" -> modifier.fillMaxWidth().aspectRatio(16f / 9f)
        else -> modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier
            .clip(RoundedCornerShape(24.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = primaryColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            secondaryColor
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Native holiday visual background drawing (trees, fireworks, snowflakes, confetti, etc.)
            CardHolidayVisualBackground(
                decorationStyle = activeStyle,
                accentColor = accentColor,
                modifier = Modifier.matchParentSize()
            )

            // Inner ornate festive border frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = accentColor.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = columnAlignment
                ) {
                    // Top festive indicator row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeStyle) {
                                CardDecorationStyle.CHRISTMAS_TREE -> "🎄  ★  🎄"
                                CardDecorationStyle.SNOWFLAKES -> "❄  ✦  ❄"
                                CardDecorationStyle.BELLS -> "🔔  ✦  🔔"
                                CardDecorationStyle.ORNAMENTS -> "✨  🔴  ✨"
                                CardDecorationStyle.GIFT_BOXES -> "🎁  ✦  🎁"
                                CardDecorationStyle.SANTA_MAGIC -> "🎅  ★  ✨"
                                CardDecorationStyle.FIREWORKS -> "🎆  ✦  🎆"
                                CardDecorationStyle.CONFETTI -> "🎉  ★  🎉"
                                CardDecorationStyle.YEAR_2027 -> "✨  2027  ✨"
                                CardDecorationStyle.STARS -> "✦  ⭐  ✦"
                            },
                            fontSize = 15.sp,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge / Subtitle pill
                    Surface(
                        color = accentColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = template.subtitle.uppercase(),
                            color = accentColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Greeting Main Title
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = titleFontFamily,
                            fontSize = if (fontStyle == "Script") 32.sp else 24.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        textAlign = composeTextAlign,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional User Photo Frame
                    if (photoUri != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, accentColor, CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Personal Photo",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Recipient Name (with toggle)
                    if (showRecipient && recipientName.isNotBlank()) {
                        Text(
                            text = "Dearest $recipientName,",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontFamily = messageFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (fontStyle == "Script") 20.sp else 16.sp
                            ),
                            color = accentColor,
                            textAlign = composeTextAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Card Body Message
                    val messageSize = if (fontStyle == "Script") (textSizeSp * 1.3f).sp else textSizeSp.sp
                    Text(
                        text = displayMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = messageFontFamily,
                            lineHeight = if (fontStyle == "Script") (textSizeSp * 1.7f).sp else (textSizeSp * 1.45f).sp,
                            fontSize = messageSize
                        ),
                        color = Color.White.copy(alpha = 0.94f),
                        textAlign = composeTextAlign,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Decorative User Selected Stickers
                    if (stickers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            stickers.forEach { sticker ->
                                Text(
                                    text = sticker,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sender Name (with toggle)
                    if (showSender && senderName.isNotBlank()) {
                        Text(
                            text = "— Warmly, $senderName",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontFamily = messageFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (fontStyle == "Script") 18.sp else 14.5.sp
                            ),
                            color = accentColor,
                            textAlign = composeTextAlign,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Bottom subtle holiday star accent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "★  •  ★",
                            fontSize = 12.sp,
                            color = accentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
