package com.bh.core.weather

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeApi : OpenMeteoApi {
    override suspend fun forecast(
        latitude: Double,
        longitude: Double,
        currentWeather: Boolean,
        timezone: String,
        daily: String
    ): OpenMeteoResponse {
        return OpenMeteoResponse(
            latitude = latitude,
            longitude = longitude,
            timezone = timezone,
            current_weather = CurrentWeather(temperature = 21.0, windspeed = 3.0, weathercode = 1),
            daily = DailyBlock(
                time = listOf("2025-10-14", "2025-10-15"),
                temperature_2m_max = listOf(20.0, 22.0),
                temperature_2m_min = listOf(10.0, 12.0),
                precipitation_probability_mean = listOf(30, 10),
                weathercode = listOf(61, 0)
            )
        )
    }
}

class WeatherRepositoryTest {
    @Test
    fun getWeather_mapsResponse() = runBlocking {
        val repo = WeatherRepository(api = FakeApi())
        val summary = repo.getWeather(latitude = 0.0, longitude = 0.0)

        assertEquals(21.0, summary.currentTempC!!, 0.0)
        assertEquals(2, summary.daily.size)
        assertEquals("2025-10-14", summary.daily[0].dateIso)
        assertEquals(20.0, summary.daily[0].tempMaxC, 0.0)
        assertEquals(10.0, summary.daily[0].tempMinC, 0.0)
        assertEquals(30, summary.daily[0].precipProbabilityPct)
    }
}


