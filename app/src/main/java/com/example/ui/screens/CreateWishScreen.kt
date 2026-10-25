package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.ScheduleGreetingDialog
import com.example.ui.theme.HolidayPineGreen
import com.example.utils.WishExporter
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GeneratorOccasion
import com.example.model.GeneratorRecipient
import com.example.model.GeneratorRelationship
import com.example.model.GeneratorTone
import com.example.model.WishGenerationResult
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.viewmodel.HolidayViewModel
import com.example.data.subscription.SubscriptionManager
import com.example.utils.ClipboardHelper
import com.example.utils.ShareHelper

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import coil.compose.AsyncImage
import com.example.ui.components.CardPreviewView
import com.example.data.repository.CardTemplatesRepository
import com.example.utils.CardImageExporter

@Composable
fun CreateWishScreen(
    viewModel: HolidayViewModel,
    modifier: Modifier = Modifier
) {
    val genOccasion by viewModel.genOccasion.collectAsStateWithLifecycle()
    val genRecipient by viewModel.genRecipient.collectAsStateWithLifecycle()
    val genRelationship by viewModel.genRelationship.collectAsStateWithLifecycle()
    val genTone by viewModel.genTone.collectAsStateWithLifecycle()
    val personalMessage by viewModel.personalMessage.collectAsStateWithLifecycle()
    val generatedWishes by viewModel.generatedWishes.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var selectedWishPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedWishPhotoUri = uri?.toString()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_wish_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
    ) {
        item {
            // Header Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, HolidayCrimson.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HolidayCrimson,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Wish Customizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Choose your occasion, recipient, relationship & tone to create the perfect message.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // 1. Occasion
        item {
            SelectionSectionTitle("1. Select Occasion")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratorOccasion.values().forEach { occasion ->
                    val isSelected = occasion == genOccasion
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGenOccasion(occasion) },
                        label = { Text(occasion.label) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("occasion_${occasion.name.lowercase()}")
                    )
                }
            }
        }

        // 2. Recipient
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SelectionSectionTitle("2. Select Recipient")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratorRecipient.values().forEach { recipient ->
                    val isSelected = recipient == genRecipient
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGenRecipient(recipient) },
                        label = { Text(recipient.label) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("recipient_${recipient.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Relationship
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SelectionSectionTitle("3. Select Relationship")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratorRelationship.values().forEach { relationship ->
                    val isSelected = relationship == genRelationship
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGenRelationship(relationship) },
                        label = { Text(relationship.label) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("relationship_${relationship.name.lowercase()}")
                    )
                }
            }
        }

        // 4. Tone
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SelectionSectionTitle("4. Select Tone")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratorTone.values().forEach { tone ->
                    val isSelected = tone == genTone
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGenTone(tone) },
                        label = { Text(tone.label) },
                        shape = RoundedCornerShape(14.dp),
                        colors = chipColors(isSelected),
                        modifier = Modifier.testTag("tone_${tone.name.lowercase()}")
                    )
                }
            }
        }

        // 5. Personal Note / Message
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SelectionSectionTitle("5. Personal Message (Optional)")
            OutlinedTextField(
                value = personalMessage,
                onValueChange = { viewModel.setPersonalMessage(it) },
                placeholder = {
                    Text(
                        "e.g., Grateful for your help this year, see you at Christmas dinner!",
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HolidayCrimson,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personal_message_input")
            )
        }

        // 6. Add Photo to Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SelectionSectionTitle("6. Add Photo Inside Card (Optional)")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, if (selectedWishPhotoUri != null) HolidayPineGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("upload_wish_photo_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedWishPhotoUri != null) {
                        AsyncImage(
                            model = selectedWishPhotoUri,
                            contentDescription = "Uploaded Card Photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, HolidayGold, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(HolidayCrimson.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = HolidayCrimson,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedWishPhotoUri != null) "Photo Added to Card 📸" else "Upload Card Photo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (selectedWishPhotoUri != null) "This photo will appear inside the card when shared" else "Add a picture to be embedded inside the holiday card",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedWishPhotoUri != null) {
                        IconButton(
                            onClick = { selectedWishPhotoUri = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove photo", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HolidayPineGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Generate Button
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { viewModel.generateWish() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HolidayCrimson,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_wish_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HolidayGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Wish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Generated Results
        if (generatedWishes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = { viewModel.generateWish() },
                        modifier = Modifier.testTag("regenerate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate variations",
                            tint = HolidayCrimson
                        )
                    }
                }
            }

            items(generatedWishes, key = { it.id }) { result ->
                GeneratedWishCard(
                    result = result,
                    photoUri = selectedWishPhotoUri,
                    isFavorite = favoriteIds.contains(result.id),
                    onToggleFavorite = { viewModel.toggleFavoriteGenerated(result) },
                    onRegenerate = { viewModel.regenerateGeneratedWish(result.id) }
                )
            }
        }
    }
}

@Composable
private fun SelectionSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun chipColors(isSelected: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = HolidayCrimson,
    selectedLabelColor = Color.White,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun GeneratedWishCard(
    result: WishGenerationResult,
    photoUri: String? = null,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRegenerate: () -> Unit
) {
    val context = LocalContext.current
    val isProUser by SubscriptionManager.isPro.collectAsStateWithLifecycle()

    val matchingTemplate = remember(result.occasion) {
        when (result.occasion) {
            GeneratorOccasion.CHRISTMAS -> CardTemplatesRepository.getChristmasTemplates().first()
            GeneratorOccasion.NEW_YEAR -> CardTemplatesRepository.getNewYearTemplates().first()
            GeneratorOccasion.HOLIDAY_SEASON -> CardTemplatesRepository.getSeasonTemplates().first()
            else -> CardTemplatesRepository.getAllTemplates().first()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("generated_wish_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.2.dp, if (photoUri != null) HolidayGold else HolidayGold.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HolidayGold.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "${result.occasion.label} • ${result.tone.label}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp).testTag("fav_generated_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) HolidayCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Card Preview with photo if photo was uploaded
            if (photoUri != null) {
                CardPreviewView(
                    template = matchingTemplate,
                    customTitle = "${result.occasion.label} Greeting",
                    recipientName = result.recipient.label,
                    customMessage = result.wishText,
                    senderName = "",
                    photoUri = photoUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    text = result.wishText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!isProUser) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.8.dp, HolidayGold.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "✨ Made with Holiday Wishes 🎄",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            var showScheduleDialog by remember { mutableStateOf(false) }

            if (showScheduleDialog) {
                ScheduleGreetingDialog(
                    greetingTitle = "${result.occasion.label} Wish",
                    initialRecipientName = result.recipient.label,
                    messageText = result.wishText,
                    onDismiss = { showScheduleDialog = false },
                    onScheduledSuccess = { timeStr ->
                        Toast.makeText(
                            context,
                            "Wish scheduled for $timeStr! 🚀 You will receive a prompt to auto-send on WhatsApp when the time arrives.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }

            // Primary Card Sharing & Download Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share Exact Card with Photo to Facebook, WhatsApp, etc.
                Button(
                    onClick = {
                        ShareHelper.shareCard(
                            context = context,
                            title = "${result.occasion.label} Greeting",
                            recipient = result.recipient.label,
                            message = result.wishText,
                            sender = "",
                            template = matchingTemplate,
                            photoUri = photoUri
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).height(40.dp).testTag("share_card_with_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (photoUri != null) "Share Card 📸" else "Share Card 🎨",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Download Card to Gallery
                OutlinedButton(
                    onClick = {
                        val bitmap = CardImageExporter.renderCardBitmap(
                            template = matchingTemplate,
                            customTitle = "${result.occasion.label} Greeting",
                            recipientName = result.recipient.label,
                            customMessage = result.wishText,
                            senderName = "",
                            fontStyle = "Classic",
                            isPro = isProUser,
                            photoUri = photoUri,
                            context = context
                        )
                        val saved = CardImageExporter.saveBitmapToGallery(context, bitmap, "${result.occasion.label} Card")
                        if (saved) {
                            Toast.makeText(context, "Card with photo saved to Gallery! 📥✨", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Could not save card to gallery", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HolidayPineGreen),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = HolidayPineGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", color = HolidayPineGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Actions Row: Schedule, Copy Text, Share Text Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showScheduleDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            onRegenerate()
                            Toast.makeText(context, "Wish regenerated with AI! ✨", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp).testTag("regenerate_generated_button_${result.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Regenerate with AI",
                            tint = HolidayGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val textToCopy = if (isProUser) result.wishText else "${result.wishText}\n\n✨ Made with Holiday Wishes 🎄"
                            ClipboardHelper.copyText(context, textToCopy)
                        },
                        modifier = Modifier.size(34.dp).testTag("copy_generated_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { ShareHelper.shareWish(context, result.wishText, "Holiday Wish") },
                        modifier = Modifier.size(34.dp).testTag("share_text_generated_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
