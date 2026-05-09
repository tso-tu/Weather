package com.example.weather

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather.dataclasses.Day
import kotlinx.coroutines.MainScope
import java.text.SimpleDateFormat
import java.util.Date

class DayActivity : AppCompatActivity() {
    private lateinit var dateView: TextView
    private lateinit var temperatureView: TextView
    private lateinit var weatherView: TextView
    private lateinit var windView: TextView
    private lateinit var humidityView: TextView
    private lateinit var precipitationView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_day)

        val days: MutableList<Day> = MainActivity.days
        val i = intent.getIntExtra("index", 3)
        val day: Day = days[i]

        val formater = SimpleDateFormat("d MMMM, EE")
        val date = formater.format(Date())

        dateView = findViewById(R.id.current_date)
        dateView.text = date



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}