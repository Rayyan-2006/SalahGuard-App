package com.salahguard.app.domain.service

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.Madhab
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class PrayerTimeCalculator @Inject constructor() {

    fun calculatePrayers(
        date: LocalDate,
        latitude: Double,
        longitude: Double
    ): List<Prayer> {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents.from(
            Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        )
        
        // Use KARACHI method as it is standard for the Indian subcontinent.
        // Also explicitly set Madhab to HANAFI as it's predominant in India.
        val params = CalculationMethod.KARACHI.getParameters()
        params.madhab = Madhab.HANAFI
        
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        return listOf(
            createPrayer(PrayerName.FAJR, date, prayerTimes.fajr),
            createPrayer(PrayerName.SUNRISE, date, prayerTimes.sunrise),
            createPrayer(PrayerName.DHUHR, date, prayerTimes.dhuhr),
            createPrayer(PrayerName.ASR, date, prayerTimes.asr),
            createPrayer(PrayerName.MAGHRIB, date, prayerTimes.maghrib),
            createPrayer(PrayerName.ISHA, date, prayerTimes.isha)
        )
    }

    private fun createPrayer(name: PrayerName, date: LocalDate, prayerDate: Date): Prayer {
        val localTime = prayerDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return Prayer(
            name = name,
            date = date,
            scheduledTime = localTime,
            status = PrayerStatus.PENDING
        )
    }
}
