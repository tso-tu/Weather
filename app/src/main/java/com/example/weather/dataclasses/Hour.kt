package com.example.weather.dataclasses


data class Hour(
    var hour: String,
    var weather: String,
    var temperature: Int,
    var feels_like: Int,
    var wind: String,
    var humidity: Int,
    var precipitation: Int?
)
