package com.salahguard.app.domain.service

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val state: StateFlow<PlayerState>
    
    fun play(url: String)
    fun playList(urls: List<String>)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun setPlaybackSpeed(speed: Float)
    fun release()
    fun next()
    fun previous()
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val currentIndex: Int = -1,
    val playbackSpeed: Float = 1.0f,
    val error: String? = null
)
