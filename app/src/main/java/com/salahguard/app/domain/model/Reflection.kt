package com.salahguard.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Reflection(
    val id: Long = 0,
    val prayerName: String,
    val date: LocalDate,
    val time: LocalTime,
    val reflectionText: String,
    val mood: String?
)
