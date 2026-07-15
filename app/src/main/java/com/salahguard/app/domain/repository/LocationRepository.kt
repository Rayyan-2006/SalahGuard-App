package com.salahguard.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getCurrentLocation(): Flow<Pair<Double, Double>?>
    suspend fun hasLocationPermission(): Boolean
}
