package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.Surah
import com.salahguard.app.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSurahsUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    operator fun invoke(): Flow<List<Surah>> = repository.getSurahs()
}
