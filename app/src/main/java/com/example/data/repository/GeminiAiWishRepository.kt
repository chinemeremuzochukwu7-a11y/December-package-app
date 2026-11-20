package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.model.AiTone
import com.example.model.AiWishLength
import com.example.model.AiWishRequest
import com.example.model.AiWishResponse
import com.example.model.CardOccasion
import com.example.model.WishCategory
import com.example.utils.NetworkUtils
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.UUID

/**
 * Production Gemini API implementation of [AiWishRepository] powered by Firebase AI Logic.
 * Uses cost-efficient Gemini 3.x models with strict maxOutputTokens caps to protect unit margins.
 */
class GeminiAiWishRepository(context: Context) : AiWishRepository {

    private val appContext = context.applicationContext

    private val primaryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.85f
                maxOutputTokens = 450
            }
        )
    }

    private val liteModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                temperature = 0.8f
                maxOutputTokens = 180
            }
        )
    }

    override suspend fun generateWish(request: AiWishRequest): Result<AiWishResponse> {
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            return Result.failure(IllegalStateException("Internet connection required for AI wish generation."))
        }

        val recipientLabel = if (request.recipientName.isNotBlank()) {
            "${request.recipient.displayName} named ${request.recipientName.trim()}"
        } else {
            request.recipient.displayName
        }

        val prompt = buildString {
            appendLine("Write an authentic, emotionally resonant ${request.occasion.displayName} wish in ${request.language.displayName}.")
            appendLine("Recipient: $recipientLabel.")
            appendLine("Tone: ${request.tone.displayName}.")
            appendLine("Target length: ${request.length.displayName} (${request.length.wordRange}).")
            if (request.personalDetails.isNotBlank()) {
                appendLine("Personal details to weave in naturally: \"${request.personalDetails.trim()}\"")
            }
            appendLine("Rules: Return ONLY the ready-to-send greeting text (salutation, body, and sign-off). No markdown headings, no quotes around the whole message, no meta commentary.")
        }

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                primaryModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("Gemini returned an empty wish. Please try again."))
            } else {
                Result.success(
                    AiWishResponse(
                        id = UUID.randomUUID().toString(),
                        text = text,
                        occasion = request.occasion,
                        recipient = request.recipient,
                        recipientName = request.recipientName.trim(),
                        tone = request.tone,
                        language = request.language,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini wish generation timed out", e)
            Result.failure(IllegalStateException("AI generation timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini wish generation failed", e)
            Result.failure(e)
        }
    }

    override suspend fun generateCardGreeting(
        occasion: CardOccasion,
        recipientName: String,
        senderName: String,
        tone: AiTone,
        length: AiWishLength,
        customPrompt: String,
        seed: Long
    ): String {
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            throw IllegalStateException("Internet connection required for AI card generation.")
        }

        val occasionLabel = when (occasion) {
            CardOccasion.CHRISTMAS -> "Christmas"
            CardOccasion.NEW_YEAR -> "New Year"
            CardOccasion.HOLIDAY_SEASON -> "Holiday Season"
        }

        val prompt = buildString {
            appendLine("Write a heartfelt $occasionLabel greeting card message.")
            if (recipientName.isNotBlank()) appendLine("Address to: ${recipientName.trim()}.")
            if (senderName.isNotBlank()) appendLine("Sign off from: ${senderName.trim()}.")
            appendLine("Tone: ${tone.displayName}.")
            appendLine("Length: ${length.displayName} (${length.wordRange}). Fits cleanly inside a greeting card layout.")
            if (customPrompt.isNotBlank()) {
                appendLine("Special note to include: \"${customPrompt.trim()}\"")
            }
            appendLine("Rules: Return ONLY the greeting card text. No markdown, no hashtags unless tasteful, no preamble.")
        }

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                primaryModel.generateContent(prompt)
            }
            response.text?.trim()?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Gemini returned an empty greeting.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini card greeting timed out", e)
            throw IllegalStateException("AI generation timed out. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini card greeting failed", e)
            throw e
        }
    }

    override suspend fun regenerateWishText(
        category: WishCategory,
        currentText: String,
        seed: Long
    ): String {
        if (!NetworkUtils.isNetworkAvailable(appContext)) {
            throw IllegalStateException("Internet connection required to regenerate text with AI.")
        }

        val prompt = buildString {
            appendLine("Rewrite and refresh this ${category.displayName} holiday wish so it feels original, warm, and ready to share:")
            if (currentText.isNotBlank()) {
                appendLine("Original: \"${currentText.trim().take(400)}\"")
            }
            appendLine("Keep it between 25 and 60 words. Return ONLY the rewritten wish text with no quotes or explanation.")
        }

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                liteModel.generateContent(prompt)
            }
            response.text?.trim()?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Gemini returned an empty rewrite.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini wish regeneration timed out", e)
            throw IllegalStateException("AI regeneration timed out. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini wish regeneration failed", e)
            throw e
        }
    }

    private companion object {
        const val TAG = "GeminiAiWishRepo"
        const val TIMEOUT_MS = 45_000L
    }
}
