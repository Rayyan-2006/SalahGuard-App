package com.salahguard.app.data.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.salahguard.app.domain.service.AudioPlayer
import com.salahguard.app.domain.service.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Media3AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer, Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(this@Media3AudioPlayer)
            }
        }
    }

    override fun play(url: String) {
        initializePlayer()
        exoPlayer?.let {
            it.setMediaItem(MediaItem.fromUri(url))
            it.prepare()
            it.play()
        }
    }

    override fun playList(urls: List<String>) {
        initializePlayer()
        exoPlayer?.let {
            it.setMediaItems(urls.map { url -> MediaItem.fromUri(url) })
            it.prepare()
            it.play()
        }
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun resume() {
        exoPlayer?.play()
    }

    override fun stop() {
        exoPlayer?.stop()
    }

    override fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _state.update { it.copy(playbackSpeed = speed) }
    }

    override fun next() {
        exoPlayer?.seekToNextMediaItem()
    }

    override fun previous() {
        exoPlayer?.seekToPreviousMediaItem()
    }

    override fun release() {
        stopProgressUpdate()
        exoPlayer?.release()
        exoPlayer = null
    }

    // Player.Listener methods
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _state.update { it.copy(isPlaying = isPlaying) }
        if (isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        _state.update { 
            it.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                duration = exoPlayer?.duration?.coerceAtLeast(0) ?: 0
            ) 
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        _state.update { it.copy(currentIndex = exoPlayer?.currentMediaItemIndex ?: -1) }
    }

    override fun onPlayerError(error: PlaybackException) {
        _state.update { it.copy(error = error.localizedMessage) }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _state.update { 
                    it.copy(
                        currentPosition = exoPlayer?.currentPosition ?: 0,
                        duration = exoPlayer?.duration?.coerceAtLeast(0) ?: 0
                    ) 
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
}
