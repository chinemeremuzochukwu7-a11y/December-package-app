package com.example.data.repository

import com.example.model.AiTone
import com.example.model.AiWishLength
import com.example.model.AiWishRequest
import com.example.model.AiWishResponse
import com.example.model.CardOccasion
import com.example.model.WishCategory

/**
 * Interface defining the AI Wish and Card generation service.
 */
interface AiWishRepository {
    suspend fun generateWish(request: AiWishRequest): Result<AiWishResponse>

    suspend fun generateCardGreeting(
        occasion: CardOccasion,
        recipientName: String = "",
        senderName: String = "",
        tone: AiTone = AiTone.WARM,
        length: AiWishLength = AiWishLength.MEDIUM,
        customPrompt: String = "",
        seed: Long = System.currentTimeMillis()
    ): String

    suspend fun regenerateWishText(
        category: WishCategory,
        currentText: String = "",
        seed: Long = System.currentTimeMillis()
    ): String
}
