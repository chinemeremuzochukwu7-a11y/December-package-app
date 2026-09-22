package com.example.data.repository

import com.example.model.AiWishRequest
import com.example.model.AiWishResponse

/**
 * Interface defining the AI Wish generation service.
 * In production, this can be implemented by a secure backend service (e.g., Firebase Functions, Cloud Run,
 * or server-side Gemini endpoint) without changing the client UI or ViewModel architecture.
 */
interface AiWishRepository {
    suspend fun generateWish(request: AiWishRequest): Result<AiWishResponse>
}
