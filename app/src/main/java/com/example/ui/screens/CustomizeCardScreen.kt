package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import com.example.data.ads.AdManager
import com.example.data.subscription.SubscriptionManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CardPreviewView
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.ui.viewmodel.HolidayViewModel
import com.example.utils.CardImageExporter

@Composable
fun CustomizeCardScreen(
    viewModel: HolidayViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMyCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val template by viewModel.selectedCardTemplate.collectAsStateWithLifecycle()
    val recipientName by viewModel.cardRecipientName.collectAsStateWithLifecycle()
    val customMessage by viewModel.cardCustomMessage.collectAsStateWithLifecycle()
    val senderName by viewModel.cardSenderName.collectAsStateWithLifecycle()
    val textSizeSp by viewModel.cardTextSizeSp.collectAsStateWithLifecycle()
    val textAlign by viewModel.cardTextAlign.collectAsStateWithLifecycle()
    val showRecipient by viewModel.showRecipient.collectAsStateWithLifecycle()
    val showSender by viewModel.showSender.collectAsStateWithLifecycle()
    val cardFontStyle by viewModel.cardFontStyle.collectAsStateWithLifecycle()
    val cardAspectRatio by viewModel.cardAspectRatio.collectAsStateWithLifecycle()
    val cardStickers by viewModel.cardStickers.collectAsStateWithLifecycle()
    val cardPhotoUri by viewModel.cardPhotoUri.collectAsStateWithLifecycle()

    var cardViewRef by remember { mutableStateOf<View?>(null) }
    var isFlipped by remember { mutableStateOf(false) }
    val flipAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500),
        label = "card_flip"
    )

    // Zero-permission Android Photo Picker for personal holiday photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setCardPhotoUri(uri.toString())
            Toast.makeText(context, "Holiday photo framed in card! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    // Storage permission launcher for legacy Android versions (SDK <= 28)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveCardImageToGallery(context, cardViewRef, template.title)
        } else {
            Toast.makeText(context, "Storage permission is needed to save to gallery", Toast.LENGTH_LONG).show()
        }
    }

    val credits by SubscriptionManager.credits.collectAsStateWithLifecycle()
    val isPro by SubscriptionManager.isPro.collectAsStateWithLifecycle()
    var showCreditDialog by remember { mutableStateOf(false) }
    val activity = context as? Activity

    val executeCardActionWithCredits = { onProceed: () -> Unit ->
        if (isPro) {
            onProceed()
        } else {
            if (credits < SubscriptionManager.CARD_CREATION_CREDIT_COST) {
                showCreditDialog = true
            } else {
                // Free users must watch rewarded ad before starting creation
                if (activity != null) {
                    AdManager.showRewardedAd(
                        activity = activity,
                        onRewardEarned = { /* 1 credit added */ },
                        onDismiss = {
                            if (SubscriptionManager.useCredits(SubscriptionManager.CARD_CREATION_CREDIT_COST)) {
                                onProceed()
                            } else {
                                showCreditDialog = true
                            }
                        }
                    )
                } else {
                    if (SubscriptionManager.useCredits(SubscriptionManager.CARD_CREATION_CREDIT_COST)) {
                        onProceed()
                    }
                }
            }
        }
    }

    val executeCardShare = {
        if (isPro) {
            shareCardAction(
                context = context,
                view = cardViewRef,
                templateTitle = template.title,
                recipient = recipientName,
                message = customMessage,
                sender = senderName
            )
        } else {
            if (activity != null) {
                AdManager.showRewardedAd(
                    activity = activity,
                    onRewardEarned = { /* 1 credit added */ },
                    onDismiss = {
                        shareCardAction(
                            context = context,
                            view = cardViewRef,
                            templateTitle = template.title,
                            recipient = recipientName,
                            message = customMessage,
                            sender = senderName
                        )
                    }
                )
            } else {
                shareCardAction(
                    context = context,
                    view = cardViewRef,
                    templateTitle = template.title,
                    recipient = recipientName,
                    message = customMessage,
                    sender = senderName
                )
            }
        }
    }

    if (showCreditDialog) {
        AlertDialog(
            onDismissRequest = { showCreditDialog = false },
            title = {
                Text("Need More Credits 🎨", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Creating a card costs 15 credits. You currently have $credits credits.\n\nWatch a rewarded ad to earn 1 credit each, or upgrade to Pro for unlimited card creation!"
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
            .testTag("customize_card_screen")
    ) {
        // Screen Top Navigation Bar
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
                    modifier = Modifier.testTag("customize_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Personalize Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HolidayGold.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = template.badge,
                        color = HolidayGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Credit status banner for card creation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPro) HolidayGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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
                            text = if (isPro) "PRO Unlimited Card Studio" else "Card Creation: 15 Credits",
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

        // Scrollable content: Live Preview + Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE PREVIEW SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE CARD PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        TextButton(
                            onClick = { isFlipped = !isFlipped }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Flip Card",
                                tint = HolidayCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFlipped) "Front View" else "3D Flip View",
                                fontSize = 12.sp,
                                color = HolidayCrimson,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Capture View reference
                    val view = LocalView.current
                    cardViewRef = view

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = flipAngle
                                cameraDistance = 14f * density
                            }
                    ) {
                        CardPreviewView(
                            template = template,
                            recipientName = recipientName,
                            customMessage = customMessage,
                            senderName = senderName,
                            textSizeSp = textSizeSp,
                            textAlign = textAlign,
                            showRecipient = showRecipient,
                            showSender = showSender,
                            fontStyle = cardFontStyle,
                            aspectRatio = cardAspectRatio,
                            stickers = cardStickers,
                            photoUri = cardPhotoUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_card_preview")
                        )
                    }
                }
            }

            // 2. PRIMARY ACTION BUTTONS: SAVE CARD, SAVE IMAGE, SHARE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save Card Button (to My Cards / Room)
                    Button(
                        onClick = {
                            executeCardActionWithCredits {
                                viewModel.saveCurrentCard {
                                    Toast.makeText(context, "Card saved to My Cards! 🎄", Toast.LENGTH_SHORT).show()
                                    onNavigateToMyCards()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_card_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Save Card to My Cards", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Save Image to Gallery Button
                        OutlinedButton(
                            onClick = {
                                executeCardActionWithCredits {
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                        permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        saveCardImageToGallery(context, cardViewRef, template.title)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, HolidayPineGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("save_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = HolidayPineGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Image",
                                color = HolidayPineGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Share Card Button
                        Button(
                            onClick = {
                                executeCardShare()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HolidayGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("share_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF3B2700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share Card",
                                color = Color(0xFF3B2700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 3. PERSONALIZATION INPUTS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Card Personalization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Recipient Name Field
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { viewModel.updateCardRecipient(it) },
                        label = { Text("Recipient's Name (e.g., Mary, Grandma)") },
                        placeholder = { Text("Mary") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HolidayCrimson
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipient_name_input")
                    )

                    // Personal Message Field
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { viewModel.updateCardMessage(it) },
                        label = { Text("Personal Greeting Message") },
                        placeholder = { Text("Wishing you a beautiful Christmas filled with love and happiness.") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HolidayCrimson
                        ),
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_message_input")
                    )

                    // Sender Name Field
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { viewModel.updateCardSender(it) },
                        label = { Text("From / Sender Name (e.g., Michael)") },
                        placeholder = { Text("Michael") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HolidayCrimson
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sender_name_input")
                    )
                }
            }

            // 4. OPTIONAL FORMATTING CONTROLS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Card Formatting & Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Text Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Message Text Size",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${textSizeSp.toInt()} sp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HolidayCrimson
                            )
                        }
                        Slider(
                            value = textSizeSp,
                            onValueChange = { viewModel.updateCardTextSize(it) },
                            valueRange = 12f..20f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = HolidayCrimson,
                                activeTrackColor = HolidayCrimson
                            ),
                            modifier = Modifier.testTag("text_size_slider")
                        )
                    }

                    // Text Alignment Options
                    Column {
                        Text(
                            text = "Text Alignment",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AlignChip(
                                label = "Left",
                                icon = Icons.Default.FormatAlignLeft,
                                isSelected = textAlign == "Left",
                                onClick = { viewModel.updateCardTextAlign("Left") },
                                modifier = Modifier.testTag("align_left_chip")
                            )
                            AlignChip(
                                label = "Center",
                                icon = Icons.Default.FormatAlignCenter,
                                isSelected = textAlign == "Center",
                                onClick = { viewModel.updateCardTextAlign("Center") },
                                modifier = Modifier.testTag("align_center_chip")
                            )
                            AlignChip(
                                label = "Right",
                                icon = Icons.Default.FormatAlignRight,
                                isSelected = textAlign == "Right",
                                onClick = { viewModel.updateCardTextAlign("Right") },
                                modifier = Modifier.testTag("align_right_chip")
                            )
                        }
                    }

                    // Show / Hide Recipient Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Show Recipient Name",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Display 'Dearest [Name]' greeting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showRecipient,
                            onCheckedChange = { viewModel.toggleShowRecipient(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = HolidayCrimson, checkedTrackColor = HolidayCrimson.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("toggle_recipient_switch")
                        )
                    }

                    // Show / Hide Sender Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Show Sender Name",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Display '— Warmly, [Name]' sign-off",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showSender,
                            onCheckedChange = { viewModel.toggleShowSender(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = HolidayCrimson, checkedTrackColor = HolidayCrimson.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("toggle_sender_switch")
                        )
                    }

                    // Card Aspect Ratio Selector
                    Column {
                        Text(
                            text = "Card Aspect Ratio (Stories & Status)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val ratios = listOf(
                                "Standard" to "Standard (3:4)",
                                "Square" to "Square (1:1)",
                                "Story" to "Story (9:16)",
                                "Landscape" to "Landscape (16:9)"
                            )
                            ratios.forEach { (key, label) ->
                                FilterChip(
                                    selected = cardAspectRatio == key,
                                    onClick = { viewModel.setCardAspectRatio(key) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HolidayCrimson,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Typography & Font Style
                    Column {
                        Text(
                            text = "Typography Style",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val styles = listOf(
                                "Classic" to "Classic",
                                "Script" to "Festive Script",
                                "Serif" to "Elegant Serif",
                                "Modern" to "Modern Clean"
                            )
                            styles.forEach { (key, label) ->
                                FilterChip(
                                    selected = cardFontStyle == key,
                                    onClick = { viewModel.setCardFontStyle(key) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HolidayGold,
                                        selectedLabelColor = Color(0xFF332000)
                                    )
                                )
                            }
                        }
                    }

                    // Holiday Photo Frame (Zero-Permission Photo Picker)
                    Column {
                        Text(
                            text = "Add Your Photo to Card",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = HolidayPineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (cardPhotoUri != null) "Change Photo" else "Select Photo",
                                    color = HolidayPineGreen,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (cardPhotoUri != null) {
                                OutlinedButton(
                                    onClick = { viewModel.setCardPhotoUri(null) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Holiday Stickers & Stamps
                    Column {
                        Text(
                            text = "Holiday Stickers & Stamps (Tap to add)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val stickersList = listOf("🎄", "⭐", "🎁", "🔔", "🎅", "❄️", "🥂", "🕊️", "🍾", "🕯️")
                            stickersList.forEach { sticker ->
                                val isSelected = cardStickers.contains(sticker)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) HolidayGold.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) HolidayGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable { viewModel.toggleCardSticker(sticker) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = sticker, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AlignChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
        },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HolidayCrimson,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}

private fun saveCardImageToGallery(context: Context, view: View?, title: String) {
    if (view == null) {
        Toast.makeText(context, "Card preview not ready yet", Toast.LENGTH_SHORT).show()
        return
    }
    val bitmap = CardImageExporter.viewToBitmap(view)
    val success = CardImageExporter.saveBitmapToGallery(context, bitmap, title)
    if (success) {
        Toast.makeText(context, "Card saved to your gallery! ✨", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, "Failed to save card to gallery", Toast.LENGTH_SHORT).show()
    }
}

private fun shareCardAction(
    context: Context,
    view: View?,
    templateTitle: String,
    recipient: String,
    message: String,
    sender: String
) {
    if (view != null) {
        val bitmap = CardImageExporter.viewToBitmap(view)
        val shareText = buildString {
            if (recipient.isNotBlank()) append("To $recipient:\n\n")
            append(message)
            if (sender.isNotBlank()) append("\n\nFrom: $sender")
            append("\n\n✨ Created with Holiday Wishes")
        }
        CardImageExporter.shareCardImage(context, bitmap, templateTitle, shareText)
    } else {
        // Fallback to text sharing
        com.example.utils.ShareHelper.shareCard(context, templateTitle, recipient, message, sender)
    }
}
