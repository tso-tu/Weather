package com.example.weather.dataclasses

data class Day(
    val date: String,
    val day_of_week: String,
    val temperature: Int,
    val feels_temperaure: Int,
    val hours_list: List<Hour>
)

