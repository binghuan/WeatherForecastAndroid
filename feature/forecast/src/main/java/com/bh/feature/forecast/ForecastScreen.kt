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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
    state: UiState,
    onSelectCity: (City) -> Unit,
    onRefresh: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
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
            CityPicker(current = state.selectedCity, onSelectCity = onSelectCity)

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
            Text(text = current.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            City.Predefined.cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = {
                        onSelectCity(city)
                        expanded = false
                    }
                )
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
    val dateText = runCatching {
        LocalDate.parse(dateIso).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }.getOrElse { dateIso }

    ListItem(
        headlineContent = { Text(text = dateText) },
        supportingContent = { Text(text = "${minC.toInt()}° / ${maxC.toInt()}°  ·  ${precipPct}% rain") },
        leadingContent = { Text(text = weatherEmoji(weatherCode), style = MaterialTheme.typography.titleLarge) }
    )
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


