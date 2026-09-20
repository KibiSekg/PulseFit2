package com.pulsefit.app.data.model

import com.google.gson.annotations.SerializedName

/** Maps the subset of the OpenWeatherMap /weather response PulseFit actually uses. */
data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("main") val main: MainWeather,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("name") val locationName: String
)

data class WeatherCondition(
    @SerializedName("main") val main: String,       // e.g. "Rain", "Clear"
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class MainWeather(
    @SerializedName("temp") val tempCelsius: Double,
    @SerializedName("feels_like") val feelsLikeCelsius: Double,
    @SerializedName("humidity") val humidity: Int
)

data class Wind(
    @SerializedName("speed") val speedMs: Double
)

/** Simple risk classification used to drive Adaptive Weather Routing decisions. */
enum class WeatherRisk { SAFE, CAUTION, UNSAFE }

fun WeatherResponse.assessRisk(): WeatherRisk {
    val condition = weather.firstOrNull()?.main.orEmpty()
    return when {
        condition in listOf("Thunderstorm", "Tornado") -> WeatherRisk.UNSAFE
        condition in listOf("Rain", "Snow") && wind.speedMs > 10 -> WeatherRisk.UNSAFE
        condition in listOf("Rain", "Snow", "Drizzle") -> WeatherRisk.CAUTION
        main.tempCelsius >= 35 || main.tempCelsius <= -5 -> WeatherRisk.CAUTION
        else -> WeatherRisk.SAFE
    }
}
