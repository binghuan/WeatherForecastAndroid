package com.bh.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bh.core.weather.WeatherRepository
import com.bh.core.weather.WeatherSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val summary: WeatherSummary? = null,
    val selectedCity: City = City.Predefined.default()
)

sealed class City(
    val name: String, val latitude: Double, val longitude: Double
) {
    class Predefined private constructor(
        name: String, latitude: Double, longitude: Double
    ) : City(name, latitude, longitude) {
        companion object {
            val cities = listOf(
                Predefined("San Francisco", 37.7749, -122.4194),
                Predefined("New York", 40.7128, -74.0060),
                Predefined("London", 51.5074, -0.1278),
                Predefined("Tokyo", 35.6762, 139.6503),
                Predefined("Sydney", -33.8688, 151.2093)
            )

            fun default(): Predefined = cities.first()
        }
    }
}

class ForecastViewModel(
    private val repository: WeatherRepository = WeatherRepository.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState

    init {
        refresh()
    }

    fun onCitySelected(city: City) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        refresh()
    }

    fun refresh() {
        val city = _uiState.value.selectedCity
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result =
                    repository.getWeather(city.latitude, city.longitude)
                _uiState.value =
                    _uiState.value.copy(isLoading = false, summary = result)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Unknown error"
                )
            }
        }
    }
}


