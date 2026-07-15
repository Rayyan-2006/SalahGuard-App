package com.salahguard.app.data.remote.api

import com.salahguard.app.data.remote.dto.PrayerTimesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") date: String, // DD-MM-YYYY
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2 // Islamic Society of North America (ISNA)
    ): PrayerTimesResponse
}
