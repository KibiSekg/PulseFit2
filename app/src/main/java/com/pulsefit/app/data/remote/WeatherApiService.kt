package com.pulsefit.app.data.remote

import com.pulsefit.app.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** OpenWeatherMap Current Weather Data API — powers Adaptive Weather Routing. */
interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
