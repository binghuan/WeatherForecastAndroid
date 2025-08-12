package com.bh.core.weather

data class OpenMeteoResponse(
    val latitude: Double?,
    val longitude: Double?,
    val timezone: String?,
    val current_weather: CurrentWeather?,
    val daily: DailyBlock?
)

data class CurrentWeather(
    val temperature: Double?, val windspeed: Double?, val weathercode: Int?
)

data class DailyBlock(
    val time: List<String>?,
    val temperature_2m_max: List<Double>?,
    val temperature_2m_min: List<Double>?,
    val precipitation_probability_mean: List<Int>?,
    val weathercode: List<Int>?
)

data class DailyForecast(
    val dateIso: String,
    val tempMaxC: Double,
    val tempMinC: Double,
    val precipProbabilityPct: Int,
    val weatherCode: Int
)

data class WeatherSummary(
    val currentTempC: Double?, val daily: List<DailyForecast>
)

