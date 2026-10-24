package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import com.example.data.local.FavoriteWishEntity
import com.example.utils.ClipboardHelper
import com.example.utils.ShareHelper
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SavedCardEntity
import com.example.data.repository.CardTemplatesRepository
import com.example.model.CardDecorationStyle
import com.example.model.CardOccasion
import com.example.model.CardTemplate
import com.example.ui.components.CardPreviewView
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.CardImageExporter

@Composable
fun MyCardsScreen(
    viewModel: HolidayViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    val favoriteWishes by viewModel.favorites.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    var cardToDelete by remember { mutableStateOf<SavedCardEntity?>(null) }
    var activeViewRef by remember { mutableStateOf<View?>(null) }
    val view = LocalView.current
    activeViewRef = view

    // Storage permission launcher for legacy Android versions (SDK <= 28)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted. Tap Save Image again to download.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permission is required to save to gallery", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_cards_screen")
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("my_cards_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Save cards and wishes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tab Row: Saved Cards vs Saved Wishes
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = HolidayCrimson
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Saved Cards (${savedCards.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Saved Wishes (${favoriteWishes.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // Empty state or List of Saved Cards
        if (savedCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HolidayGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = HolidayCrimson,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "You haven't created any cards yet.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choose a template to create your first holiday card.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateToTemplates,
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("choose_template_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Choose a Card Template", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(savedCards, key = { it.id }) { cardEntity ->
                    SavedCardItem(
                        card = cardEntity,
                        onShareClick = {
                            com.example.utils.ShareHelper.shareCard(
                                context = context,
                                title = cardEntity.title,
                                recipient = if (cardEntity.showRecipient) cardEntity.recipientName else "",
                                message = cardEntity.message,
                                sender = if (cardEntity.showSender) cardEntity.senderName else ""
                            )
                        },
                        onRegenerateText = {
                            viewModel.regenerateSavedCardTextWithAi(cardEntity.id) {
                                Toast.makeText(context, "Card text regenerated with AI! ✨", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSaveImageClick = {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                if (activeViewRef != null) {
                                    val bitmap = CardImageExporter.viewToBitmap(activeViewRef!!)
                                    val success = CardImageExporter.saveBitmapToGallery(context, bitmap, cardEntity.title)
                                    if (success) {
                                        Toast.makeText(context, "Card saved to your gallery! ✨", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Failed to save card to gallery", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            cardToDelete = cardEntity
                        }
                    )
                }
            }
        }
        } else {
            // Saved Wishes view
            if (favoriteWishes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HolidayGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(88.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = HolidayCrimson,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "No saved wishes yet.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap the heart icon on any wish in the Wishes Library or AI Wish Generator to save it here for offline access.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteWishes, key = { it.id }) { wish ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = wish.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.removeFavoriteById(wish.id)
                                            Toast.makeText(context, "Wish removed from saved", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove favorite",
                                            tint = HolidayCrimson,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = wish.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = {
                                            viewModel.regenerateFavoriteWishWithAi(wish.id) {
                                                Toast.makeText(context, "Saved wish regenerated with AI! ✨", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = HolidayGold.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.5f)),
                                        modifier = Modifier.testTag("regenerate_saved_wish_${wish.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Regenerate with AI",
                                                tint = HolidayGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "AI Regenerate",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HolidayGold
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                ClipboardHelper.copyText(context, wish.text)
                                                Toast.makeText(context, "Wish copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                        }
                                        IconButton(
                                            onClick = {
                                                ShareHelper.shareWish(context, wish.text, "Holiday Wish")
                                            }
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (cardToDelete != null) {
            val target = cardToDelete!!
            AlertDialog(
                onDismissRequest = { cardToDelete = null },
                title = {
                    Text(text = "Delete Saved Card?", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Are you sure you want to permanently delete this saved card for ${if (target.recipientName.isNotBlank()) target.recipientName else target.title}?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSavedCard(target.id)
                            cardToDelete = null
                            Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cardToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SavedCardItem(
    card: SavedCardEntity,
    onShareClick: () -> Unit,
    onRegenerateText: () -> Unit,
    onSaveImageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Reconstruct template from entity
    val decoration = try {
        CardDecorationStyle.valueOf(card.decorationStyle)
    } catch (e: Exception) {
        CardDecorationStyle.STARS
    }

    val occasion = try {
        CardOccasion.valueOf(card.occasion)
    } catch (e: Exception) {
        CardOccasion.CHRISTMAS
    }

    val pseudoTemplate = CardTemplate(
        id = card.templateId,
        title = card.title,
        subtitle = card.subtitle,
        defaultMessage = card.message,
        occasion = occasion,
        primaryColorHex = card.primaryColorHex,
        secondaryColorHex = card.secondaryColorHex,
        accentColorHex = card.accentColorHex,
        badge = "Saved Card",
        decorationStyle = decoration
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_${card.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Preview
            CardPreviewView(
                template = pseudoTemplate,
                recipientName = card.recipientName,
                customMessage = card.message,
                senderName = card.senderName,
                textSizeSp = card.textSizeSp,
                textAlign = card.textAlign,
                showRecipient = card.showRecipient,
                showSender = card.showSender,
                decorationStyle = decoration,
                modifier = Modifier.height(260.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Regenerate Card Message Button
            Button(
                onClick = onRegenerateText,
                colors = ButtonDefaults.buttonColors(containerColor = HolidayGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("regenerate_saved_card_${card.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF3E2C00),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Regenerate Text with AI ✨",
                    color = Color(0xFF3E2C00),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Share, Save Image, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share
                Button(
                    onClick = onShareClick,
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Save Image
                OutlinedButton(
                    onClick = onSaveImageClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HolidayPineGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = HolidayPineGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Save Image", color = HolidayPineGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Delete
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("delete_saved_card_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete saved card",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
