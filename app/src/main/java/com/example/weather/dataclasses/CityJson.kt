package com.example.weather.dataclasses
import kotlinx.serialization.Serializable
@Serializable
data class CityJson(
    val city: String,
    val latitude: Double,
    val longitude: Double
)
