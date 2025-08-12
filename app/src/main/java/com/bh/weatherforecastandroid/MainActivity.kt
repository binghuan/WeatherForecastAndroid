package com.bh.weatherforecastandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bh.feature.forecast.ForecastRoute
import com.bh.feature.forecast.ForecastViewModel
import com.bh.weatherforecastandroid.ui.theme.WeatherForecastAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherForecastAndroidTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ForecastRoute(viewModel = ForecastViewModel())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForecastPreview() {
    WeatherForecastAndroidTheme { ForecastRoute(viewModel = ForecastViewModel()) }
}