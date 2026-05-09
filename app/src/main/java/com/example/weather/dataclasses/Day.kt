package com.example.weather.dataclasses

import java.io.Serializable

data class Day (
    val date: String,
    val day_of_week: String,
    val hours_list: List<Hour>,
    val mean_temperature: Int,
    val mean_feels_like: Int,
    val mean_weather: String
): Serializable

