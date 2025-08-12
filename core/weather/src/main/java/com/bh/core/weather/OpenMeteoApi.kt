package com.bh.core.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    // Daily forecast for 7 days and current weather
    // Docs: https://open-meteo.com/en/docs
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("timezone") timezone: String = "auto",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode"
    ): OpenMeteoResponse
}


