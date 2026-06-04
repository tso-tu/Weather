package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.adapters.DaysListAdapter
import com.example.weather.dataclasses.Day
import com.example.weather.dataclasses.Hour
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var currentDateView: TextView
    private lateinit var currentTemperatureView: TextView
    private lateinit var currentWeatherView: TextView
    private lateinit var currentWeatherImageView: ImageView
    private lateinit var backgroundImageView: ImageView
    private lateinit var gradientImageView: ImageView
    private lateinit var citiesButton: ImageButton
    private lateinit var mapButton: ImageButton
    private lateinit var themeButton: ImageButton

    private lateinit var cityOnToolbarView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var adapter: DaysListAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var loader: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        getSavedTheme()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        swipeLayout = findViewById(R.id.swipeLayout)
        loader = findViewById(R.id.loader)
        showLoader()

        val formater = SimpleDateFormat("d MMMM, EE", Locale("ru"))
        val currentDate = formater.format(Date())
        currentDateView = findViewById(R.id.current_date)
        currentDateView.text = currentDate
        currentTemperatureView = findViewById(R.id.current_temperature)
        currentWeatherView = findViewById(R.id.current_weather)
        currentWeatherImageView = findViewById(R.id.weather)

        backgroundImageView = findViewById(R.id.background)
        backgroundImageView.setOnClickListener {
            val intent = Intent(this, DayActivity::class.java)
            changeCityLauncher.launch(intent)
        }

        gradientImageView = findViewById(R.id.gradient)

        recyclerView = findViewById(R.id.days)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = DaysListAdapter(this,days.slice(1 until days.size), object :
            DaysListAdapter.OnClickListener {
            override fun onClick(day: Day, position: Int) {
                onClick(position)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.setNestedScrollingEnabled(true)

        citiesButton = findViewById(R.id.cities_button)
        citiesButton.setOnClickListener {
            val intent = Intent(this, CitiesActivity::class.java)
            changeCityLauncher.launch(intent)
        }

        cityOnToolbarView = findViewById(R.id.city)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
        } else {
            fetchLocation()
        }

        mapButton = findViewById(R.id.map_button)
        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            changeCityLauncher.launch(intent)
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

    private fun showLoader() {
        swipeLayout.visibility = View.GONE
        loader.visibility = View.VISIBLE
    }

    private fun showContent() {
        swipeLayout.visibility = View.VISIBLE
        loader.visibility = View.GONE
        swipeLayout.setOnRefreshListener {
            fetchLocation()
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"
                    fetchWeatherData(weatherUrl)
                } else {
                    val wifiManager =
                        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    val ipAddress: String? =
                        Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress())
                    println(ipAddress)
                    val queue = Volley.newRequestQueue(this)
                    val request = StringRequest(Request.Method.GET,"http://ip-api.com/json/$ipAddress",
                        { response ->
                            try {
                                val jsonResponse = JSONObject(response)
                                latitude = jsonResponse.getString("lat").toDouble()
                                longitude = jsonResponse.getString("lon").toDouble()
                                val weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"
                                fetchWeatherData(weatherUrl)
                            } catch (e: Exception){
                                e.printStackTrace()
                            }
                        }, { error ->
                            error.printStackTrace()
                        })
                    queue.add(request)
                    println("Локация не определяется")
                }
            }
    }

    private fun fetchWeatherData(url: String) {
        days.clear()
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val list = jsonResponse.getJSONArray("list")

                    val dawn = jsonResponse.getJSONObject("city").getLong("sunrise")
                    val dusk = jsonResponse.getJSONObject("city").getLong("sunset")


                    var currentDate = list.getJSONObject(0).getString("dt_txt").split("\\s+".toRegex())[0]

                    val hoursList : MutableList<Hour> = mutableListOf()
                    for (i in 0 until list.length()){
                        if (currentDate == list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[0]) {
                            val main = list.getJSONObject(i).getJSONObject("main")

                            val temperature = main.getInt("temp")
                            val weather = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("main")
                            val feelsLike = main.getInt("feels_like")

                            val wind = list.getJSONObject(i).getJSONObject("wind").getInt("speed").toString()
                            val humidity = main.getInt("humidity")

                            var precipitation = 0
                            try {
                                precipitation = list.getJSONObject(i).getJSONObject("rain").optInt("1h", list.getJSONObject(i).getJSONObject("rain").optInt("3h", 0))
                            } catch (e: JSONException) {
                            }
                            hoursList.add(Hour(list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[1].slice(0 until 5), weather, temperature, feelsLike, wind, humidity, precipitation))
                        }
                        else {
                            var meanTemperature = 0
                            var meanFeelsLike = 0
                            var cnt = 0
                            for (hour in hoursList){
                                cnt++
                                meanTemperature += hour.temperature
                                meanFeelsLike += hour.feelsLike
                            }
                            meanTemperature /= cnt
                            meanFeelsLike /= cnt

                            val weatherList : MutableList<String> = mutableListOf()
                            for (hour in hoursList) {
                                weatherList.add(hour.weather)
                            }
                            val meanWeather = weatherList.groupingBy { it }.eachCount().maxBy { it.value }.key

                            days.add(Day(currentDate,hoursList.toList(), meanTemperature, meanFeelsLike, meanWeather, dawn, dusk))
                            currentDate = list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[0]
                            hoursList.clear()
                            val main = list.getJSONObject(i).getJSONObject("main")

                            val temperature = main.getInt("temp")
                            val weather = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("main")
                            val feelsLike = main.getInt("feels_like")

                            val wind = list.getJSONObject(i).getJSONObject("wind").getInt("speed").toString()
                            val humidity = main.getInt("humidity")

                            var precipitation = 0
                            try {
                                precipitation = list.getJSONObject(i).getJSONObject("rain").optInt("1h", list.getJSONObject(i).getJSONObject("rain").optInt("3h", 0))
                            } catch (e: JSONException) {
                            }
                            hoursList.add(Hour(list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[1].slice(0 until 5), weather, temperature, feelsLike, wind, humidity, precipitation))
                        }
                    }
                    setCurrentWeatherData()
                    adapter = DaysListAdapter(this, days.slice(1 until days.size),object : DaysListAdapter.OnClickListener {
                        override fun onClick(day: Day, position: Int) {
                            onClick(position)
                        }
                    })
                    recyclerView.setAdapter(adapter)

                } catch (error: Exception) {
                    error.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        queue.add(request)
    }

    private fun setCurrentWeatherData() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/geo/1.0/reverse?lat=$latitude&lon=$longitude&appid=$API_KEY&lang=ru"
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONArray(response)
                    try {
                        city = jsonResponse.getJSONObject(0).getJSONObject("local_names").getString("ru")
                    } catch (error: JSONException) {
                        city = jsonResponse.getJSONObject(0).getString("name")
                    }
                    cityOnToolbarView.text = city
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            },
            { error ->
        error.printStackTrace()
        })
        queue.add(request)


        val weather = days[0].hoursList[0].weather
        val temperature = days[0].hoursList[0].temperature
        val feelsLike = days[0].hoursList[0].feelsLike
        currentTemperatureView.text = "$temperature°C"
        currentWeatherView.text = "${translate(weather)}, ощущается как $feelsLike°"

        val weatherImage = when (weather) {
            "Clear" -> R.drawable.clear_anim
            "Clouds" -> R.drawable.cloudy_anim
            "Rain" -> R.drawable.rain_anim
            "Thunderstorm" -> R.drawable.thunder_anim
            "Snow" -> R.drawable.rain_anim
            "Drizzle" -> R.drawable.rain_anim
            else -> R.drawable.cloudy_anim
        }
        currentWeatherImageView.setImageResource(weatherImage)
        val animation = currentWeatherImageView.drawable as AnimationDrawable
        animation.start()

        val currentTime = System.currentTimeMillis() / 1000
        val dawnStart = days[0].dawn - 1800
        val dawnEnd =  days[0].dawn + 1800

        val duskStart = days[0].dusk - 1800
        val duskEnd = days[0].dusk + 1800

        val background = when {
            currentTime in dawnStart..dawnEnd -> R.drawable.sunrise_sunset_background
            currentTime in duskStart..duskEnd -> R.drawable.sunrise_sunset_background
            (currentTime > days[0].dusk) || (currentTime < days[0].dawn) -> R.drawable.night_background
            else -> R.drawable.day_background
        }
        backgroundImageView.setImageDrawable(ContextCompat.getDrawable(this, background))

        val gradient = when {
            currentTime in dawnStart..dawnEnd -> R.drawable.sunrise_sunset_gradient
            currentTime in duskStart..duskEnd -> R.drawable.sunrise_sunset_gradient
            (currentTime > days[0].dusk) || (currentTime < days[0].dawn) -> R.drawable.night_gradient
            else -> R.drawable.day_gradient
        }
        gradientImageView.setImageDrawable(ContextCompat.getDrawable(this, gradient))

        swipeLayout.isRefreshing = false
        showContent()
    }

    private fun onClick(index: Int) {
        val intent = Intent(this, DayActivity::class.java)
        intent.putExtra("index", index+1)
        intent.putExtra("city", city)
        changeCityLauncher.launch(intent)
    }

    private fun getSavedTheme() {
        val isNight = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark_mode", false)
        if (isNight) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
            showLoader()
            val data = result.data
            city = data?.getStringExtra("city") ?: return@registerForActivityResult
            latitude = data.getDoubleExtra("latitude", 0.0)
            longitude = data.getDoubleExtra("longitude", 0.0)

            days.clear()
            val weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"
            cityOnToolbarView.text = city
            fetchWeatherData(weatherUrl)
        }
    }

    companion object {
        const val API_KEY = "8c195f5286cded5d2d2d91cf76330fbb"
        val days: MutableList<Day> = mutableListOf()
        var latitude = 0.0
        var longitude = 0.0
        var city = ""
    }
}