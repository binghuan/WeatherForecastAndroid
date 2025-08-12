package com.bh.feature.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.bh.core.weather.DailyForecast
import com.bh.core.weather.WeatherSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ForecastRoute(viewModel: ForecastViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ForecastScreen(
        state = state,
        onSelectCity = viewModel::onCitySelected,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun ForecastScreen(
    state: UiState, onSelectCity: (City) -> Unit, onRefresh: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weather Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = onRefresh,
                    label = { Text("Refresh") },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            CityPicker(
                current = state.selectedCity, onSelectCity = onSelectCity
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Error: ${state.errorMessage}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {
                    val summary = state.summary
                    if (summary != null) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = state.selectedCity.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${summary.currentTempC?.toInt() ?: "-"}°C",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(summary.daily) { day ->
                                DailyForecastRow(
                                    dateIso = day.dateIso,
                                    minC = day.tempMinC,
                                    maxC = day.tempMaxC,
                                    precipPct = day.precipProbabilityPct,
                                    weatherCode = day.weatherCode
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityPicker(current: City, onSelectCity: (City) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = current.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            City.Predefined.cities.forEach { city ->
                DropdownMenuItem(text = { Text(city.name) }, onClick = {
                    onSelectCity(city)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    dateIso: String,
    minC: Double,
    maxC: Double,
    precipPct: Int,
    weatherCode: Int
) {
    val parsedDate = runCatching { LocalDate.parse(dateIso) }.getOrNull()
    val dateText =
        parsedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            ?: dateIso
    val dayOfWeek = parsedDate?.dayOfWeek?.getDisplayName(
        TextStyle.SHORT, Locale.getDefault()
    ) ?: ""
    val headline =
        if (dayOfWeek.isNotEmpty()) "$dayOfWeek, $dateText" else dateText

    ListItem(
        headlineContent = { Text(text = headline) },
        supportingContent = { Text(text = "${minC.toInt()}° / ${maxC.toInt()}°  ·  ${precipPct}% rain") },
        leadingContent = {
            Text(
                text = weatherEmoji(weatherCode),
                style = MaterialTheme.typography.titleLarge
            )
        })
}

private fun weatherEmoji(code: Int): String = when (code) {
    0 -> "☀️" // Clear sky
    1, 2 -> "🌤️" // Mainly clear, partly cloudy
    3 -> "☁️" // Overcast
    45, 48 -> "🌫️" // Fog
    51, 53, 55 -> "🌦️" // Drizzle
    61, 63, 65 -> "🌧️" // Rain
    66, 67 -> "🌧️❄️" // Freezing rain
    71, 73, 75, 77 -> "❄️" // Snow
    80, 81, 82 -> "🌦️" // Rain showers
    85, 86 -> "🌨️" // Snow showers
    95 -> "⛈️" // Thunderstorm
    96, 99 -> "⛈️" // Thunderstorm with hail
    else -> "🌡️"
}

// ---------- Previews ----------

@Preview(name = "Forecast - Content", showBackground = true, widthDp = 360)
@Composable
private fun PreviewForecastScreen_Content() {
    val sampleDaily = listOf(
        DailyForecast("2025-10-14", 12.0, 20.0, 30, 61),
        DailyForecast("2025-10-15", 13.0, 22.0, 10, 0),
        DailyForecast("2025-10-16", 11.0, 18.0, 60, 80),
        DailyForecast("2025-10-17", 10.0, 19.0, 20, 3),
        DailyForecast("2025-10-18", 9.0, 17.0, 50, 45)
    )
    val state = UiState(
        isLoading = false,
        errorMessage = null,
        summary = WeatherSummary(currentTempC = 21.5, daily = sampleDaily)
    )
    ForecastScreen(state = state, onSelectCity = {}, onRefresh = {})
}

@Preview(name = "Forecast - Loading", showBackground = true, widthDp = 360)
@Composable
private fun PreviewForecastScreen_Loading() {
    val state = UiState(isLoading = true)
    ForecastScreen(state = state, onSelectCity = {}, onRefresh = {})
}

@Preview(name = "Forecast - Error", showBackground = true, widthDp = 360)
@Composable
private fun PreviewForecastScreen_Error() {
    val state = UiState(isLoading = false, errorMessage = "Network error")
    ForecastScreen(state = state, onSelectCity = {}, onRefresh = {})
}

@Preview(name = "CityPicker", showBackground = true, widthDp = 360)
@Composable
private fun PreviewCityPicker() {
    Surface {
        CityPicker(
            current = City.Predefined.default(), onSelectCity = {})
    }
}


