package com.salahguard.app.domain.repository

import com.salahguard.app.domain.model.DailyIntention
import kotlinx.coroutines.flow.Flow

interface DailyIntentionRepository {
    suspend fun getDailyIntention(): DailyIntention
    suspend fun getAllIntentions(): List<DailyIntention>
    suspend fun toggleFavorite(id: Int)
    suspend fun markAsCompleted(id: Int)
    fun getFavoriteIntentions(): Flow<List<DailyIntention>>
}
