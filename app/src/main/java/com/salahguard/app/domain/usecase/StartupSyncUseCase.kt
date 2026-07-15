package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.repository.QuranRepository
import javax.inject.Inject

class StartupSyncUseCase @Inject constructor(
    private val quranRepository: QuranRepository
) {
    suspend operator fun invoke() {
        // Seed Quran data if needed
        quranRepository.syncQuranData()
    }
}
