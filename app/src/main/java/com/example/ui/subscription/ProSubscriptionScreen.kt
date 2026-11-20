package com.example.ui.subscription

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import android.widget.Toast
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.utils.NetworkUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.repository.PricingTiers
import com.example.data.subscription.SubscriptionManager
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSubscriptionScreen(
    onDismiss: () -> Unit = {},
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isPro by SubscriptionManager.isPro.collectAsState()
    val activeTier by SubscriptionManager.activeTier.collectAsState()
    val isLoading by SubscriptionManager.isLoading.collectAsState()

    var showNoNetworkDialog by remember { mutableStateOf(false) }

    // ONLY 3 Profitable Plans: $2.90/mo (150 cr), $5.90/mo (400 cr), $35.00/yr (350 cr/mo = 4,200/yr)
    val starterTier = PricingTiers.STARTER_MONTHLY
    val plusTier = PricingTiers.PLUS_MONTHLY
    val yearlyTier = PricingTiers.ANNUAL_VIP
    var selectedReferenceTierId by remember { mutableStateOf(plusTier.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Holiday Wishes Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPro) "Active: $activeTier • Watermark Removed" else "Monthly & Yearly Pro Plans",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_subscription_screen")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                showNoNetworkDialog = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚠️ No internet connection. Please connect to Wi-Fi or mobile data.")
                                }
                                return@TextButton
                            }
                            SubscriptionManager.restorePurchases { active ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (active) "Purchases restored! Active Pro access confirmed."
                                        else "No previous purchases found for this account."
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restore", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(HolidayGold, Color(0xFFFFA000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unlock Holiday Wishes Pro",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Unlock 1,000+ luxury cards & wishes, remove all watermarks, download in HD, go 100% ad-free, and get generous monthly Gemini AI credits.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Payment Network Connectivity Status Banner
            item {
                val isConnected = NetworkUtils.isNetworkAvailable(context)
                val connectionType = NetworkUtils.getNetworkTypeName(context)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isConnected) HolidayPineGreen.copy(alpha = 0.1f) else HolidayCrimson.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) HolidayPineGreen.copy(alpha = 0.4f) else HolidayCrimson.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_network_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isConnected) HolidayPineGreen else HolidayCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isConnected) "Network Ready ($connectionType)" else "No Internet Connection Detected",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) HolidayPineGreen else HolidayCrimson
                            )
                            Text(
                                text = if (isConnected)
                                    "Connected to $connectionType. Ready to process secure Google Play billing."
                                else
                                    "Connect to Wi-Fi or mobile data before upgrading via Google Play.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Price Section: ONLY 3 Plans ($2.90/mo, $5.90/mo, $35.00/yr)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Plan 1: Starter Monthly ($2.90/mo — 150 AI Credits/mo)
                    ReferenceTierCard(
                        tierTitle = starterTier.title,
                        price = starterTier.priceDisplay,
                        period = "Billed Monthly • 150 Credits/mo",
                        badge = starterTier.badge,
                        badgeColor = HolidayPineGreen,
                        isSelected = selectedReferenceTierId == starterTier.id,
                        onClick = { selectedReferenceTierId = starterTier.id },
                        features = starterTier.features,
                        modifier = Modifier.testTag("tier_starter_monthly")
                    )

                    // Plan 2: Plus Monthly ($5.90/mo — 400 AI Credits/mo) [Recommended]
                    ReferenceTierCard(
                        tierTitle = plusTier.title,
                        price = plusTier.priceDisplay,
                        period = "Billed Monthly • 400 Credits/mo",
                        badge = plusTier.badge,
                        badgeColor = HolidayCrimson,
                        isSelected = selectedReferenceTierId == plusTier.id,
                        onClick = { selectedReferenceTierId = plusTier.id },
                        features = plusTier.features,
                        modifier = Modifier.testTag("tier_plus_monthly")
                    )

                    // Plan 3: Annual VIP Pass ($35.00/yr — 350 AI Credits/mo = 4,200/yr)
                    ReferenceTierCard(
                        tierTitle = yearlyTier.title,
                        price = yearlyTier.priceDisplay,
                        period = "Billed Annually (~$2.92/mo)",
                        badge = yearlyTier.badge,
                        badgeColor = HolidayGold,
                        isSelected = selectedReferenceTierId == yearlyTier.id,
                        onClick = { selectedReferenceTierId = yearlyTier.id },
                        features = yearlyTier.features,
                        modifier = Modifier.testTag("tier_yearly_35")
                    )
                }
            }

            // Button Section: Selected Tier Purchase Button, Restore, & Security Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val selectedTier = when (selectedReferenceTierId) {
                        yearlyTier.id -> yearlyTier
                        plusTier.id -> plusTier
                        else -> starterTier
                    }
                    val isYearlySelected = selectedTier.id == yearlyTier.id

                    // Primary Action Button
                    Button(
                        onClick = {
                            val isConnected = NetworkUtils.isNetworkAvailable(context)
                            if (!isConnected) {
                                showNoNetworkDialog = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚠️ No internet connection. Please connect to Wi-Fi or mobile data.")
                                }
                                return@Button
                            }
                            if (activity != null) {
                                SubscriptionManager.purchaseByProductId(
                                    activity = activity,
                                    productId = selectedTier.id,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("🎉 Welcome to ${selectedTier.title}! Pro Activated.")
                                        }
                                    },
                                    onError = { err ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isYearlySelected) HolidayGold else HolidayCrimson
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("purchase_pro_tier_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isYearlySelected) Color(0xFF332000) else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val buttonLabel = when (selectedTier.id) {
                                yearlyTier.id -> "Subscribe Yearly ($35.00/yr • Save 51%)"
                                plusTier.id -> "Subscribe Plus ($5.90/mo • 400 Cr)"
                                else -> "Subscribe Starter ($2.90/mo • 150 Cr)"
                            }
                            Text(
                                text = buttonLabel,
                                fontWeight = FontWeight.Bold,
                                color = if (isYearlySelected) Color(0xFF332000) else Color.White,
                                fontSize = 15.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Restore Purchases & Billing Label Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val isConnected = NetworkUtils.isNetworkAvailable(context)
                                if (!isConnected) {
                                    showNoNetworkDialog = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("⚠️ No internet connection. Connect to Wi-Fi or mobile data to restore purchases.")
                                    }
                                    return@TextButton
                                }
                                SubscriptionManager.restorePurchases { active ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (active) "Purchases restored! Active Pro access confirmed."
                                            else "No existing purchases found."
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.testTag("restore_purchases_button")
                        ) {
                            Text(
                                text = "Restore Purchases",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Secure Google Play Billing",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Play Security & Policy Footnote
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HolidayPineGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cancel anytime in Google Play Store • Auto-renews until cancelled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Payments are processed securely via your Google Play account.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
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
                    contentDescription = "No Network",
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "An active Wi-Fi or mobile cellular data connection is required before Google Play Billing can be launched.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Please connect to Wi-Fi or enable cellular mobile data on your device, then try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (NetworkUtils.isNetworkAvailable(context)) {
                            showNoNetworkDialog = false
                            Toast.makeText(context, "Network connected! Ready to upgrade.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Still offline. Please check your Wi-Fi or mobile data.", Toast.LENGTH_SHORT).show()
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
private fun ReferenceTierCard(
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Radio Circle + Title + Price
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
                        modifier = Modifier.size(22.dp)
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

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = tierTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Price display prominent on the right
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

            // Optional Badge Row below title for spacious, beautiful rendering (prevents squishing)
            if (badge != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Features list below header
            Column(modifier = Modifier.fillMaxWidth()) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = HolidayPineGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
