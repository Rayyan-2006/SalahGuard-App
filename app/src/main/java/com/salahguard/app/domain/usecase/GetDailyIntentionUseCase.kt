package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.DailyIntention
import com.salahguard.app.domain.repository.DailyIntentionRepository
import javax.inject.Inject

class GetDailyIntentionUseCase @Inject constructor(
    private val repository: DailyIntentionRepository
) {
    suspend operator fun invoke(): DailyIntention {
        return repository.getDailyIntention()
    }
}
