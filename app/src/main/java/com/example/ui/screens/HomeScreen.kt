package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.CardTemplatesRepository
import com.example.data.repository.WishesRepository
import com.example.model.CardTemplate
import com.example.model.Wish
import com.example.model.WishCategory
import com.example.ui.components.CardPreviewView
import com.example.ui.components.HolidayCountdownCard
import com.example.ui.components.SnowfallEffect
import com.example.ui.components.WishCard
import com.example.ui.theme.GreatVibesFontFamily
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayCrimsonDark
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.ClipboardHelper
import com.example.utils.ShareHelper

@Composable
fun HomeScreen(
    viewModel: HolidayViewModel,
    onNavigateToCreateWish: () -> Unit,
    onNavigateToAiWishGenerator: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToCustomizeCard: (CardTemplate) -> Unit,
    onNavigateToWishesCategory: (WishCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val snowEffectsEnabled by viewModel.snowEffectsEnabled.collectAsStateWithLifecycle()
    val featuredCardTemplates = CardTemplatesRepository.getAllTemplates().take(4)
    val christmasWishes = WishesRepository.getChristmasWishes().take(4)
    val newYearWishes = WishesRepository.getNewYearWishes().take(4)
    val popularWishes = WishesRepository.getPopularWishes().take(6)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Section
            item {
                HeroHeaderSection(
                    snowEffectsEnabled = snowEffectsEnabled,
                    onToggleSnowEffects = { viewModel.toggleSnowEffects() },
                    onCreateWishClick = onNavigateToAiWishGenerator,
                    onCreateCardClick = onNavigateToCards
                )
            }

            // Section: Three Main Quick Actions
            item {
                ThreeMainActionsRow(
                    onWishesClick = { onNavigateToWishesCategory(WishCategory.ALL) },
                    onAiGeneratorClick = onNavigateToAiWishGenerator,
                    onCardsClick = onNavigateToCards
                )
            }

            // Section: Live Holiday Countdown
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    HolidayCountdownCard()
                }
            }

            // Section: Prominent "Create a Wish with AI" Banner
            item {
                AiWishPromoCard(
                    onGenerateClick = onNavigateToAiWishGenerator
                )
            }

        // Section: Create a Holiday Card Spotlight
        item {
            SectionHeader(
                title = "Create a Holiday Card",
                subtitle = "Pick a template, personalize & share",
                onSeeAllClick = onNavigateToCards
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(featuredCardTemplates, key = { "home_card_${it.id}" }) { template ->
                    HomeCardSpotlightItem(
                        template = template,
                        onCustomizeClick = {
                            viewModel.prepareCardForCustomization(template)
                            onNavigateToCustomizeCard(template)
                        }
                    )
                }
            }
        }

        // Section: Christmas Wishes
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Christmas Wishes",
                subtitle = "Warmth, wonder & holiday cheer",
                onSeeAllClick = { onNavigateToWishesCategory(WishCategory.CHRISTMAS) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(christmasWishes, key = { it.id }) { wish ->
                    HomeWishHorizontalCard(
                        wish = wish,
                        isFavorite = favoriteIds.contains(wish.id),
                        onToggleFavorite = { viewModel.toggleFavorite(wish) }
                    )
                }
            }
        }

        // Section: New Year Wishes
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "New Year Wishes",
                subtitle = "Hope, inspiration & new beginnings",
                onSeeAllClick = { onNavigateToWishesCategory(WishCategory.NEW_YEAR) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(newYearWishes, key = { it.id }) { wish ->
                    HomeWishHorizontalCard(
                        wish = wish,
                        isFavorite = favoriteIds.contains(wish.id),
                        onToggleFavorite = { viewModel.toggleFavorite(wish) }
                    )
                }
            }
        }

        // Section: Popular Wishes
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Popular Wishes",
                subtitle = "Most loved by families & friends",
                onSeeAllClick = { onNavigateToWishesCategory(WishCategory.ALL) }
            )
        }

        items(popularWishes, key = { "pop_${it.id}" }) { wish ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                WishCard(
                    wish = wish,
                    isFavorite = favoriteIds.contains(wish.id),
                    onToggleFavorite = { viewModel.toggleFavorite(wish) }
                )
            }
        }
    }

    // Ambient floating snowfall across screen
    SnowfallEffect(
        snowflakeCount = 20,
        enabled = snowEffectsEnabled
    )
    }
}

@Composable
private fun HeroHeaderSection(
    snowEffectsEnabled: Boolean,
    onToggleSnowEffects: () -> Unit,
    onCreateWishClick: () -> Unit,
    onCreateCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = HolidayCrimson),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HolidayCrimson,
                            HolidayCrimsonDark
                        )
                    )
                )
                .padding(22.dp)
        ) {
            // Hero card local snowfall
            SnowfallEffect(
                snowflakeCount = 30,
                enabled = snowEffectsEnabled
            )

            Column {
                // Festive Pill Badge & Snow Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HolidayGold.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎄 HOLIDAY WISHES",
                                color = HolidayGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Snowfall Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (snowEffectsEnabled) HolidayGold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onToggleSnowEffects() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (snowEffectsEnabled) "❄️ Snow ON" else "❄️ Snow OFF",
                                color = if (snowEffectsEnabled) HolidayGold else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Heading
                Text(
                    text = "Share Joy.\nShare Wishes.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = PlayfairDisplayFontFamily,
                        lineHeight = 34.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Short description
                Text(
                    text = "Celebrate Christmas & New Year with heartfelt messages, festive greeting cards, and joyous blessings for those you love.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Generate with AI button
                    Button(
                        onClick = onCreateWishClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HolidayGold,
                            contentColor = Color(0xFF332000)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_generate_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Generator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Create a Card button
                    Button(
                        onClick = onCreateCardClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create a Card",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Three Main Actions required by Prompt:
 * 🎄 Holiday Wishes
 * ✨ AI Wish Generator
 * 🎨 Create a Card
 */
@Composable
private fun ThreeMainActionsRow(
    onWishesClick: () -> Unit,
    onAiGeneratorClick: () -> Unit,
    onCardsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 🎄 Holiday Wishes
        MainActionCard(
            modifier = Modifier.weight(1f),
            iconEmoji = "🎄",
            title = "Holiday\nWishes",
            subtitle = "Library",
            containerColor = HolidayCrimson.copy(alpha = 0.08f),
            accentColor = HolidayCrimson,
            onClick = onWishesClick,
            testTag = "home_action_wishes"
        )

        // ✨ AI Wish Generator
        MainActionCard(
            modifier = Modifier.weight(1f),
            iconEmoji = "✨",
            title = "AI Wish\nGenerator",
            subtitle = "Custom AI",
            containerColor = HolidayGold.copy(alpha = 0.16f),
            accentColor = Color(0xFF7A5800),
            onClick = onAiGeneratorClick,
            testTag = "home_action_ai"
        )

        // 🎨 Create a Card
        MainActionCard(
            modifier = Modifier.weight(1f),
            iconEmoji = "🎨",
            title = "Create\na Card",
            subtitle = "Templates",
            containerColor = HolidayPineGreen.copy(alpha = 0.08f),
            accentColor = HolidayPineGreen,
            onClick = onCardsClick,
            testTag = "home_action_cards"
        )
    }
}

@Composable
private fun MainActionCard(
    modifier: Modifier = Modifier,
    iconEmoji: String,
    title: String,
    subtitle: String,
    containerColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Prominent "Create a Wish with AI" Section required on Home Screen
 */
@Composable
private fun AiWishPromoCard(
    onGenerateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("ai_wish_promo_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, HolidayGold.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            HolidayGold.copy(alpha = 0.15f),
                            HolidayCrimson.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HolidayGold.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "✨ AI POWERED",
                            color = Color(0xFF6B4E00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create a Wish with AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Create a personal message for someone special.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onGenerateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HolidayCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("home_generate_with_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HolidayGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate with AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCardSpotlightItem(
    template: CardTemplate,
    onCustomizeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .testTag("home_card_spotlight_${template.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            CardPreviewView(
                template = template,
                modifier = Modifier.height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCustomizeClick,
                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Customize Card", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "See All",
                color = HolidayCrimson,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HomeWishHorizontalCard(
    wish: Wish,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
            .testTag("home_wish_${wish.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HolidayCrimson.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = wish.occasion,
                            color = HolidayCrimson,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) HolidayCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = wish.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { ClipboardHelper.copyText(context, wish.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy wish",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { ShareHelper.shareWish(context, wish.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share wish",
                        tint = HolidayCrimson,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
