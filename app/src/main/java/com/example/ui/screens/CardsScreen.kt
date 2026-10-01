package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.CardTemplatesRepository
import com.example.model.CardOccasion
import com.example.model.CardTemplate
import com.example.ui.components.CardPreviewView
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.ShareHelper

enum class CardFilterType {
    ALL, PRO_ONLY, FREE_ONLY, CHRISTMAS, NEW_YEAR
}

@Composable
fun CardsScreen(
    viewModel: HolidayViewModel,
    onNavigateToCustomize: (CardTemplate) -> Unit,
    onNavigateToMyCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()
    val isVipUser by viewModel.isVipUser.collectAsStateWithLifecycle()
    val unlockedCardIds by viewModel.unlockedCardIds.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(CardFilterType.ALL) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var proDialogTargetTemplate by remember { mutableStateOf<CardTemplate?>(null) }
    var rewardedAdTemplate by remember { mutableStateOf<CardTemplate?>(null) }

    val filteredTemplates = when (selectedFilter) {
        CardFilterType.ALL -> CardTemplatesRepository.getAllTemplates()
        CardFilterType.PRO_ONLY -> CardTemplatesRepository.getProTemplates()
        CardFilterType.FREE_ONLY -> CardTemplatesRepository.getFreeTemplates()
        CardFilterType.CHRISTMAS -> CardTemplatesRepository.getChristmasTemplates()
        CardFilterType.NEW_YEAR -> CardTemplatesRepository.getNewYearTemplates()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("cards_screen")
    ) {
        // Pro Upgrade Banner (if not already Pro or VIP)
        if (!isProUser && !isVipUser) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable {
                        proDialogTargetTemplate = null
                        showProUpgradeDialog = true
                    }
                    .testTag("pro_upgrade_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HolidayGold.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.5.dp, HolidayGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Holiday Wishes Pro & VIP Pass",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Unlock 8+ luxury cards from $4.99 or with short videos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HolidayCrimson
                    ) {
                        Text(
                            text = "UNLOCK",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            // Already Pro / VIP Badge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = HolidayGold.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, HolidayGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isVipUser) "⭐" else "👑", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVipUser) "Ultimate VIP Active — All Luxury Cards & Unlimited AI Unlocked" else "Holiday Wishes Pro Active — All Templates Unlocked",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action Bar: Category Filter Chips & "My Cards" shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryPill(
                    text = "All (${CardTemplatesRepository.getAllTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.ALL,
                    onClick = { selectedFilter = CardFilterType.ALL },
                    modifier = Modifier.testTag("filter_all_cards")
                )
                CategoryPill(
                    text = "👑 Pro (${CardTemplatesRepository.getProTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.PRO_ONLY,
                    onClick = { selectedFilter = CardFilterType.PRO_ONLY },
                    modifier = Modifier.testTag("filter_pro_cards")
                )
                CategoryPill(
                    text = "✨ Free (${CardTemplatesRepository.getFreeTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.FREE_ONLY,
                    onClick = { selectedFilter = CardFilterType.FREE_ONLY },
                    modifier = Modifier.testTag("filter_free_cards")
                )
                CategoryPill(
                    text = "🎄 Christmas (${CardTemplatesRepository.getChristmasTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.CHRISTMAS,
                    onClick = { selectedFilter = CardFilterType.CHRISTMAS },
                    modifier = Modifier.testTag("filter_christmas_cards")
                )
                CategoryPill(
                    text = "🎉 New Year (${CardTemplatesRepository.getNewYearTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.NEW_YEAR,
                    onClick = { selectedFilter = CardFilterType.NEW_YEAR },
                    modifier = Modifier.testTag("filter_newyear_cards")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // My Cards Badge Button
            Surface(
                onClick = onNavigateToMyCards,
                shape = RoundedCornerShape(14.dp),
                color = HolidayGold.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, HolidayGold),
                modifier = Modifier.testTag("my_cards_nav_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "My Cards",
                        tint = HolidayGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "My (${savedCards.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HolidayGold
                    )
                }
            }
        }

        // Card Templates Grid (Adaptive 1 column on small phones, 2 columns on tablets/wide screens)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(filteredTemplates, key = { it.id }) { template ->
                val isUnlocked = viewModel.isCardUnlocked(template)
                GridCardTemplateItem(
                    template = template,
                    isUnlocked = isUnlocked,
                    onCustomizeClick = {
                        if (isUnlocked) {
                            viewModel.prepareCardForCustomization(template)
                            onNavigateToCustomize(template)
                        } else {
                            proDialogTargetTemplate = template
                            showProUpgradeDialog = true
                        }
                    },
                    onQuickShareClick = {
                        ShareHelper.shareCard(
                            context = context,
                            title = template.title,
                            recipient = "",
                            message = template.defaultMessage,
                            sender = ""
                        )
                    }
                )
            }
        }
    }

    // Pro Upgrade Dialog ($5 / $10 purchase or Watch Ad)
    if (showProUpgradeDialog) {
        ProUpgradeDialog(
            targetTemplate = proDialogTargetTemplate,
            onPurchaseTier = { tierId ->
                viewModel.purchaseProTier(tierId)
                Toast.makeText(context, "Welcome to Holiday Wishes Pro! Enjoy all luxury cards! 🎉", Toast.LENGTH_LONG).show()
                showProUpgradeDialog = false
            },
            onWatchRewardedAd = {
                rewardedAdTemplate = proDialogTargetTemplate
                showProUpgradeDialog = false
            },
            onRestorePurchases = {
                val restored = viewModel.restorePurchases()
                Toast.makeText(
                    context,
                    if (restored) "Purchases successfully restored! Welcome back! 🎉" else "No existing Google Play purchases found.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = {
                showProUpgradeDialog = false
                proDialogTargetTemplate = null
            }
        )
    }

    // Rewarded Ad Dialog (AdMob simulation with real unlock grant)
    if (rewardedAdTemplate != null) {
        RewardedAdDialog(
            template = rewardedAdTemplate!!,
            onRewardEarned = {
                val unlocked = rewardedAdTemplate!!
                viewModel.unlockCardViaRewardedAd(unlocked.id)
                Toast.makeText(context, "🎉 ${unlocked.title} unlocked for free!", Toast.LENGTH_SHORT).show()
                viewModel.prepareCardForCustomization(unlocked)
                onNavigateToCustomize(unlocked)
            },
            onDismiss = {
                rewardedAdTemplate = null
            }
        )
    }
}

@Composable
private fun GridCardTemplateItem(
    template: CardTemplate,
    isUnlocked: Boolean,
    onCustomizeClick: () -> Unit,
    onQuickShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_template_${template.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (template.isPro) 1.5.dp else 1.dp,
            color = if (template.isPro) HolidayGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Preview inside grid with lock indicator if locked
            Box(modifier = Modifier.fillMaxWidth()) {
                CardPreviewView(
                    template = template,
                    modifier = Modifier.height(260.dp)
                )

                if (template.isPro) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) HolidayPineGreen else Color.Black.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, if (isUnlocked) Color.White else HolidayGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) Color.White else HolidayGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isUnlocked) "PRO UNLOCKED" else "PRO EXCLUSIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.White else HolidayGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Customize & Quick Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCustomizeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isUnlocked) HolidayGold else HolidayCrimson
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("customize_button_${template.id}")
                ) {
                    Icon(
                        imageVector = if (!isUnlocked) Icons.Default.Lock else Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (!isUnlocked) Color(0xFF332000) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!isUnlocked) "Unlock PRO" else "Customize",
                        fontWeight = FontWeight.Bold,
                        color = if (!isUnlocked) Color(0xFF332000) else Color.White,
                        fontSize = 13.5.sp
                    )
                }

                OutlinedButton(
                    onClick = onQuickShareClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HolidayCrimson),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_card_button_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = HolidayCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        color = HolidayCrimson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HolidayCrimson,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
    )
}
