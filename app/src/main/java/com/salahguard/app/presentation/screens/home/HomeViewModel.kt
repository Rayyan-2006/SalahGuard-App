package com.salahguard.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.DailyIntentionRepository
import com.salahguard.app.domain.repository.LocationRepository
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.service.HopeModeReminders
import com.salahguard.app.domain.usecase.GetDailyIntentionUseCase
import com.salahguard.app.domain.usecase.GetRecoveryStatsUseCase
import com.salahguard.app.domain.usecase.GetTodayPrayersUseCase
import com.salahguard.app.domain.usecase.GetVerseOfTheDayUseCase
import com.salahguard.app.domain.usecase.GetWeeklyProgressUseCase
import com.salahguard.app.domain.usecase.SyncPrayerTimesUseCase
import com.salahguard.app.domain.usecase.UpdatePrayerStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

private data class HomeData(
    val prayers: List<Prayer>,
    val dismissedDate: String?,
    val intentionDismissedDate: String?,
    val intentionCompletedDate: String?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayPrayersUseCase: GetTodayPrayersUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase,
    private val getRecoveryStatsUseCase: GetRecoveryStatsUseCase,
    private val syncPrayerTimesUseCase: SyncPrayerTimesUseCase,
    private val updatePrayerStatusUseCase: UpdatePrayerStatusUseCase,
    private val getDailyIntentionUseCase: GetDailyIntentionUseCase,
    private val getVerseOfTheDayUseCase: GetVerseOfTheDayUseCase,
    private val dailyIntentionRepository: DailyIntentionRepository,
    private val locationRepository: LocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var collectorsJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (!locationRepository.hasLocationPermission()) {
                _uiState.update { 
                    it.copy(
                        locationError = "Location permission is required for accurate prayer times.",
                        isLoading = false 
                    ) 
                }
                return@launch
            }

            // Observe location and sync prayer times
            locationRepository.getCurrentLocation().collect { location ->
                if (location != null) {
                    _uiState.update { it.copy(locationError = null) }
                    syncPrayerTimesUseCase(LocalDate.now(), location.first, location.second)
                } else {
                    _uiState.update { 
                        it.copy(
                            locationError = "Unable to fetch location. Please check if GPS is enabled.",
                            isLoading = false
                        )
                    }
                }
            }
        }

        if (collectorsJob != null) return
        collectorsJob = viewModelScope.launch {
            launch {
                // Observe today's prayers and dismissed state
                combine(
                    getTodayPrayersUseCase(),
                    userPreferencesRepository.getLastDismissedRecoveryDate(),
                    userPreferencesRepository.getDailyIntentionDismissedDate(),
                    userPreferencesRepository.getDailyIntentionCompletedDate()
                ) { prayers, dismissedDate, intentionDismissedDate, intentionCompletedDate ->
                    HomeData(prayers, dismissedDate, intentionDismissedDate, intentionCompletedDate)
                }.collect { data ->
                    updateNextPrayer(data.prayers)
                    checkMissedPrayers(data.prayers)
                    updateRecoveryState(data.prayers, data.dismissedDate)
                    loadDailyIntention(data.intentionDismissedDate)
                    loadVerseOfTheDay()
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

            launch {
                // Observe weekly progress
                getWeeklyProgressUseCase().collect { progress ->
                    _uiState.update { it.copy(weeklyProgress = progress) }
                }
            }

            launch {
                // Observe recovery stats
                getRecoveryStatsUseCase().collect { stats ->
                    _uiState.update { it.copy(recoveryStats = stats) }
                }
            }

            launch {
                // Periodically update next prayer to handle time transitions
                while (true) {
                    kotlinx.coroutines.delay(1000) // Every second for the countdown sync
                    updateNextPrayer(todayPrayers)
                    checkMissedPrayers(todayPrayers)
                }
            }
        }
    }

    private var todayPrayers: List<Prayer> = emptyList()

    private fun updateRecoveryState(prayers: List<Prayer>, dismissedDate: String?) {
        val today = LocalDate.now().toString()
        if (dismissedDate == today) {
            _uiState.update { it.copy(showRecoveryCard = false) }
            return
        }

        val missedPrayer = prayers.filter { it.name != PrayerName.SUNRISE }
            .lastOrNull { it.status == PrayerStatus.MISSED }
        
        val lastAttempted = prayers.filter { it.name != PrayerName.SUNRISE }
            .lastOrNull { it.status == PrayerStatus.COMPLETED || it.status == PrayerStatus.RECOVERED }

        // Show recovery card if the last non-pending prayer was missed,
        // but ONLY if no subsequent prayer has been completed/recovered today.
        val shouldShow = missedPrayer != null && (lastAttempted == null || lastAttempted.scheduledTime.isBefore(missedPrayer.scheduledTime))

        _uiState.update {
            it.copy(
                showRecoveryCard = shouldShow,
                recoveryMessage = if (shouldShow) HopeModeReminders.getRandomReminder() else ""
            )
        }
    }

    private fun getCompassionateMessage(prayerName: PrayerName?): String {
        return when (prayerName) {
            PrayerName.FAJR -> "The morning is a new beginning. Allah's mercy is greater than any missed moment."
            PrayerName.DHUHR -> "A pause in the day to reconnect. Don't worry about the past, let's focus on the next step."
            PrayerName.ASR -> "The afternoon brings another chance. Your intention to return is beloved to Allah."
            PrayerName.MAGHRIB -> "As the sun sets, so do our mistakes. A new prayer awaits your presence."
            PrayerName.ISHA -> "Rest well knowing you can start fresh. Tomorrow is a gift and another opportunity."
            else -> "Every moment is a chance to return to your path with hope and peace."
        }
    }

    fun dismissRecoveryCard() {
        viewModelScope.launch {
            userPreferencesRepository.setLastDismissedRecoveryDate(LocalDate.now().toString())
        }
    }

    fun recoverLastMissedPrayer() {
        viewModelScope.launch {
            val missedPrayer = todayPrayers.filter { it.name != PrayerName.SUNRISE }
                .lastOrNull { it.status == PrayerStatus.MISSED }
            
            missedPrayer?.let {
                updatePrayerStatusUseCase(it, PrayerStatus.RECOVERED)
                // Dismiss the card after recovery
                userPreferencesRepository.setLastDismissedRecoveryDate(LocalDate.now().toString())
            }
        }
    }

    private fun loadDailyIntention(dismissedDate: String?) {
        viewModelScope.launch {
            val intention = getDailyIntentionUseCase()
            val today = LocalDate.now().toString()
            _uiState.update {
                it.copy(
                    dailyIntention = intention,
                    isIntentionDismissed = dismissedDate == today
                )
            }
        }
    }

    private fun loadVerseOfTheDay() {
        viewModelScope.launch {
            getVerseOfTheDayUseCase()?.let { verse ->
                _uiState.update {
                    it.copy(
                        verseArabic = verse.arabicText,
                        verseTranslation = verse.translation,
                        verseReference = verse.reference
                    )
                }
            }
        }
    }

    fun toggleFavoriteIntention() {
        val currentIntention = _uiState.value.dailyIntention ?: return
        viewModelScope.launch {
            dailyIntentionRepository.toggleFavorite(currentIntention.id)
            loadDailyIntention(_uiState.value.isIntentionDismissed.let { if (it) LocalDate.now().toString() else null })
        }
    }

    fun markIntentionCompleted() {
        val currentIntention = _uiState.value.dailyIntention ?: return
        viewModelScope.launch {
            dailyIntentionRepository.markAsCompleted(currentIntention.id)
            _uiState.update { it.copy(intentionMessage = "May Allah make it easy for you today. 🌿") }
            loadDailyIntention(_uiState.value.isIntentionDismissed.let { if (it) LocalDate.now().toString() else null })
        }
    }

    fun dismissIntention() {
        viewModelScope.launch {
            userPreferencesRepository.setDailyIntentionDismissedDate(LocalDate.now().toString())
        }
    }

    fun clearIntentionMessage() {
        _uiState.update { it.copy(intentionMessage = null) }
    }

    private fun checkMissedPrayers(prayers: List<Prayer>) {
        if (prayers.isEmpty()) return
        val now = LocalTime.now()
        
        viewModelScope.launch {
            prayers.forEach { prayer ->
                if (prayer.name != PrayerName.SUNRISE && 
                    prayer.status == PrayerStatus.PENDING
                ) {
                    // A prayer is missed if the NEXT prayer has already started
                    val nextPrayer = prayers.filter { it.name != PrayerName.SUNRISE }
                        .firstOrNull { it.scheduledTime.isAfter(prayer.scheduledTime) }
                    
                    if (nextPrayer != null && now.isAfter(nextPrayer.scheduledTime)) {
                        updatePrayerStatusUseCase(prayer, PrayerStatus.MISSED)
                    } else if (nextPrayer == null && now.isBefore(LocalTime.MIDNIGHT) && now.isBefore(prayer.scheduledTime) == false) {
                        // For Isha, if it's past midnight and it's still today's date context
                        // we can mark it as missed once the next day starts.
                        // However, since this loop runs on today's prayers, we can just check if it's 
                        // very late or if tomorrow has arrived.
                    }
                }
            }
        }
    }

    private fun updateNextPrayer(prayers: List<Prayer>) {
        if (prayers.isEmpty()) return
        todayPrayers = prayers

        val now = LocalTime.now()
        
        // Filter out SUNRISE from "next prayer" if we only want actual prayers, 
        // as Sunrise is a landmark but not one of the 5 daily prayers.
        val actualPrayers = prayers.filter { it.name != PrayerName.SUNRISE }
        
        val next = actualPrayers.firstOrNull { it.scheduledTime.isAfter(now) }
            ?: actualPrayers.first() // If all passed, next is Fajr tomorrow (simplified)

        // Find current prayer
        val current = actualPrayers.lastOrNull { !it.scheduledTime.isAfter(now) }
            ?: actualPrayers.last() // If before Fajr, previous was Isha yesterday

        val isNextDay = !next.scheduledTime.isAfter(now)

        val duration = if (!isNextDay) {
            Duration.between(now, next.scheduledTime)
        } else {
            // It's after Isha, so next is Fajr tomorrow
            Duration.between(now, LocalTime.MAX).plus(Duration.between(LocalTime.MIN, next.scheduledTime))
        }

        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")

        _uiState.update {
            it.copy(
                currentPrayerName = current.name.name.lowercase().replaceFirstChar { char -> char.uppercase() },
                currentPrayerTime = current.scheduledTime.format(timeFormatter),
                nextPrayerName = next.name.name.lowercase().replaceFirstChar { char -> char.uppercase() },
                nextPrayerTime = next.scheduledTime.format(timeFormatter),
                remainingSeconds = duration.seconds
            )
        }
    }
}
