package com.salahguard.app.data.repository

import com.salahguard.app.data.local.dao.PrayerDao
import com.salahguard.app.data.local.entity.PrayerEntity
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.PrayerRepository
import com.salahguard.app.domain.service.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class PrayerRepositoryImpl @Inject constructor(
    private val dao: PrayerDao,
    private val calculator: PrayerTimeCalculator
) : PrayerRepository {

    override fun getPrayersForDate(date: LocalDate): Flow<List<Prayer>> =
        dao.getPrayersForDate(date.toString()).map { entities ->
            val existing = entities.associateBy { it.name }
            
            PrayerName.entries.map { name ->
                existing[name.name]?.toDomain() ?: createDefaultPrayer(name, date)
            }
        }

    override fun getPrayersForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Prayer>> =
        dao.getPrayersForRange(startDate.toString(), endDate.toString()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun syncPrayerTimes(date: LocalDate, latitude: Double, longitude: Double): Boolean {
        val prayers = calculator.calculatePrayers(date, latitude, longitude)
        val existingEntities = dao.getPrayersForDateOnce(date.toString()).associateBy { it.name }

        var changed = false
        prayers.forEach { prayer ->
            val existing = existingEntities[prayer.name.name]
            val newScheduledTime = prayer.scheduledTime.toString()
            
            if (existing == null || existing.scheduledTime != newScheduledTime) {
                val entity = PrayerEntity(
                    name = prayer.name.name,
                    date = date.toString(),
                    scheduledTime = newScheduledTime,
                    status = existing?.status ?: PrayerStatus.PENDING.name,
                    completedAt = existing?.completedAt
                )
                dao.upsert(entity)
                changed = true
            }
        }
        return changed
    }

    private fun createDefaultPrayer(name: PrayerName, date: LocalDate): Prayer {
        val time = when (name) {
            PrayerName.FAJR -> LocalTime.of(5, 30)
            PrayerName.SUNRISE -> LocalTime.of(6, 45)
            PrayerName.DHUHR -> LocalTime.of(12, 15)
            PrayerName.ASR -> LocalTime.of(15, 45)
            PrayerName.MAGHRIB -> LocalTime.of(18, 20)
            PrayerName.ISHA -> LocalTime.of(19, 45)
        }
        return Prayer(name, date, time, PrayerStatus.PENDING)
    }

    override suspend fun markCompleted(prayer: Prayer) {
        dao.upsert(
            prayer.copy(
                status = PrayerStatus.COMPLETED,
                completedAt = LocalTime.now()
            ).toEntity()
        )
    }

    // Intentionally named "markMissed", not "markFailed" -
    // the vision doc is explicit that the app must never shame the user.
    // The UI layer decides what encouraging copy to show alongside this state.
    override suspend fun markMissed(prayer: Prayer) {
        dao.upsert(prayer.copy(status = PrayerStatus.MISSED).toEntity())
    }

    override suspend fun markRecovered(prayer: Prayer) {
        dao.upsert(
            prayer.copy(
                status = PrayerStatus.RECOVERED,
                completedAt = LocalTime.now()
            ).toEntity()
        )
    }

    override suspend fun markPending(prayer: Prayer) {
        dao.upsert(
            prayer.copy(
                status = PrayerStatus.PENDING,
                completedAt = null
            ).toEntity()
        )
    }
}

private fun PrayerEntity.toDomain() = Prayer(
    name = PrayerName.valueOf(name),
    date = LocalDate.parse(date),
    scheduledTime = LocalTime.parse(scheduledTime),
    status = PrayerStatus.valueOf(status),
    completedAt = completedAt?.let { LocalTime.parse(it) }
)

private fun Prayer.toEntity() = PrayerEntity(
    name = name.name,
    date = date.toString(),
    scheduledTime = scheduledTime.toString(),
    status = status.name,
    completedAt = completedAt?.toString()
)
