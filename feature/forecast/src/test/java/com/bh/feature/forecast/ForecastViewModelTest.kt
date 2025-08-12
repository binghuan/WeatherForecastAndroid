package com.bh.feature.forecast

import com.bh.core.weather.CurrentWeather
import com.bh.core.weather.DailyBlock
import com.bh.core.weather.OpenMeteoResponse
import com.bh.core.weather.OpenMeteoApi
import com.bh.core.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
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
            current_weather = CurrentWeather(temperature = 18.0, windspeed = 2.0, weathercode = 1),
            daily = DailyBlock(
                time = listOf("2025-10-14"),
                temperature_2m_max = listOf(20.0),
                temperature_2m_min = listOf(10.0),
                precipitation_probability_mean = listOf(30),
                weathercode = listOf(61)
            )
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refresh_updatesUiState() = runTest(dispatcher) {
        val vm = ForecastViewModel(repository = WeatherRepository(api = FakeApi()))

        // Allow launched coroutines to run
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(18.0, state.summary?.currentTempC!!, 0.0)
        assertEquals(1, state.summary?.daily?.size ?: 0)
    }
}


