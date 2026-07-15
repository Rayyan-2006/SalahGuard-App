package com.salahguard.app.domain.repository

import com.salahguard.app.domain.model.Reflection
import kotlinx.coroutines.flow.Flow

interface ReflectionRepository {
    fun getAllReflections(): Flow<List<Reflection>>
    suspend fun saveReflection(reflection: Reflection)
    suspend fun deleteReflection(id: Long)
}
