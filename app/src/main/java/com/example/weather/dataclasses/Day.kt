package com.example.weather.dataclasses

import java.io.Serializable

data class Day (
    val date: String,
    val hoursList: List<Hour>,
    val meanTemperature: Int,
    val meanFeelsLike: Int,
    val meanWeather: String,
    val dawn: Long,
    val dusk: Long
): Serializable

