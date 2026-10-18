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
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import com.example.model.CardTemplate
import com.example.model.CardOccasion
import com.example.ui.components.ScheduleGreetingDialog
import java.io.File
import java.io.FileOutputStream
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
import com.example.ui.components.ProUpgradeDialog
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
    var cardBounds by remember { mutableStateOf<Rect?>(null) }
    var selectedTextLength by remember { mutableStateOf(CardTextLength.MEDIUM) }
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
            saveCardImageToGallery(context, cardViewRef, cardBounds, template.title)
        } else {
            Toast.makeText(context, "Storage permission is needed to save to gallery", Toast.LENGTH_LONG).show()
        }
    }

    val credits by SubscriptionManager.credits.collectAsStateWithLifecycle()
    val isPro by SubscriptionManager.isPro.collectAsStateWithLifecycle()
    var showCreditDialog by remember { mutableStateOf(false) }
    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDownloadSuccessDialog by remember { mutableStateOf(false) }
    var cachedImagePath by remember { mutableStateOf<String?>(null) }
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
                template = template,
                templateTitle = template.title,
                recipient = recipientName,
                message = customMessage,
                sender = senderName,
                fontStyle = cardFontStyle
            )
        } else {
            if (activity != null) {
                AdManager.showRewardedAd(
                    activity = activity,
                    onRewardEarned = { /* 1 credit added */ },
                    onDismiss = {
                        shareCardAction(
                            context = context,
                            template = template,
                            templateTitle = template.title,
                            recipient = recipientName,
                            message = customMessage,
                            sender = senderName,
                            fontStyle = cardFontStyle
                        )
                    }
                )
            } else {
                shareCardAction(
                    context = context,
                    template = template,
                    templateTitle = template.title,
                    recipient = recipientName,
                    message = customMessage,
                    sender = senderName,
                    fontStyle = cardFontStyle
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showCreditDialog = false
                            if (activity != null) {
                                AdManager.showRewardedAd(activity)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
                    ) {
                        Text("Watch Ad (+1)")
                    }
                    Button(
                        onClick = {
                            showCreditDialog = false
                            showProUpgradeDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HolidayGold)
                    ) {
                        Text("Upgrade Pro", color = Color(0xFF332000), fontWeight = FontWeight.Bold)
                    }
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

    if (showProUpgradeDialog) {
        ProUpgradeDialog(
            targetTemplate = template,
            onPurchaseTier = { tierId ->
                viewModel.purchaseProTier(tierId)
                Toast.makeText(context, "Welcome to Holiday Wishes Pro! Watermark removed! 🎉", Toast.LENGTH_LONG).show()
                showProUpgradeDialog = false
            },
            onWatchRewardedAd = {
                viewModel.unlockCardViaRewardedAd(template.id)
                Toast.makeText(context, "Card unlocked! 🎄", Toast.LENGTH_SHORT).show()
                showProUpgradeDialog = false
            },
            onRestorePurchases = {
                val restored = viewModel.restorePurchases()
                Toast.makeText(
                    context,
                    if (restored) "Purchases restored! Active Pro access confirmed."
                    else "No existing purchases found.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showProUpgradeDialog = false }
        )
    }

    // Dialog showing confirmation and next steps when card is downloaded
    if (showDownloadSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Card Downloaded to Device!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your holiday card has been saved in full HD to your device's Photos/Gallery (in Pictures/HolidayWishes).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "You can also schedule it right now to automatically deliver on WhatsApp, Telegram, Instagram, SMS, Email, or other platforms at any exact time & date!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadSuccessDialog = false
                        showScheduleDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Auto-Send ⏰", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadSuccessDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Schedule Delivery Dialog for exact date and time
    if (showScheduleDialog) {
        ScheduleGreetingDialog(
            greetingTitle = template.title,
            initialRecipientName = recipientName,
            messageText = customMessage,
            cardImagePath = cachedImagePath,
            onDismiss = { showScheduleDialog = false },
            onScheduledSuccess = { formattedDateTime ->
                Toast.makeText(
                    context,
                    "Greeting scheduled for $formattedDateTime! 🚀 You will receive a high-priority prompt to send when the time arrives.",
                    Toast.LENGTH_LONG
                ).show()
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

        // Credit status banner for card creation (compact, responsive, never overflows)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isPro) HolidayGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, if (isPro) HolidayGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPro) Icons.Default.Stars else Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = if (isPro) HolidayGold else HolidayCrimson,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPro) "👑 PRO Unlimited Card Studio" else "Card Creation: 15 Credits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!isPro) {
                        Text(
                            text = "Balance: $credits Credits • Watch ad to create",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                if (!isPro) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = HolidayCrimson.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            if (activity != null) {
                                AdManager.showRewardedAd(activity)
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = HolidayCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+1 Credit",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = HolidayCrimson,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
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
                            .onGloballyPositioned { coordinates ->
                                cardBounds = coordinates.boundsInRoot()
                            }
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

            // Remove Watermark Banner for Free Users
            if (!isPro) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HolidayGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProUpgradeDialog = true }
                        .testTag("remove_watermark_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Watermark included on free cards",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Upgrade to Pro ($9.99/mo or $79/yr) to remove watermark & unlock all luxury cards",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HolidayGold,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Remove",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF332000),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
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
                        // Download Card to Device Button
                        Button(
                            onClick = {
                                executeCardActionWithCredits {
                                    val pristineBitmap = CardImageExporter.renderCardBitmap(
                                        template = template,
                                        customTitle = template.title,
                                        recipientName = recipientName,
                                        customMessage = customMessage,
                                        senderName = senderName,
                                        fontStyle = cardFontStyle,
                                        isPro = isPro
                                    )
                                    val success = CardImageExporter.saveBitmapToGallery(context, pristineBitmap, template.title)
                                    if (success) {
                                        Toast.makeText(context, "Card downloaded to device gallery! 📥✨", Toast.LENGTH_LONG).show()
                                        showDownloadSuccessDialog = true
                                    } else {
                                        Toast.makeText(context, "Could not save card to gallery", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HolidayPineGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("save_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Download Card",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Share Card Button
                        OutlinedButton(
                            onClick = {
                                executeCardShare()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, HolidayGold),
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

                    // Schedule Auto-Send at exact date and time
                    OutlinedButton(
                        onClick = {
                            if (cardViewRef != null) {
                                try {
                                    val bitmap = CardImageExporter.viewToBitmap(cardViewRef!!)
                                    val cacheFolder = File(context.cacheDir, "images")
                                    if (!cacheFolder.exists()) cacheFolder.mkdirs()
                                    val cacheFile = File(cacheFolder, "card_sched_${System.currentTimeMillis()}.png")
                                    val stream = FileOutputStream(cacheFile)
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                                    stream.close()
                                    cachedImagePath = cacheFile.absolutePath
                                } catch (ignored: Exception) {}
                            }
                            showScheduleDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, HolidayCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("schedule_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = HolidayCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Schedule Auto-Send (WhatsApp / Media / Apps)",
                            color = HolidayCrimson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
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

                    // Text Length Selection & Token Deduction Generator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HolidayCrimson.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, HolidayCrimson.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        text = "AI Greeting Length",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = HolidayGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, HolidayGold)
                                ) {
                                    Text(
                                        text = "Cost: ${selectedTextLength.tokensRequired} 🪙",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HolidayGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Length selection chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CardTextLength.values().forEach { lengthOption ->
                                    val isSelected = selectedTextLength == lengthOption
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedTextLength = lengthOption },
                                        label = {
                                            Column {
                                                Text(
                                                    "${lengthOption.label} (${lengthOption.tokensRequired}🪙)",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 11.5.sp
                                                )
                                                Text(
                                                    lengthOption.approxWords,
                                                    fontSize = 9.sp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("length_chip_${lengthOption.name.lowercase()}"),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = HolidayCrimson,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Live Credit Calculation Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HolidayGold.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth().testTag("card_credit_calculation_box")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⚡", fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Live Credit Calculation & Deduction",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Selected: ${selectedTextLength.label} (${selectedTextLength.approxWords})\n" +
                                               "• Required: ${selectedTextLength.tokensRequired} Tokens 🪙 (deducted upon creation)\n" +
                                               "• Current Balance: $credits Credits\n" +
                                               "• Balance After Generation: ${if (isPro) "Unlimited (Pro Active 👑)" else "${maxOf(0, credits - selectedTextLength.tokensRequired)} Credits"}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Generate and Deduct Tokens Button
                            Button(
                                onClick = {
                                    val tokensNeeded = selectedTextLength.tokensRequired
                                    if (SubscriptionManager.hasEnoughCredits(tokensNeeded)) {
                                        SubscriptionManager.useCredits(tokensNeeded)
                                        val generatedMsg = generateCardMessage(template, recipientName, senderName, selectedTextLength)
                                        viewModel.updateCardMessage(generatedMsg)
                                        Toast.makeText(
                                            context,
                                            "Generated ${selectedTextLength.label} greeting! Used $tokensNeeded tokens 🪙",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        showCreditDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_greeting_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Generate ${selectedTextLength.label} Greeting (${selectedTextLength.tokensRequired} Tokens 🪙)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

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

private fun saveCardImageToGallery(
    context: Context,
    view: View?,
    bounds: Rect?,
    title: String,
    onSuccess: ((String?) -> Unit)? = null
) {
    if (view == null) {
        Toast.makeText(context, "Card preview not ready yet", Toast.LENGTH_SHORT).show()
        return
    }
    val bitmap = CardImageExporter.viewToBitmap(view, bounds)
    val success = CardImageExporter.saveBitmapToGallery(context, bitmap, title)
    var cachedPath: String? = null
    try {
        val cacheFolder = File(context.cacheDir, "images")
        if (!cacheFolder.exists()) cacheFolder.mkdirs()
        val cacheFile = File(cacheFolder, "card_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(cacheFile)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        cachedPath = cacheFile.absolutePath
    } catch (ignored: Exception) {}

    if (success) {
        Toast.makeText(context, "Card downloaded to device gallery! 📥✨", Toast.LENGTH_LONG).show()
        com.example.utils.RateAppManager.triggerMagicEvent(context, "✨ Magical Card Saved!")
        onSuccess?.invoke(cachedPath)
    } else {
        Toast.makeText(context, "Failed to save card to gallery", Toast.LENGTH_SHORT).show()
    }
}

private fun shareCardAction(
    context: Context,
    template: CardTemplate,
    templateTitle: String,
    recipient: String,
    message: String,
    sender: String,
    fontStyle: String = "Classic"
) {
    val isPro = com.example.data.subscription.SubscriptionManager.isPro.value
    val bitmap = CardImageExporter.renderCardBitmap(
        template = template,
        customTitle = templateTitle,
        recipientName = recipient,
        customMessage = message,
        senderName = sender,
        fontStyle = fontStyle,
        isPro = isPro
    )
    val shareText = buildString {
        if (recipient.isNotBlank()) append("To $recipient:\n\n")
        append(message)
        if (sender.isNotBlank()) append("\n\nFrom: $sender")
        if (!isPro) {
            append("\n\n✨ Made with Holiday Wishes 🎄")
        }
    }
    CardImageExporter.shareCardImage(context, bitmap, templateTitle, shareText)
    com.example.utils.RateAppManager.triggerMagicEvent(context, "✨ Magical Card Shared!")
}

enum class CardTextLength(val label: String, val approxWords: String, val tokensRequired: Int) {
    SHORT("Short", "15-25 words", 5),
    MEDIUM("Medium", "40-60 words", 10),
    LONG("Long", "80-120 words", 15),
    POETIC("Poetic", "130-180 words", 20)
}

private fun generateCardMessage(
    template: CardTemplate,
    recipientName: String,
    senderName: String,
    length: CardTextLength
): String {
    val name = if (recipientName.isNotBlank()) recipientName else "friend"
    val occasion = template.occasion

    return when (length) {
        CardTextLength.SHORT -> {
            when (occasion) {
                CardOccasion.CHRISTMAS ->
                    "Wishing you peace, cozy warmth, and boundless joy this Christmas, $name! May your season be filled with love."
                CardOccasion.NEW_YEAR ->
                    "Cheers to 365 days of health, happiness, and extraordinary success in the New Year, $name!"
                CardOccasion.HOLIDAY_SEASON ->
                    "Sending you warm holiday greetings, $name! May this festive season surround your home in peaceful light."
                else ->
                    "Sending you warm holiday greetings, $name! May this festive season surround your home in peaceful light."
            }
        }
        CardTextLength.MEDIUM -> {
            when (occasion) {
                CardOccasion.CHRISTMAS ->
                    "May the wonder and cozy warmth of Christmas illuminate your heart, $name. Wishing you and your family serene peace, delicious feasts, sparkling laughter, and cherished memories that last forever!"
                CardOccasion.NEW_YEAR ->
                    "Happy New Year, $name! May the coming chapter open brilliant doors of new opportunities, vibrant good health, and joyful breakthroughs. Step boldly into your brightest year yet!"
                CardOccasion.HOLIDAY_SEASON ->
                    "As the snow falls softly and holiday lights glow warm by the fireside, I am sending you my heartfelt blessings, $name. May this festive season bring restful moments, comforting smiles, and deep happiness."
                else ->
                    "As the snow falls softly and holiday lights glow warm by the fireside, I am sending you my heartfelt blessings, $name. May this festive season bring restful moments, comforting smiles, and deep happiness."
            }
        }
        CardTextLength.LONG -> {
            when (occasion) {
                CardOccasion.CHRISTMAS ->
                    "Merry Christmas, dearest $name! As the tree lights twinkle and carolers sing of peace on Earth, may your home become a peaceful sanctuary of love and joyous togetherness. May the quiet miracles of the holiday season fill your heart with hope, provide healing and comfort, and surround you with the laughter of those you hold dear. Here is to a joyful, blessed Christmas!"
                CardOccasion.NEW_YEAR ->
                    "Wishing you a magnificent and prosperous New Year, $name! May every morning of the coming year greet you with renewed strength, sharp clarity, and bold ambition. Leave all burdens behind in the old year, and welcome the unlimited possibilities waiting ahead. May your family be blessed with radiant health, financial abundance, and everlasting peace of mind."
                CardOccasion.HOLIDAY_SEASON ->
                    "Warmest seasonal greetings to you and yours, $name! The holiday season is a beautiful reminder to pause, count our blessings, and celebrate the gift of shared friendship and family. May your days be wrapped in cozy sweaters, sweet cinnamon treats, and heartwarming conversations. May the glow of the holidays guide your steps with peace, harmony, and joy all winter long."
                else ->
                    "Warmest seasonal greetings to you and yours, $name! The holiday season is a beautiful reminder to pause, count our blessings, and celebrate the gift of shared friendship and family. May your days be wrapped in cozy sweaters, sweet cinnamon treats, and heartwarming conversations. May the glow of the holidays guide your steps with peace, harmony, and joy all winter long."
            }
        }
        CardTextLength.POETIC -> {
            when (occasion) {
                CardOccasion.CHRISTMAS ->
                    "Dearest $name, like constellations glowing softly through winter skies, may Christmas wrap your home in celestial grace and wonder. May peace descend upon your family like pure white snow, filling every chamber with warmth, divine blessings, and enduring laughter. May the timeless symphony of love and goodwill resonate through all your festive days."
                CardOccasion.NEW_YEAR ->
                    "To dearest $name: As the old year turns its final page, may the dawn of the New Year arrive like golden sunlight over quiet mountains. May health, profound triumph, and boundless happiness rise up to meet you at every sunrise. Cherish the journey and step forward into a year overflowing with peace, divine favor, and radiant beauty."
                else ->
                    "Dearest $name, may the magic of this holiday season unfold like a rich tapestry of golden wonders. May cozy evenings by the fireplace, joyful reunions with loved ones, and sweet reflections of gratitude bring immense delight to your soul. Wishing you boundless peace and triumphant joy all winter long."
            }
        }
    }
}
