package com.salahguard.app.presentation.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.DefaultReciter
import com.salahguard.app.domain.model.Surah
import com.salahguard.app.domain.repository.QuranAudioRepository
import com.salahguard.app.domain.repository.QuranRepository
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.service.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val audioRepository: QuranAudioRepository,
    private val player: AudioPlayer,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePlayerState()
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            player.state.collect { playerState ->
                _uiState.update { 
                    it.copy(
                        playerState = playerState,
                        playingAyahId = if (playerState.currentIndex != -1 && it.isSurahMode) {
                            it.verses.getOrNull(playerState.currentIndex)?.number
                        } else it.playingAyahId
                    ) 
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                repository.getSurahs(),
                userPreferencesRepository.getLastOpenedSurahId()
            ) { surahs, lastId ->
                surahs to lastId
            }.collect { (surahs, lastId) ->
                val lastOpened = surahs.find { it.id == lastId }
                _uiState.update { 
                    it.copy(
                        surahs = surahs,
                        filteredSurahs = filterSurahs(surahs, it.searchQuery),
                        lastOpenedSurah = lastOpened,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredSurahs = filterSurahs(it.surahs, query)
            ) 
        }
    }

    private fun filterSurahs(surahs: List<Surah>, query: String): List<Surah> {
        if (query.isBlank()) return surahs
        return surahs.filter { 
            it.englishName.contains(query, ignoreCase = true) || 
            it.name.contains(query) ||
            it.id.toString() == query
        }
    }

    fun selectSurah(surah: Surah) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSurah = surah, isLoading = true, isReadingMode = true) }
            userPreferencesRepository.setLastOpenedSurahId(surah.id)
            repository.getVersesForSurah(surah.id).collect { verses ->
                _uiState.update { it.copy(verses = verses, isLoading = false) }
            }
        }
    }

    fun exitReadingMode() {
        player.stop()
        _uiState.update { it.copy(isReadingMode = false, selectedSurah = null, verses = emptyList(), playingAyahId = null) }
    }

    fun playAyah(ayah: Ayah) {
        val url = audioRepository.getAudioUrl(DefaultReciter, ayah.surahId, ayah.number)
        _uiState.update { it.copy(isSurahMode = false, playingAyahId = ayah.number) }
        player.play(url)
    }

    fun playSurah() {
        val selectedSurah = _uiState.value.selectedSurah ?: return
        val urls = audioRepository.getAudioUrlsForSurah(DefaultReciter, _uiState.value.verses)
        _uiState.update { it.copy(isSurahMode = true) }
        player.playList(urls)
    }

    fun togglePlayPause() {
        if (_uiState.value.playerState.isPlaying) {
            player.pause()
        } else {
            player.resume()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun nextAyah() {
        player.next()
    }

    fun previousAyah() {
        player.previous()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
