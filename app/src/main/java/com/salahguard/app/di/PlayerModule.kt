package com.salahguard.app.di

import com.salahguard.app.data.service.Media3AudioPlayer
import com.salahguard.app.domain.service.AudioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(
        impl: Media3AudioPlayer
    ): AudioPlayer
}
