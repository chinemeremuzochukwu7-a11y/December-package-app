package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.ShareHelper

@Composable
fun CardsScreen(
    viewModel: HolidayViewModel,
    onNavigateToCustomize: (CardTemplate) -> Unit,
    onNavigateToMyCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    var selectedOccasion by remember { mutableStateOf<CardOccasion?>(CardOccasion.CHRISTMAS) }

    val filteredTemplates = when (selectedOccasion) {
        CardOccasion.CHRISTMAS -> CardTemplatesRepository.getChristmasTemplates()
        CardOccasion.NEW_YEAR -> CardTemplatesRepository.getNewYearTemplates()
        null -> CardTemplatesRepository.getAllTemplates()
        else -> CardTemplatesRepository.getAllTemplates()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("cards_screen")
    ) {
        // Top Action Bar: Category Chips & "My Cards" shortcut button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = "Christmas (${CardTemplatesRepository.getChristmasTemplates().size})",
                    isSelected = selectedOccasion == CardOccasion.CHRISTMAS,
                    onClick = { selectedOccasion = CardOccasion.CHRISTMAS },
                    modifier = Modifier.testTag("filter_christmas_cards")
                )
                CategoryPill(
                    text = "New Year (${CardTemplatesRepository.getNewYearTemplates().size})",
                    isSelected = selectedOccasion == CardOccasion.NEW_YEAR,
                    onClick = { selectedOccasion = CardOccasion.NEW_YEAR },
                    modifier = Modifier.testTag("filter_newyear_cards")
                )
                CategoryPill(
                    text = "All Cards (${CardTemplatesRepository.getAllTemplates().size})",
                    isSelected = selectedOccasion == null,
                    onClick = { selectedOccasion = null },
                    modifier = Modifier.testTag("filter_all_cards")
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
                        text = "My Cards (${savedCards.size})",
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
                GridCardTemplateItem(
                    template = template,
                    onCustomizeClick = {
                        viewModel.prepareCardForCustomization(template)
                        onNavigateToCustomize(template)
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
}

@Composable
private fun GridCardTemplateItem(
    template: CardTemplate,
    onCustomizeClick: () -> Unit,
    onQuickShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_template_${template.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Preview inside grid
            CardPreviewView(
                template = template,
                modifier = Modifier.height(260.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Customize & Quick Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCustomizeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("customize_button_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customize",
                        fontWeight = FontWeight.Bold,
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
