package com.salahguard.app.domain.service

object HopeModeReminders {
    private val reminders = listOf(
        "Every prayer is another opportunity to return.",
        "Allah loves those who turn back to Him.",
        "Small steps today become lifelong consistency.",
        "One missed prayer does not define your journey.",
        "Allah's mercy is greater than our shortcomings.",
        "Every sincere return is beloved.",
        "Begin again with hope.",
        "Today's next prayer is a fresh opportunity.",
        "Tranquility is just one Sujud away.",
        "Your intention to return is already a step forward.",
        "Each prayer is a new beginning.",
        "Grace is found in the effort to reconnect.",
        "Allah is closer than we imagine.",
        "Returning to prayer is returning to peace.",
        "Your journey is uniquely yours, keep moving forward.",
        "Mercy awaits in every Takbir.",
        "The door to connection is always open.",
        "Consistency grows from small acts of devotion.",
        "Every moment is a chance to start fresh.",
        "Peace is found in remembrance."
    )

    fun getRandomReminder(): String = reminders.random()
}
