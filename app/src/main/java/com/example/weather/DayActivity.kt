package com.example.weather

import ChartMarkerView
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var gradientImageView: ImageView
    private lateinit var citiesButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var themeButton: ImageButton

    private lateinit var hoursChart: LineChart

    private lateinit var cityOnToolbarView: TextView

    private val hours: MutableList<String> = mutableListOf()
    private val temperatures: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_day)

        val days: MutableList<Day> = MainActivity.days
        val i = intent.getIntExtra("index", 0)
        day= days[i]

        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputDateFormat = SimpleDateFormat("d MMMM, EE", Locale("ru"))
        val date = outputDateFormat.format(inputDateFormat.parse(day.date))
        dateView = findViewById(R.id.date)
        dateView.text = date
        temperatureView = findViewById(R.id.temperature)
        temperatureView.text = "${day.hoursList[0].temperature}°C"
        weatherView = findViewById(R.id.weather)
        val weather = day.hoursList[0].weather
        val feelsLike = day.hoursList[0].feelsLike
        weatherView.text = "${translate(weather)}, ощущается как $feelsLike°"
        windView = findViewById(R.id.wind)
        windView.text = day.hoursList[0].wind
        humidityView = findViewById(R.id.humidity)
        humidityView.text = day.hoursList[0].humidity.toString()
        precipitationView = findViewById(R.id.precipitation)
        precipitationView.text = day.hoursList[0].precipitation.toString()
        val weatherImage = when (weather) {
            "Clear" -> R.drawable.clear_anim
            "Clouds" -> R.drawable.cloudy_anim
            "Rain" -> R.drawable.rain_anim
            "Thunderstorm" -> R.drawable.thunder_anim
            "Snow" -> R.drawable.rain_anim
            "Drizzle" -> R.drawable.rain_anim
            else -> R.drawable.cloudy_anim
        }
        //currentWeatherImageView.setImageDrawable(ContextCompat.getDrawable(this, weatherImage))
        weatherImageView = findViewById(R.id.weather_img)
        weatherImageView.setImageResource(weatherImage)
        val animation = weatherImageView.drawable as AnimationDrawable
        animation.start()
        backgroundImageView = findViewById(R.id.background)
        gradientImageView = findViewById(R.id.gradient)
        setBackground(day.hoursList[0].hour, i)

        citiesButton = findViewById(R.id.cities_button)
        citiesButton.setOnClickListener {
            val intent = Intent(this, CitiesActivity::class.java)
            changeCityLauncher.launch(intent)
        }

        cityOnToolbarView = findViewById(R.id.city)
        cityOnToolbarView.text = MainActivity.city

        for (hour in day.hoursList){
            hours.add(hour.hour)
        }
        for (hour in day.hoursList){
            temperatures.add(hour.temperature)
        }

        hoursChart = findViewById(R.id.hoursChart)
        createChart(i)

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        themeButton = findViewById(R.id.theme_button)
        themeButton.setOnClickListener {
            ThemeChanger.changeTheme(this)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

   private fun createChart(dayNum: Int) {
        val entries = mutableListOf<Entry>()
        for (i in temperatures.indices) {
            entries.add(Entry(i.toFloat(), temperatures[i].toFloat()))
        }
        val dataSet = LineDataSet(entries, "")
       dataSet.apply {
           color = ContextCompat.getColor(this@DayActivity, R.color.blue)
           lineWidth = 5f
           setDrawCircles(false)
           setDrawValues(false)
           mode = LineDataSet.Mode.CUBIC_BEZIER
           setDrawCircleHole(false)
           circleRadius = 6f

           setDrawHighlightIndicators(false)
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

           setVisibleXRangeMaximum(4f)
           isDragDecelerationEnabled = true

           legend.isEnabled = false

           axisLeft.isEnabled = false
           axisRight.isEnabled = false

           xAxis.apply {
               setDrawGridLines(true)
               gridColor = Color.LTGRAY
               position = XAxis.XAxisPosition.BOTTOM
               valueFormatter = IndexAxisValueFormatter(hours)

               setLabelCount(hours.size, false)
               granularity = 1f
               isGranularityEnabled = true
               setAvoidFirstLastClipping(true)
               setDrawLabels(true)
               textSize = 16f

               textColor = ContextCompat.getColor(context, R.color.grey)
               labelRotationAngle = 0f

               axisMinimum = -0.5f
               axisMaximum = (hours.size - 1).toFloat() + 0.5f

               axisLineColor = Color.GRAY
               axisLineWidth = 1f
               setDrawAxisLine(false)
               setCenterAxisLabels(false)
           }

           xAxis.setDrawGridLines(false)

           axisLeft.apply {
               isEnabled = true
               setDrawGridLines(true)
               setDrawAxisLine(false)
               setDrawLabels(false)

               axisMaximum = 40f

               enableGridDashedLine(10f, 10f, 0f)
               gridColor = Color.rgb(180, 180, 180)
               gridLineWidth = 1f
           }
           axisRight.isEnabled = false

           setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
               override fun onValueSelected(e: Entry?, h: Highlight?) {
                   if (e != null) {
                       val i = e.x.toInt()
                       if (i in day.hoursList.indices) {
                           val selectedHour = day.hoursList[i]

                           temperatureView.text = "${selectedHour.temperature}°C"

                           val weather = selectedHour.weather
                           val feelsLike = selectedHour.feelsLike
                           weatherView.text = "${translate(weather)}, ощущается как $feelsLike°"

                           windView.text = selectedHour.wind
                           humidityView.text = selectedHour.humidity.toString()
                           precipitationView.text = selectedHour.precipitation.toString()
                           val weatherImage = when (weather) {
                               "Clear" -> R.drawable.clear_anim
                               "Clouds" -> R.drawable.cloudy_anim
                               "Rain" -> R.drawable.rain_anim
                               "Thunderstorm" -> R.drawable.thunder_anim
                               "Snow" -> R.drawable.rain_anim
                               "Drizzle" -> R.drawable.rain_anim
                               else -> R.drawable.cloudy_anim
                           }
                           //currentWeatherImageView.setImageDrawable(ContextCompat.getDrawable(this, weatherImage))
                           weatherImageView.setImageResource(weatherImage)
                           val animation = weatherImageView.drawable as AnimationDrawable
                           animation.start()
                           setBackground(selectedHour.hour, dayNum)
                       }
                   }
               }

               override fun onNothingSelected() {
                   val firstHour = day.hoursList[0]
                   temperatureView.text = "${firstHour.temperature}°C"
                   val weather = firstHour.weather
                   val feelsLike = firstHour.feelsLike
                   weatherView.text = "${translate(weather)}, ощущается как $feelsLike°"
                   windView.text = firstHour.wind
                   humidityView.text = firstHour.humidity.toString()
                   precipitationView.text = firstHour.precipitation.toString()
                   val weatherImage = when (weather) {
                       "Clear" -> R.drawable.clear_anim
                       "Clouds" -> R.drawable.cloudy_anim
                       "Rain" -> R.drawable.rain_anim
                       "Thunderstorm" -> R.drawable.thunder_anim
                       "Snow" -> R.drawable.rain_anim
                       "Drizzle" -> R.drawable.rain_anim
                       else -> R.drawable.cloudy_anim
                   }
                   //currentWeatherImageView.setImageDrawable(ContextCompat.getDrawable(this, weatherImage))
                   weatherImageView.setImageResource(weatherImage)
                   val animation = weatherImageView.drawable as AnimationDrawable
                   animation.start()
                   setBackground(firstHour.hour, dayNum)
               }
           })
           marker = ChartMarkerView(this@DayActivity)
           setNoDataText("Данных нет")
           extraBottomOffset = 20f
           invalidate()
       }
   }

    private fun setBackground(hour:String, dayNum: Int) {
        val dawnStart = day.dawn - 5400 + dayNum*86400
        val dawnEnd =  day.dawn + 5400 + dayNum*86400

        val duskStart = day.dusk - 5400 + dayNum*86400
        val duskEnd = day.dusk + 5400 + dayNum*86400

        println(dawnStart)
        println(duskStart)
        val dateTimeString = "${day.date} $hour"
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ru"))
        val time = format.parse(dateTimeString)!!.time / 1000
        println(time)
        val background = when {
            time in dawnStart..dawnEnd -> R.drawable.sunrise_sunset_background
            time in duskStart..duskEnd -> R.drawable.sunrise_sunset_background
            (time > duskEnd) || (time < dawnStart) -> R.drawable.night_background
            else -> R.drawable.day_background
        }
        backgroundImageView.setImageDrawable(ContextCompat.getDrawable(this@DayActivity, background))

        val gradient = when {
            time in dawnStart..dawnEnd -> R.drawable.sunrise_sunset_gradient2
            time in duskStart..duskEnd -> R.drawable.sunrise_sunset_gradient2
            (time > day.dusk + dayNum*86400) || (time < day.dawn + dayNum*86400) -> R.drawable.night_gradient2
            else -> R.drawable.day_gradient2
        }

        gradientImageView.setImageDrawable(ContextCompat.getDrawable(this@DayActivity, gradient))
    }

    private fun translate(weather: String): String {
        return when (weather) {
            "Clear" -> "Ясно"
            "Clouds" -> "Облачно"
            "Rain" -> "Дождь"
            "Thunderstorm" -> "Гроза"
            "Snow" -> "Снег"
            "Drizzle" -> "Морось"
            "Mist" -> "Туман"
            "Smoke" -> "Дымка"
            "Haze" -> "Мгла"
            "Dust" -> "Пыль"
            "Fog" -> "Туман"
            "Sand" -> "Песок"
            "Ash" -> "Пепел"
            "Squall" -> "Шквал"
            "Tornado" -> "Торнадо"
            else -> weather
        }
    }

    private val changeCityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val city = data?.getStringExtra("city") ?: return@registerForActivityResult
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)

            if (latitude != 0.0 && longitude != 0.0) {
                val intent = Intent().apply {
                    putExtra("fromDayActivity", true)
                    putExtra("city", city)
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}