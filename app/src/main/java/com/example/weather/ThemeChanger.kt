package com.example.weather

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class ThemeChanger {
    companion object {
        fun changeTheme(context: Context) {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

            when (currentMode) {
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    prefs.edit { putBoolean("dark_mode", false) }
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    prefs.edit { putBoolean("dark_mode", true) }
                }
            }
        }
    }
}