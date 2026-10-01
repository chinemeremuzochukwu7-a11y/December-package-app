package com.example.ui.subscription

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.subscription.SubscriptionManager
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter
import kotlinx.coroutines.launch

/**
 * Screen presenting Holiday Wishes Pro upgrade with RevenueCat:
 * - Direct RevenueCat Paywall integration
 * - Built-in Holiday Wishes Custom Paywall fallback (with Yearly / Monthly selection)
 * - Support for RevenueCat Customer Center (subscription management & help)
 * - Instant restore purchases & real-time entitlement validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSubscriptionScreen(
    onDismiss: () -> Unit = {},
    initialTab: Int = 0 // 0 = Paywall, 1 = Customer Center
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isPro by SubscriptionManager.isPro.collectAsState()
    val offerings by SubscriptionManager.offerings.collectAsState()
    val isLoading by SubscriptionManager.isLoading.collectAsState()
    val errorMessage by SubscriptionManager.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(if (isPro) 1 else initialTab) }
    var useNativeRevenueCatUi by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPro) "Holiday Wishes Pro Active" else "Holiday Wishes Pro",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_paywall_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            SubscriptionManager.refreshCustomerInfo { proActive ->
                                scope.launch {
                                    val msg = if (proActive) "Pro status verified active!" else "Customer info refreshed."
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh status")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab switcher: Paywall vs Customer Center
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upgrade / Plans")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Customer Center")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                if (useNativeRevenueCatUi) {
                    // RevenueCat Hosted Paywall UI Composable
                    Box(modifier = Modifier.fillMaxSize()) {
                        val paywallOptions = remember {
                            PaywallOptions.Builder(dismissRequest = { onDismiss() })
                                .build()
                        }
                        Paywall(options = paywallOptions)
                    }
                } else {
                    // Holiday Wishes Branded Custom Paywall
                    HolidayPaywallContent(
                        isPro = isPro,
                        isLoading = isLoading,
                        offerings = offerings,
                        onSelectPackage = { pkg ->
                            if (activity != null) {
                                SubscriptionManager.purchase(
                                    activity = activity,
                                    packageToPurchase = pkg,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Welcome to Holiday Wishes Pro!")
                                        }
                                    },
                                    onError = { err ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Purchase notice: $err")
                                        }
                                    }
                                )
                            }
                        },
                        onRestore = {
                            SubscriptionManager.restore(
                                onSuccess = { active ->
                                    scope.launch {
                                        if (active) {
                                            snackbarHostState.showSnackbar("Purchases restored successfully! Pro unlocked.")
                                        } else {
                                            snackbarHostState.showSnackbar("No active Pro subscription found for this Google Play account.")
                                        }
                                    }
                                },
                                onError = { err ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Restore notice: $err")
                                    }
                                }
                            )
                        },
                        onToggleNativePaywall = { useNativeRevenueCatUi = true }
                    )
                }
            } else {
                // Customer Center Screen (RevenueCat Customer Support & Subscription Management)
                Box(modifier = Modifier.fillMaxSize()) {
                    CustomerCenter(
                        modifier = Modifier.fillMaxSize(),
                        onDismiss = { selectedTab = 0 }
                    )
                }
            }
        }
    }
}

/**
 * Holiday Wishes Branded Paywall Presentation with Yearly & Monthly packages
 */
@Composable
private fun HolidayPaywallContent(
    isPro: Boolean,
    isLoading: Boolean,
    offerings: com.revenuecat.purchases.Offerings?,
    onSelectPackage: (Package) -> Unit,
    onRestore: () -> Unit,
    onToggleNativePaywall: () -> Unit
) {
    val scrollState = rememberScrollState()

    val currentOffering = offerings?.current
    val monthlyPkg = SubscriptionManager.getMonthlyPackage()
    val yearlyPkg = SubscriptionManager.getYearlyPackage()

    var selectedTier by remember { mutableStateOf("yearly") } // "yearly" or "monthly"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Pro Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8B0000), // Festive Crimson
                                Color(0xFFC41E3A),
                                Color(0xFFD4AF37)  // Warm Gold
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPro) "YOU ARE PRO" else "HOLIDAY WISHES PRO",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPro)
                            "Your Pro entitlement (holiday_wishes_pro) is active! Enjoy all VIP cards and unlimited features."
                        else
                            "Create heartwarming memories with luxury cards, unlimited multilingual AI, and ad-free joy.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f)),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Features list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProFeatureRow(
                icon = Icons.Default.AutoAwesome,
                title = "Unlimited AI Wish Generations",
                subtitle = "Personalized wishes for family, coworkers, and clients with zero daily limits"
            )
            ProFeatureRow(
                icon = Icons.Default.FormatPaint,
                title = "Exclusive VIP Card Templates",
                subtitle = "Luxury gold foil, animated snow frames, and royal aesthetic borders"
            )
            ProFeatureRow(
                icon = Icons.Default.Download,
                title = "High-Res Export & Watermark-Free",
                subtitle = "Crisp 300 DPI exports suitable for photo printing and story sharing"
            )
            ProFeatureRow(
                icon = Icons.Default.Translate,
                title = "All 7 Holiday Languages",
                subtitle = "English, Spanish, French, German, Italian, Portuguese, and Polish"
            )
            ProFeatureRow(
                icon = Icons.Default.LockOpen,
                title = "100% Ad-Free Experience",
                subtitle = "Zero banners, zero interruptions during the festive season"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subscription Package Selector: Yearly vs Monthly
        if (!isPro) {
            Text(
                text = "Choose Your Plan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yearly Package Card
                val isYearlySelected = selectedTier == "yearly"
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isYearlySelected) 2.dp else 1.dp,
                            color = if (isYearlySelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedTier = "yearly" }
                        .testTag("tier_yearly_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isYearlySelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD4AF37),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "BEST VALUE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Yearly",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val yearlyPrice = yearlyPkg?.product?.price?.formatted ?: "$14.99"
                        Text(
                            text = yearlyPrice,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "per year",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                // Monthly Package Card
                val isMonthlySelected = selectedTier == "monthly"
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isMonthlySelected) 2.dp else 1.dp,
                            color = if (isMonthlySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedTier = "monthly" }
                        .testTag("tier_monthly_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMonthlySelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FLEXIBLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Text(
                            text = "Monthly",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val monthlyPrice = monthlyPkg?.product?.price?.formatted ?: "$2.99"
                        Text(
                            text = monthlyPrice,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "per month",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Purchase Button
            val activePkg = if (selectedTier == "yearly") yearlyPkg else monthlyPkg
            Button(
                onClick = {
                    if (activePkg != null) {
                        onSelectPackage(activePkg)
                    } else {
                        // Fallback notice if sandbox offering not synced
                        onRestore()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_pro_subscription_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC41E3A),
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTier == "yearly") "Unlock Yearly Pro" else "Unlock Monthly Pro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            // Already Pro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "All Pro Features Unlocked",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Entitlement: holiday_wishes_pro",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action links: Restore Purchases & RevenueCat Hosted Paywall
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = onRestore,
                enabled = !isLoading,
                modifier = Modifier.testTag("restore_purchases_button")
            ) {
                Text(
                    text = "Restore Purchases",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            TextButton(
                onClick = onToggleNativePaywall
            ) {
                Text(
                    text = "Hosted Paywall UI",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Subscriptions automatically renew through Google Play unless canceled at least 24 hours before the end of the current period. Manage anytime in Google Play Account Settings or the Customer Center.",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun ProFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
