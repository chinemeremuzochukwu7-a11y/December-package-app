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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.model.AiTone
import com.example.model.AiWishLength
import com.example.model.CardDecorationStyle
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.ShareHelper

enum class CardFilterType {
    ALL, SEASON, CHRISTMAS, NEW_YEAR, PRO_ONLY, FREE_ONLY
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
    val customTemplateMessages by viewModel.customCardTemplateMessages.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(CardFilterType.ALL) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var showAiCardCreationDialog by remember { mutableStateOf(false) }
    var proDialogTargetTemplate by remember { mutableStateOf<CardTemplate?>(null) }
    var rewardedAdTemplate by remember { mutableStateOf<CardTemplate?>(null) }

    val filteredTemplates = when (selectedFilter) {
        CardFilterType.ALL -> CardTemplatesRepository.getAllTemplates()
        CardFilterType.SEASON -> CardTemplatesRepository.getSeasonTemplates()
        CardFilterType.CHRISTMAS -> CardTemplatesRepository.getChristmasTemplates()
        CardFilterType.NEW_YEAR -> CardTemplatesRepository.getNewYearTemplates()
        CardFilterType.PRO_ONLY -> CardTemplatesRepository.getProTemplates()
        CardFilterType.FREE_ONLY -> CardTemplatesRepository.getFreeTemplates()
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
                                text = "Unlock 1,000+ luxury cards with Pro ($9.99/mo) or daily rewarded ads",
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

        // AI Card Creation Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showAiCardCreationDialog = true }
                .testTag("ai_create_card_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = HolidayCrimson.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.2.dp, HolidayCrimson.copy(alpha = 0.5f))
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
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HolidayCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Create Card with AI ✨",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HolidayCrimson
                        )
                        Text(
                            text = "Let AI design your card, personalized greeting & theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HolidayCrimson
                ) {
                    Text(
                        text = "CREATE ✨",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                    text = "❄️ Season (${CardTemplatesRepository.getSeasonTemplates().size})",
                    isSelected = selectedFilter == CardFilterType.SEASON,
                    onClick = { selectedFilter = CardFilterType.SEASON },
                    modifier = Modifier.testTag("filter_season_cards")
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
                val customMsg = customTemplateMessages[template.id]
                GridCardTemplateItem(
                    template = template,
                    customMessage = customMsg,
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
                        if (!isUnlocked) {
                            rewardedAdTemplate = template
                            Toast.makeText(context, "Watch a short ad to unlock and share this Pro card! 🎁", Toast.LENGTH_SHORT).show()
                        } else {
                            ShareHelper.shareCard(
                                context = context,
                                title = template.title,
                                recipient = "",
                                message = customMsg ?: template.defaultMessage,
                                sender = "",
                                template = template
                            )
                        }
                    },
                    onRegenerateText = {
                        viewModel.regenerateCardTemplateDefaultMessage(template) {
                            Toast.makeText(context, "Card text regenerated with AI! ✨", Toast.LENGTH_SHORT).show()
                        }
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

    // AI Card Creator Dialog
    if (showAiCardCreationDialog) {
        CreateCardWithAiDialog(
            onDismiss = { showAiCardCreationDialog = false },
            onCreateCard = { occasion, recipient, sender, tone, themeStyle, length, prompt, photoUri ->
                val template = viewModel.createCardWithAi(
                    occasion = occasion,
                    recipientName = recipient,
                    senderName = sender,
                    tone = tone,
                    themeStyle = themeStyle,
                    length = length,
                    customPrompt = prompt,
                    photoUri = photoUri
                )
                Toast.makeText(context, "AI card crafted! Opening customizer ✨", Toast.LENGTH_SHORT).show()
                onNavigateToCustomize(template)
            }
        )
    }
}

@Composable
private fun GridCardTemplateItem(
    template: CardTemplate,
    customMessage: String?,
    isUnlocked: Boolean,
    onCustomizeClick: () -> Unit,
    onQuickShareClick: () -> Unit,
    onRegenerateText: () -> Unit
) {
    val displayTemplate = remember(template, customMessage) {
        if (customMessage != null) template.copy(defaultMessage = customMessage) else template
    }

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
                    template = displayTemplate,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Quick AI Regenerate text button on card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't like this text?",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    onClick = onRegenerateText,
                    shape = RoundedCornerShape(10.dp),
                    color = HolidayGold.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("regenerate_card_template_${template.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Regenerate with AI",
                            tint = Color(0xFF6B4E00),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Regenerate with AI ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B4E00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
fun CreateCardWithAiDialog(
    onDismiss: () -> Unit,
    onCreateCard: (
        occasion: CardOccasion,
        recipient: String,
        sender: String,
        tone: AiTone,
        themeStyle: CardDecorationStyle,
        length: AiWishLength,
        customPrompt: String,
        photoUri: String?
    ) -> Unit
) {
    var occasion by remember { mutableStateOf(CardOccasion.CHRISTMAS) }
    var recipientName by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var tone by remember { mutableStateOf(AiTone.WARM) }
    var themeStyle by remember { mutableStateOf(CardDecorationStyle.STARS) }
    var length by remember { mutableStateOf(AiWishLength.MEDIUM) }
    var customPrompt by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HolidayCrimson,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Card with AI ✨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = HolidayCrimson
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Let AI craft a personalized card template, design theme, and greeting message.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Occasion
                Text(
                    text = "Occasion",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val occasions: List<Pair<CardOccasion, String>> = listOf(
                        CardOccasion.CHRISTMAS to "🎄 Christmas",
                        CardOccasion.NEW_YEAR to "🎉 New Year",
                        CardOccasion.HOLIDAY_SEASON to "❄️ Season"
                    )
                    occasions.forEach { (occ, label) ->
                        val isSelected = occasion == occ
                        FilterChip(
                            selected = isSelected,
                            onClick = { occasion = occ },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HolidayCrimson,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 2. Recipient Name
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Recipient Name (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g., Mom, Sweetheart, Alex", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ai_card_recipient_input")
                )

                // 3. Sender Name
                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Sender Name (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g., With love, David", fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ai_card_sender_input")
                )

                // 4. Tone
                Text(
                    text = "Tone",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tones: List<Pair<AiTone, String>> = listOf(
                        AiTone.WARM to "Warm ❤️",
                        AiTone.LOVING to "Loving 💖",
                        AiTone.POETIC to "Poetic 📜",
                        AiTone.FUNNY to "Funny 😄",
                        AiTone.INSPIRATIONAL to "Inspiring 🌟",
                        AiTone.PRAYERFUL to "Blessed ✝️",
                        AiTone.PROFESSIONAL to "Formal 💼"
                    )
                    tones.forEach { (t, label) ->
                        val isSelected = tone == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { tone = t },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HolidayGold,
                                selectedLabelColor = Color(0xFF332200)
                            )
                        )
                    }
                }

                // 5. Theme Style
                Text(
                    text = "Card Theme Style",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val styles: List<Pair<CardDecorationStyle, String>> = listOf(
                        CardDecorationStyle.STARS to "✨ Stars",
                        CardDecorationStyle.SNOWFLAKES to "❄️ Snowflakes",
                        CardDecorationStyle.CHRISTMAS_TREE to "🎄 Tree",
                        CardDecorationStyle.ORNAMENTS to "🔴 Ornaments",
                        CardDecorationStyle.FIREWORKS to "🎆 Fireworks",
                        CardDecorationStyle.BELLS to "🔔 Bells",
                        CardDecorationStyle.GIFT_BOXES to "🎁 Gifts"
                    )
                    styles.forEach { (st, label) ->
                        val isSelected = themeStyle == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { themeStyle = st },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HolidayPineGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 6. Length
                Text(
                    text = "Greeting Length",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val lengths: List<Pair<AiWishLength, String>> = listOf(
                        AiWishLength.SHORT to "Short",
                        AiWishLength.MEDIUM to "Medium",
                        AiWishLength.LONG to "Long",
                        AiWishLength.POETIC to "Poetic"
                    )
                    lengths.forEach { (len, label) ->
                        val isSelected = length == len
                        FilterChip(
                            selected = isSelected,
                            onClick = { length = len },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HolidayCrimson,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 7. Custom Prompt / Details
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    label = { Text("Personal Memories or Details (Optional)", fontSize = 12.sp) },
                    placeholder = { Text("e.g. mention our family ski trip, baking cookies...", fontSize = 11.5.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ai_card_custom_prompt_input")
                )

                // 8. Add Photo to Card (Zero-Permission Photo Picker)
                Text(
                    text = "Add Photo Inside Card (Optional)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = HolidayPineGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedPhotoUri != null) "Photo Attached 📸" else "Upload Card Photo",
                            color = HolidayPineGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (selectedPhotoUri != null) {
                        IconButton(
                            onClick = { selectedPhotoUri = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove photo",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateCard(occasion, recipientName, senderName, tone, themeStyle, length, customPrompt, selectedPhotoUri)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_create_card_with_ai")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Card ✨", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
