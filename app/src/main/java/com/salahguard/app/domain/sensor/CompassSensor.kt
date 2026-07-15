package com.salahguard.app.domain.sensor

import kotlinx.coroutines.flow.Flow

interface CompassSensor {
    fun getAzimuth(): Flow<Float>
}
