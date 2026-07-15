package com.salahguard.app.domain.model

data class Mosque(
    val name: String,
    val distance: Double, // in km
    val travelTimeMinutes: Int,
    val latitude: Double,
    val longitude: Double
)
