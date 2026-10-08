package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    isFavorite = favoriteIds.contains(result.id),
                    onToggleFavorite = { viewModel.toggleFavoriteGenerated(result) }
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
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("generated_wish_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.wishText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            val isProUser by SubscriptionManager.isPro.collectAsStateWithLifecycle()
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        val textToCopy = if (isProUser) result.wishText else "${result.wishText}\n\n✨ Made with Holiday Wishes 🎄"
                        ClipboardHelper.copyText(context, textToCopy)
                    },
                    modifier = Modifier.testTag("copy_generated_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { ShareHelper.shareWish(context, result.wishText, "Holiday Wish") },
                    modifier = Modifier.testTag("share_generated_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
