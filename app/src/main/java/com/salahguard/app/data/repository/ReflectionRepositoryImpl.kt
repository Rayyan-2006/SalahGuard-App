package com.salahguard.app.data.repository

import com.salahguard.app.data.local.dao.ReflectionDao
import com.salahguard.app.data.local.entity.ReflectionEntity
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.domain.repository.ReflectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class ReflectionRepositoryImpl @Inject constructor(
    private val dao: ReflectionDao
) : ReflectionRepository {

    override fun getAllReflections(): Flow<List<Reflection>> =
        dao.getAllReflections().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveReflection(reflection: Reflection) {
        dao.insertReflection(reflection.toEntity())
    }

    override suspend fun deleteReflection(id: Long) {
        dao.deleteReflection(id)
    }
}

private fun ReflectionEntity.toDomain() = Reflection(
    id = id,
    prayerName = prayerName,
    date = LocalDate.parse(date),
    time = LocalTime.parse(time),
    reflectionText = reflectionText,
    mood = mood
)

private fun Reflection.toEntity() = ReflectionEntity(
    id = id,
    prayerName = prayerName,
    date = date.toString(),
    time = time.toString(),
    reflectionText = reflectionText,
    mood = mood
)
