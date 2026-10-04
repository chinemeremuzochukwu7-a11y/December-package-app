package com.example.ui.screens

import android.app.Activity
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ads.AdManager
import com.example.data.local.AiGeneratedWishEntity
import com.example.data.subscription.SubscriptionManager
import com.example.model.AiLanguage
import com.example.model.AiOccasion
import com.example.model.AiRecipient
import com.example.model.AiTone
import com.example.model.AiWishResponse
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.ClipboardHelper
import com.example.utils.ShareHelper

@Composable
fun AiWishGeneratorScreen(
    viewModel: HolidayViewModel,
    onNavigateToCreateCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val occasion by viewModel.aiOccasion.collectAsStateWithLifecycle()
    val recipient by viewModel.aiRecipient.collectAsStateWithLifecycle()
    val recipientName by viewModel.aiRecipientName.collectAsStateWithLifecycle()
    val tone by viewModel.aiTone.collectAsStateWithLifecycle()
    val language by viewModel.aiLanguage.collectAsStateWithLifecycle()
    val personalDetails by viewModel.aiPersonalDetails.collectAsStateWithLifecycle()
    val isGenerating by viewModel.aiIsGenerating.collectAsStateWithLifecycle()
    val generatedResult by viewModel.aiGeneratedResult.collectAsStateWithLifecycle()
    val errorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()
    val history by viewModel.aiWishHistory.collectAsStateWithLifecycle()
    val credits by SubscriptionManager.credits.collectAsStateWithLifecycle()
    val isPro by SubscriptionManager.isPro.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showCreditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity

    val onPerformGenerateWish = {
        if (isPro) {
            viewModel.generateAiWish()
        } else {
            if (credits < SubscriptionManager.WISH_CREATION_CREDIT_COST) {
                showCreditDialog = true
            } else {
                // Free user before start creating must watch rewarded ads
                if (activity != null) {
                    AdManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { /* +1 credit earned */ },
                        onDismiss = {
                            if (SubscriptionManager.useCredits(SubscriptionManager.WISH_CREATION_CREDIT_COST)) {
                                viewModel.generateAiWish()
                            } else {
                                showCreditDialog = true
                            }
                        }
                    )
                } else {
                    if (SubscriptionManager.useCredits(SubscriptionManager.WISH_CREATION_CREDIT_COST)) {
                        viewModel.generateAiWish()
                    }
                }
            }
        }
    }

    if (showCreditDialog) {
        AlertDialog(
            onDismissRequest = { showCreditDialog = false },
            title = {
                Text("Need More Credits 🌟", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Creating a wish costs 10 credits. You currently have $credits credits.\n\nWatch a rewarded ad to earn 1 credit each, or upgrade to Pro for unlimited generation!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreditDialog = false
                        if (activity != null) {
                            AdManager.showRewardedAd(activity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
                ) {
                    Text("Watch Ad (+1 Credit)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreditDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_wish_generator_screen")
    ) {
        // Tab row: Generator & History
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = HolidayCrimson
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Generator", fontWeight = FontWeight.SemiBold)
                    }
                },
                modifier = Modifier.testTag("tab_ai_generator")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (history.isNotEmpty()) "History (${history.size})" else "History",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                modifier = Modifier.testTag("tab_ai_history")
            )
        }

        // Credit status banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPro) HolidayGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPro) Icons.Default.Stars else Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (isPro) HolidayGold else HolidayCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isPro) "PRO Active • Unlimited" else "10 Credits per Wish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (!isPro) {
                            Text(
                                text = "Your Balance: $credits Credits • Watch ad to create",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!isPro) {
                    TextButton(
                        onClick = {
                            if (activity != null) {
                                AdManager.showRewardedAd(activity)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedTabIndex == 0) {
            AiGeneratorContent(
                occasion = occasion,
                recipient = recipient,
                recipientName = recipientName,
                tone = tone,
                language = language,
                personalDetails = personalDetails,
                isGenerating = isGenerating,
                generatedResult = generatedResult,
                errorMessage = errorMessage,
                onOccasionChange = { viewModel.setAiOccasion(it) },
                onRecipientChange = { viewModel.setAiRecipient(it) },
                onRecipientNameChange = { viewModel.setAiRecipientName(it) },
                onToneChange = { viewModel.setAiTone(it) },
                onLanguageChange = { viewModel.setAiLanguage(it) },
                onPersonalDetailsChange = { viewModel.setAiPersonalDetails(it) },
                onGenerateWish = onPerformGenerateWish,
                onGenerateAgain = onPerformGenerateWish,
                onSaveWish = { text, occ -> viewModel.saveAiWishToFavorites(text, occ) },
                onCreateCard = { text, name ->
                    viewModel.prepareCardFromAiWish(text, name)
                    onNavigateToCreateCard()
                },
                onClearError = { viewModel.clearAiErrorMessage() }
            )
        } else {
            AiHistoryContent(
                history = history,
                onDelete = { viewModel.deleteAiWishHistory(it) },
                onCreateCard = { text, name ->
                    viewModel.prepareCardFromAiWish(text, name)
                    onNavigateToCreateCard()
                },
                onSaveWish = { text, occ -> viewModel.saveAiWishToFavorites(text, occ) }
            )
        }
    }
}

@Composable
private fun AiGeneratorContent(
    occasion: AiOccasion,
    recipient: AiRecipient,
    recipientName: String,
    tone: AiTone,
    language: AiLanguage,
    personalDetails: String,
    isGenerating: Boolean,
    generatedResult: AiWishResponse?,
    errorMessage: String?,
    onOccasionChange: (AiOccasion) -> Unit,
    onRecipientChange: (AiRecipient) -> Unit,
    onRecipientNameChange: (String) -> Unit,
    onToneChange: (AiTone) -> Unit,
    onLanguageChange: (AiLanguage) -> Unit,
    onPersonalDetailsChange: (String) -> Unit,
    onGenerateWish: () -> Unit,
    onGenerateAgain: () -> Unit,
    onSaveWish: (String, String) -> Unit,
    onCreateCard: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("ai_header_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HolidayCrimson.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, HolidayCrimson.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HolidayCrimson.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = HolidayCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "AI Wish Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tell us who you're writing for and what you want to say.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Section: Occasion
        item {
            SectionTitle(title = "Occasion", subtitle = "Choose the celebration")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiOccasion.values().forEach { occ ->
                    val isSelected = occ == occasion
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOccasionChange(occ) },
                        label = { Text("${occ.iconEmoji} ${occ.displayName}") },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("ai_occasion_${occ.name.lowercase()}")
                    )
                }
            }
        }

        // Section: Recipient
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionTitle(title = "Recipient", subtitle = "Who is this wish for?")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiRecipient.values().forEach { rec ->
                    val isSelected = rec == recipient
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRecipientChange(rec) },
                        label = { Text(rec.displayName) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("ai_recipient_${rec.name.lowercase()}")
                    )
                }
            }
        }

        // Section: Optional Recipient's Name
        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = recipientName,
                onValueChange = onRecipientNameChange,
                label = { Text("Recipient's name (optional)") },
                placeholder = { Text("e.g., Sarah, Mom, John") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HolidayCrimson,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_recipient_name_input")
            )
        }

        // Section: Tone
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionTitle(title = "Tone", subtitle = "Set the emotional mood")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiTone.values().forEach { t ->
                    val isSelected = t == tone
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToneChange(t) },
                        label = { Text(t.displayName) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("ai_tone_${t.name.lowercase()}")
                    )
                }
            }
        }

        // Section: Language
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionTitle(title = "Language", subtitle = "Select wish language")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiLanguage.values().forEach { lang ->
                    val isSelected = lang == language
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLanguageChange(lang) },
                        label = { Text("${lang.displayName} (${lang.nativeName})") },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("ai_lang_${lang.code}")
                    )
                }
            }
        }

        // Section: Personal details
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionTitle(title = "Personal Details", subtitle = "Tell the AI what you want to say...")
            OutlinedTextField(
                value = personalDetails,
                onValueChange = onPersonalDetailsChange,
                placeholder = {
                    Text(
                        "I want to wish my mother a peaceful Christmas and thank her for everything she has done for me.",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HolidayCrimson,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_personal_details_input")
            )
        }

        // Error message banner
        if (errorMessage != null) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_error_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Dismiss error")
                        }
                    }
                }
            }
        }

        // Generate Button
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onGenerateWish,
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolidayCrimson,
                    contentColor = Color.White,
                    disabledContainerColor = HolidayCrimson.copy(alpha = 0.6f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("ai_generate_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = HolidayGold,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Crafting your personalized wish...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HolidayGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ Generate Wish",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Generated Result Section
        if (generatedResult != null) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GeneratedResultCard(
                    result = generatedResult,
                    isGenerating = isGenerating,
                    onGenerateAgain = onGenerateAgain,
                    onSaveWish = onSaveWish,
                    onCreateCard = onCreateCard
                )
            }
        }
    }
}

@Composable
private fun GeneratedResultCard(
    result: AiWishResponse,
    isGenerating: Boolean,
    onGenerateAgain: () -> Unit,
    onSaveWish: (String, String) -> Unit,
    onCreateCard: (String, String) -> Unit
) {
    val context = LocalContext.current
    var isSaved by remember(result.id) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HolidayGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your Wish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HolidayGold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${result.occasion.displayName} • ${result.tone.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_result_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, HolidayGold.copy(alpha = 0.65f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Wish text with elegant formatting
                Text(
                    text = result.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 26.sp,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("ai_generated_text")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons row: Copy, Share, Save, Create Card, Generate Again
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy button
                    OutlinedButton(
                        onClick = { ClipboardHelper.copyText(context, result.text, "Wish copied! ✨") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("ai_copy_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Share button
                    OutlinedButton(
                        onClick = {
                            val isPro = SubscriptionManager.isPro.value
                            if (isPro) {
                                ShareHelper.shareWish(context, result.text, "AI Holiday Wish")
                            } else {
                                val act = context as? Activity
                                if (act != null) {
                                    AdManager.showRewardedAd(
                                        activity = act,
                                        onRewardEarned = { /* 1 credit added */ },
                                        onDismiss = {
                                            ShareHelper.shareWish(context, result.text, "AI Holiday Wish")
                                        }
                                    )
                                } else {
                                    ShareHelper.shareWish(context, result.text, "AI Holiday Wish")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("ai_share_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Save button
                    OutlinedButton(
                        onClick = {
                            onSaveWish(result.text, result.occasion.displayName)
                            isSaved = true
                            ClipboardHelper.copyText(context, "", "Wish saved to Favorites! 💖")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("ai_save_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isSaved) HolidayCrimson else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSaved) "Saved" else "Save", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create Card button
                    Button(
                        onClick = { onCreateCard(result.text, result.recipientName) },
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.3f).testTag("ai_create_card_button")
                    ) {
                        Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Card", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Generate Again button
                    OutlinedButton(
                        onClick = onGenerateAgain,
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("ai_generate_again_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Again", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiHistoryContent(
    history: List<AiGeneratedWishEntity>,
    onDelete: (String) -> Unit,
    onCreateCard: (String, String) -> Unit,
    onSaveWish: (String, String) -> Unit
) {
    val context = LocalContext.current

    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("ai_history_empty"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = HolidayGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HolidayCrimson,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your personalized wishes will appear here.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Generate personal holiday messages with AI and access them offline anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_history_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_history_item_${item.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HolidayCrimson.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${item.occasion} • ${item.tone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HolidayCrimson,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            val timeSpan = DateUtils.getRelativeTimeSpanString(
                                item.createdAt,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                            )

                            Text(
                                text = timeSpan.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy
                            IconButton(
                                onClick = { ClipboardHelper.copyText(context, item.text, "Wish copied! ✨") },
                                modifier = Modifier.size(36.dp).testTag("copy_history_${item.id}")
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }

                            // Share
                            IconButton(
                                onClick = { ShareHelper.shareWish(context, item.text, "Holiday Wish") },
                                modifier = Modifier.size(36.dp).testTag("share_history_${item.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                            }

                            // Create Card
                            IconButton(
                                onClick = { onCreateCard(item.text, item.recipientName) },
                                modifier = Modifier.size(36.dp).testTag("card_history_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Create Card",
                                    tint = HolidayCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete
                            IconButton(
                                onClick = { onDelete(item.id) },
                                modifier = Modifier.size(36.dp).testTag("delete_history_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun chipColors(isSelected: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = HolidayCrimson,
    selectedLabelColor = Color.White,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
