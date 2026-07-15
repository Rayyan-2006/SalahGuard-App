package com.salahguard.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class PrayerName { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }

enum class PrayerStatus { PENDING, COMPLETED, MISSED, RECOVERED }

/**
 * Pure domain model - no Room/Android annotations.
 * The data layer maps its Room entity to/from this class, so the domain
 * and presentation layers never depend on persistence details.
 */
data class Prayer(
    val name: PrayerName,
    val date: LocalDate,
    val scheduledTime: LocalTime,
    val status: PrayerStatus,
    val completedAt: LocalTime? = null
)
