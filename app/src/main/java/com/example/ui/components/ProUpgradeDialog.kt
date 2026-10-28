package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.PricingTiers
import com.example.model.CardTemplate
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.utils.NetworkUtils

@Composable
fun ProUpgradeDialog(
    targetTemplate: CardTemplate? = null,
    onPurchaseTier: (String) -> Unit,
    onWatchRewardedAd: (() -> Unit)? = null,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTierId by remember { mutableStateOf(PricingTiers.ULTIMATE_VIP.id) }
    var showNoNetworkDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("pro_upgrade_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HolidayGold.copy(alpha = 0.2f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "👑", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXCLUSIVE COLLECTION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HolidayGold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hero Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HolidayGold, Color(0xFFFFA000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unlock Holiday Wishes Pro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (targetTemplate != null)
                        "Get full access to '${targetTemplate.title}' and all luxury cards!"
                    else
                        "Bring the magic alive with exclusive holiday designs and unlimited AI wishes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Network Status Indicator in Upgrade Dialog
                val isConnected = NetworkUtils.isNetworkAvailable(context)
                val connectionType = NetworkUtils.getNetworkTypeName(context)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isConnected) HolidayPineGreen.copy(alpha = 0.08f) else HolidayCrimson.copy(alpha = 0.08f),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) HolidayPineGreen.copy(alpha = 0.35f) else HolidayCrimson.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_network_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isConnected) HolidayPineGreen else HolidayCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "Network Ready ($connectionType)" else "⚠️ Offline: Wi-Fi or Mobile Data Required",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) HolidayPineGreen else HolidayCrimson
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Plan 1: Monthly ($9.99/mo)
                val monthlyTier = PricingTiers.PRO_MONTHLY
                TierSelectionCard(
                    tierTitle = monthlyTier.title,
                    price = monthlyTier.priceDisplay,
                    period = "Billed Monthly • Cancel Anytime",
                    badge = monthlyTier.badge,
                    badgeColor = HolidayPineGreen,
                    isSelected = selectedTierId == monthlyTier.id,
                    onClick = { selectedTierId = monthlyTier.id },
                    features = listOf(
                        "🪙 300 AI Credits included each month",
                        "Unlock 1,000+ Pro Season, Xmas & New Year cards",
                        "Unlock 1,000+ Pro Wishes across all categories",
                        "Remove all watermarks on cards & wishes",
                        "100% Ad-Free Experience Across App"
                    ),
                    modifier = Modifier.testTag("tier_monthly_999")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Plan 2: Yearly ($79.00/yr)
                val yearlyTier = PricingTiers.PRO_YEARLY
                TierSelectionCard(
                    tierTitle = yearlyTier.title,
                    price = yearlyTier.priceDisplay,
                    period = "Billed Annually ($6.58/mo)",
                    badge = "⭐ SAVE 34% • UNLIMITED",
                    badgeColor = HolidayGold,
                    isSelected = selectedTierId == yearlyTier.id,
                    onClick = { selectedTierId = yearlyTier.id },
                    features = listOf(
                        "✨ UNLIMITED AI Credits for 1 full year",
                        "Save 34% compared to monthly billing",
                        "Unlock all 1,000+ Pro Cards & Wishes",
                        "Full HD Card Download & WhatsApp Auto-Schedule"
                    ),
                    modifier = Modifier.testTag("tier_yearly_79")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action Button (Purchases via Google Play Billing)
                val isYearlySelected = selectedTierId == yearlyTier.id
                Button(
                    onClick = {
                        val isNetAvailable = NetworkUtils.isNetworkAvailable(context)
                        if (!isNetAvailable) {
                            showNoNetworkDialog = true
                        } else {
                            onPurchaseTier(selectedTierId)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYearlySelected) HolidayGold else HolidayCrimson
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("purchase_pro_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isYearlySelected) Color(0xFF332000) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isYearlySelected)
                            "Subscribe Yearly ($79.00/yr • Save 34%)"
                        else
                            "Subscribe Monthly ($9.99/mo)",
                        fontWeight = FontWeight.Bold,
                        color = if (isYearlySelected) Color(0xFF332000) else Color.White,
                        fontSize = 15.sp
                    )
                }

                // Option 3: Watch Rewarded Ad (if target template is provided)
                if (targetTemplate != null && onWatchRewardedAd != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onWatchRewardedAd()
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, HolidayPineGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("watch_rewarded_ad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = HolidayPineGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watch Short Video to Unlock This Card Free",
                            color = HolidayPineGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Restore Purchases & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val isNetAvailable = NetworkUtils.isNetworkAvailable(context)
                            if (!isNetAvailable) {
                                showNoNetworkDialog = true
                            } else {
                                onRestorePurchases()
                            }
                        },
                        modifier = Modifier.testTag("restore_purchases_button")
                    ) {
                        Text(
                            text = "Restore Purchases",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Secure Google Play Billing",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showNoNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showNoNetworkDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "No Internet",
                    tint = HolidayCrimson,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Internet Connection Required",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Please connect to Wi-Fi or turn on mobile cellular data to proceed with Google Play payment.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            showNoNetworkDialog = false
                            Toast.makeText(context, "Connected to network!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Still offline. Please connect to Wi-Fi or mobile data.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HolidayCrimson)
                ) {
                    Text("Check Connection")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoNetworkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TierSelectionCard(
    tierTitle: String,
    price: String,
    period: String,
    badge: String?,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    features: List<String>,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) HolidayGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (isSelected) HolidayGold.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Selection + Title + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) HolidayGold else Color.Transparent,
                        border = BorderStroke(1.5.dp, if (isSelected) HolidayGold else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.size(20.dp)
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF332000),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = tierTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Price prominent on the right, guaranteed visible
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) HolidayCrimson else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = period,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (badge != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Features list below header (full width)
            Column(modifier = Modifier.fillMaxWidth()) {
                features.take(3).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 1.5.dp)
                    ) {
                        Text(text = "✓", color = HolidayPineGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
