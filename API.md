## Open‑Meteo API

This project uses the Open‑Meteo Forecast API. It is free and does not require an API key.

### Endpoint
- Base URL: `https://api.open-meteo.com/`
- Path: `/v1/forecast`
- Docs: `https://open-meteo.com/en/docs`

### Query parameters (used by this app)
- `latitude` Double — e.g., `37.7749`
- `longitude` Double — e.g., `-122.4194`
- `current_weather` Boolean — include current weather, `true`
- `timezone` String — e.g., `auto` (detect by coordinates)
- `daily` Comma‑separated fields — we request: `temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode`

### Example request
```text
GET /v1/forecast?latitude=37.7749&longitude=-122.4194&current_weather=true&timezone=auto&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode
Host: api.open-meteo.com
```

Open in browser:
`https://api.open-meteo.com/v1/forecast?latitude=37.7749&longitude=-122.4194&current_weather=true&timezone=auto&daily=temperature_2m_max%2Ctemperature_2m_min%2Cprecipitation_probability_mean%2Cweathercode`

### Truncated response example
```json
{
  "latitude": 37.763283,
  "longitude": -122.41286,
  "timezone": "America/Los_Angeles",
  "current_weather": {
    "time": "2025-08-12T07:00",
    "temperature": 14.6,
    "windspeed": 8.8,
    "weathercode": 45
  },
  "daily": {
    "time": ["2025-08-12", "2025-08-13", "2025-08-14"],
    "temperature_2m_max": [19.2, 18.2, 21.1],
    "temperature_2m_min": [13.5, 13.1, 15.0],
    "precipitation_probability_mean": [1, 1, 2],
    "weathercode": [51, 45, 3]
  }
}
```

### Kotlin data models (mapping)
- `OpenMeteoResponse`
  - `latitude: Double?`
  - `longitude: Double?`
  - `timezone: String?`
  - `current_weather: CurrentWeather?`
  - `daily: DailyBlock?`
- `CurrentWeather`
  - `temperature: Double?`
  - `windspeed: Double?`
  - `weathercode: Int?`
- `DailyBlock`
  - `time: List<String>?`
  - `temperature_2m_max: List<Double>?`
  - `temperature_2m_min: List<Double>?`
  - `precipitation_probability_mean: List<Int>?`
  - `weathercode: List<Int>?`

### Implementation notes
- Retrofit + Moshi is used to call and parse the API. Moshi is configured with `KotlinJsonAdapterFactory()` in `WeatherRepository.create()`.
- The repository converts `OpenMeteoResponse` to a UI‑friendly `WeatherSummary` used by `ForecastViewModel` and Compose UI.