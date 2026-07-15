package com.salahguard.app.domain.model

data class DailyIntention(
    val id: Int,
    val text: String,
    val isFavorite: Boolean = false,
    val isCompleted: Boolean = false
)
