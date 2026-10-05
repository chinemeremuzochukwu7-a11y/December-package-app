package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ads.AdManager
import com.example.data.repository.MonetizationManager
import com.example.data.subscription.SubscriptionManager
import com.example.model.Wish
import com.example.ui.theme.HolidayCrimson
import com.example.ui.theme.HolidayGold
import com.example.ui.theme.HolidayPineGreen
import com.example.utils.ClipboardHelper
import com.example.utils.ShareHelper

@Composable
fun WishCard(
    wish: Wish,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isProUser by SubscriptionManager.isPro.collectAsState()
    val monetizationManager = remember { MonetizationManager.getInstance(context) }
    val unlockedWishes by monetizationManager.unlockedWishIds.collectAsState()
    val isUnlocked = !wish.isPro || isProUser || unlockedWishes.contains(wish.id)

    val favoriteColor by animateColorAsState(
        targetValue = if (isFavorite) HolidayCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favoriteColor"
    )

    val cardBorder = if (wish.isPro) {
        BorderStroke(1.5.dp, HolidayGold.copy(alpha = if (isUnlocked) 0.8f else 0.5f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wish_card_${wish.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (wish.isPro) {
                HolidayGold.copy(alpha = 0.04f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Category Badge & Occasion / Pro Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HolidayCrimson)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = wish.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (wish.isPro) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HolidayGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isUnlocked) "👑 PRO Unlocked" else "👑 PRO VIP",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HolidayGold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (wish.isPopular) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HolidayGold.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "★ Popular",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HolidayGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wish Text Body or Pro Locked Banner
            if (isUnlocked) {
                Text(
                    text = wish.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("wish_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions: Copy, Share, Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy Button
                    IconButton(
                        onClick = {
                            ClipboardHelper.copyText(context, wish.text)
                        },
                        modifier = Modifier.testTag("copy_wish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy wish",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Share Button
                    IconButton(
                        onClick = {
                            ShareHelper.shareWish(context, wish.text, "Holiday Wish: ${wish.category.displayName}")
                        },
                        modifier = Modifier.testTag("share_wish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share wish",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("favorite_wish_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = favoriteColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                // Locked Pro State: Preview snippet + Watch Ad to Unlock
                Text(
                    text = "${wish.text.take(65)}...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.testTag("locked_wish_preview")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HolidayGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, HolidayGold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = HolidayGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pro Exclusive Wish",
                                fontWeight = FontWeight.Bold,
                                color = HolidayGold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Surface(
                            color = HolidayCrimson,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                if (activity != null) {
                                    AdManager.showRewardedAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            monetizationManager.unlockWishViaRewardedAd(wish.id)
                                            Toast.makeText(context, "🎉 Pro Wish Unlocked!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDismiss = {}
                                    )
                                } else {
                                    monetizationManager.unlockWishViaRewardedAd(wish.id)
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Watch Ad to Unlock",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
