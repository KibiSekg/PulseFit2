package com.pulsefit.app.data.repository

import com.pulsefit.app.BuildConfig
import com.pulsefit.app.data.model.WeatherResponse
import com.pulsefit.app.data.remote.RetrofitClient

class WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherResponse> = try {
        val response = RetrofitClient.weatherApi.getCurrentWeather(lat, lon, BuildConfig.OPENWEATHER_API_KEY)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
