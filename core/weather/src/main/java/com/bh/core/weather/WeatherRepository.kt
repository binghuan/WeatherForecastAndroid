package com.bh.core.weather

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WeatherRepository(
    private val api: OpenMeteoApi
) {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherSummary {
        val response = api.forecast(latitude = latitude, longitude = longitude)
        val currentTemp = response.current_weather?.temperature
        val dailyBlock = response.daily
        val daily = buildList {
            if (dailyBlock != null) {
                val size = dailyBlock.time?.size ?: 0
                for (i in 0 until size) {
                    val dateIso = dailyBlock.time?.getOrNull(i) ?: continue
                    val max = dailyBlock.temperature_2m_max?.getOrNull(i) ?: continue
                    val min = dailyBlock.temperature_2m_min?.getOrNull(i) ?: continue
                    val precip = dailyBlock.precipitation_probability_mean?.getOrNull(i) ?: 0
                    val code = dailyBlock.weathercode?.getOrNull(i) ?: 0
                    add(
                        DailyForecast(
                            dateIso = dateIso,
                            tempMaxC = max,
                            tempMinC = min,
                            precipProbabilityPct = precip,
                            weatherCode = code
                        )
                    )
                }
            }
        }
        return WeatherSummary(currentTempC = currentTemp, daily = daily)
    }

    companion object {
        fun create(): WeatherRepository {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val api = retrofit.create(OpenMeteoApi::class.java)
            return WeatherRepository(api)
        }
    }
}


