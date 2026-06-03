package com.example.weather.dataclasses
data class Hour(
    var hour: String,
    var weather: String,
    var temperature: Int,
    var feelsLike: Int,
    var wind: String,
    var humidity: Int,
    var precipitation: Int?
)
