package com.salahguard.app.di

import com.salahguard.app.data.sensor.AndroidCompassSensor
import com.salahguard.app.domain.sensor.CompassSensor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {

    @Binds
    @Singleton
    abstract fun bindCompassSensor(
        impl: AndroidCompassSensor
    ): CompassSensor
}
