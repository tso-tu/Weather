package com.example.weather

import ChartMarkerView
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather.MainActivity.Companion.days
import com.example.weather.dataclasses.Day
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import java.text.SimpleDateFormat
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.Locale

class DayActivity : AppCompatActivity() {
    lateinit var day: Day
    private lateinit var dateView: TextView
    private lateinit var temperatureView: TextView
    private lateinit var weatherView: TextView
    private lateinit var windView: TextView
    private lateinit var humidityView: TextView
    private lateinit var precipitationView: TextView
    private lateinit var weatherImageView: ImageView
    private lateinit var backgroundImageView: ImageView
    private lateinit var citiesButton: Button

    private lateinit var hoursChart: LineChart

    private val hours: MutableList<String> = mutableListOf()
    private val temperatures: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_day)

        val days: MutableList<Day> = MainActivity.days
        val i = intent.getIntExtra("index", 0)
        day= days[i]

        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("d MMMM, EE", Locale("ru"))
        val date = outputFormat.format(inputFormat.parse(day.date))

        dateView = findViewById(R.id.date)
        dateView.text = date
        temperatureView = findViewById(R.id.temperature)
        temperatureView.text = day.hours_list[0].temperature.toString()
        weatherView = findViewById(R.id.weather)
        val weather = day.hours_list[0].weather
        val feels_like = day.hours_list[0].feels_like
        weatherView.text = "$weather, ощущается как $feels_like"
        windView = findViewById(R.id.wind)
        windView.text = day.hours_list[0].wind
        humidityView = findViewById(R.id.humidity)
        humidityView.text = day.hours_list[0].humidity.toString()
        precipitationView = findViewById(R.id.precipitation)
        precipitationView.text = day.hours_list[0].precipitation.toString()
        weatherImageView = findViewById(R.id.weather_img)
        val weatherImage = when (weather) {
            "Clear" -> R.drawable.clear
            "Clouds" -> R.drawable.cloudy
            "Rain" -> R.drawable.rain
            "Thunderstorm" -> R.drawable.thunder
            else -> null
        }
        weatherImageView.setImageDrawable(ContextCompat.getDrawable(this, weatherImage!!))
        backgroundImageView = findViewById(R.id.background) as ImageView
        setBackground(day.hours_list[0].hour, i)

        citiesButton = findViewById(R.id.cities_button)
        citiesButton.setOnClickListener {
            val intent = Intent(this, CitiesActivity::class.java)
            startActivity(intent)
        }

        for (hour in day.hours_list){
            hours.add(hour.hour)
        }
        for (hour in day.hours_list){
            temperatures.add(hour.temperature)
        }

        hoursChart = findViewById(R.id.hoursChart)
        createChart(i)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


   private fun createChart(day_num: Int) {
        val entries = mutableListOf<Entry>()
        for (i in temperatures.indices) {
            entries.add(Entry(i.toFloat(), temperatures[i].toFloat()))
        }
        val dataSet = LineDataSet(entries, "")
        dataSet.apply {
            color = Color.rgb(76, 175, 80)
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        hoursChart.apply {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            isDragEnabled = true
            setDrawGridBackground(false)

            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            xAxis.apply {
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(hours)
                granularity = 1f
                setLabelCount(hours.size, true)
                setAvoidFirstLastClipping(true)
                textSize = 20f
                textColor = Color.BLACK
                axisLineColor = Color.GRAY
                axisLineWidth = 1f
            }

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null) {
                        val i = e.x.toInt()
                        val selectedHour = day.hours_list[i]

                        temperatureView.text = selectedHour.temperature.toString()

                        val weather = selectedHour.weather
                        val feels_like = selectedHour.feels_like
                        weatherView.text = "$weather, ощущается как $feels_like"

                        windView.text = selectedHour.wind
                        humidityView.text = selectedHour.humidity.toString()
                        precipitationView.text = selectedHour.precipitation.toString()
                        val weatherImage = when (weather) {
                            "Clear" -> R.drawable.clear
                            "Clouds" -> R.drawable.cloudy
                            "Rain" -> R.drawable.rain
                            "Thunderstorm" -> R.drawable.thunder
                            else -> null
                        }
                        weatherImageView.setImageDrawable(ContextCompat.getDrawable(this@DayActivity, weatherImage!!))
                        setBackground(selectedHour.hour, day_num)
                    }
                }

                override fun onNothingSelected() {
                    val firstHour = day.hours_list[0]
                    temperatureView.text = firstHour.temperature.toString()
                    val weather = firstHour.weather
                    val feels_like = firstHour.feels_like
                    weatherView.text = "$weather, ощущается как $feels_like"
                    windView.text = firstHour.wind
                    humidityView.text = firstHour.humidity.toString()
                    precipitationView.text = firstHour.precipitation.toString()
                    val weatherImage = when (weather) {
                        "Clear" -> R.drawable.clear
                        "Clouds" -> R.drawable.cloudy
                        "Rain" -> R.drawable.rain
                        "Thunderstorm" -> R.drawable.thunder
                        else -> null
                    }
                    weatherImageView.setImageDrawable(ContextCompat.getDrawable(this@DayActivity, weatherImage!!))
                    setBackground(firstHour.hour, day_num)
                }
            })
        }
        hoursChart.marker = ChartMarkerView(this)
        hoursChart.setBackgroundColor(Color.LTGRAY)
        hoursChart.setNoDataText("Данных нет")
        hoursChart.invalidate()
    }

    private fun setBackground(hour:String, day_num: Int){
        val dawnStart = day.dawn - 5400 + day_num*86400
        val dawnEnd =  day.dawn + 5400 + day_num*86400

        val duskStart = day.dusk - 5400 + day_num*86400
        val duskEnd = day.dusk + 5400 + day_num*86400

        val dateTimeString = "${day.date} ${hour}"
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val time = format.parse(dateTimeString).time.div(1000)

        val background = when {
            time in dawnStart..dawnEnd -> R.drawable.sunrise_sunset_background
            time in duskStart..duskEnd -> R.drawable.sunrise_sunset_background
            (time > day.dusk + day_num*86400) || (time < day.dawn + day_num*86400) -> R.drawable.night_background
            else -> R.drawable.day_background
        }

        backgroundImageView.setImageDrawable(ContextCompat.getDrawable(this@DayActivity, background))
    }
}