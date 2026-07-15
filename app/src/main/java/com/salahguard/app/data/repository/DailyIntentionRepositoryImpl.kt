package com.salahguard.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salahguard.app.domain.model.DailyIntention
import com.salahguard.app.domain.repository.DailyIntentionRepository
import com.salahguard.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyIntentionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : DailyIntentionRepository {

    private val gson = Gson()
    private val intentions: List<DailyIntention> by lazy {
        val json = context.assets.open("daily_intentions.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<DailyIntention>>() {}.type
        gson.fromJson(json, type)
    }

    override suspend fun getDailyIntention(): DailyIntention {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear
        val index = (dayOfYear - 1) % intentions.size
        val baseIntention = intentions[index]

        val favorites = userPreferencesRepository.getFavoriteIntentionIds().first()
        val completedDate = userPreferencesRepository.getDailyIntentionCompletedDate().first()
        
        return baseIntention.copy(
            isFavorite = favorites.contains(baseIntention.id),
            isCompleted = completedDate == today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }

    override suspend fun getAllIntentions(): List<DailyIntention> {
        val favorites = userPreferencesRepository.getFavoriteIntentionIds().first()
        val completedDate = userPreferencesRepository.getDailyIntentionCompletedDate().first()
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        return intentions.map { 
            it.copy(
                isFavorite = favorites.contains(it.id),
                isCompleted = it.id == getDailyIntention().id && completedDate == todayStr
            )
        }
    }

    override suspend fun toggleFavorite(id: Int) {
        userPreferencesRepository.toggleFavoriteIntention(id)
    }

    override suspend fun markAsCompleted(id: Int) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        userPreferencesRepository.setDailyIntentionCompletedDate(today)
    }

    override fun getFavoriteIntentions(): Flow<List<DailyIntention>> {
        return userPreferencesRepository.getFavoriteIntentionIds().map { favorites ->
            intentions.filter { favorites.contains(it.id) }.map { it.copy(isFavorite = true) }
        }
    }
}
