package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.adapters.DaysListAdapter
import com.example.weather.dataclasses.Day
import com.example.weather.dataclasses.Hour
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var currentDateView: TextView
    private lateinit var currentTemperatureView: TextView
    private lateinit var currentWeatherView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var adapter: DaysListAdapter? = null
    var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val formater = SimpleDateFormat("d MMMM, EE")
        val currentDate = formater.format(Date())

        currentDateView = findViewById(R.id.current_date)
        currentDateView.text = currentDate
        currentTemperatureView = findViewById(R.id.current_temperature)
        currentWeatherView = findViewById(R.id.current_weather)


        recyclerView = findViewById(R.id.days)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        adapter = DaysListAdapter(this,days.slice(1 until days.size), object :
            DaysListAdapter.OnClickListener {
            override fun onClick(day: Day, position: Int) {
                onClick(position)
            }
        })

        recyclerView!!.adapter = adapter
        recyclerView!!.setNestedScrollingEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchLocation()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"
                    fetchWeatherData(weatherUrl)
                } else {
                    println("Локация не определяется")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Локация не определяется", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val list = jsonResponse.getJSONArray("list")

                    var current_date = list.getJSONObject(0).getString("dt_txt").split("\\s+".toRegex())[0]

                    val hours_list : MutableList<Hour> = mutableListOf()
                    for (i in 0 until list.length()){
                        if (current_date == list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[0]) {
                            val main = list.getJSONObject(i).getJSONObject("main")

                            val temperature = main.getInt("temp")
                            val weather = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("main")
                            val feels_like = main.getInt("feels_like")

                            val wind = list.getJSONObject(i).getJSONObject("wind").getString("speed")
                            val humidity = main.getInt("humidity")

                            var precipitation = 0
                            try {
                                precipitation = list.getJSONObject(i).getJSONObject("rain").getInt("1h")
                            } catch (e: JSONException) {
                                println("Осадков нет")
                            }
                            hours_list.add(Hour(list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[1], weather, temperature, feels_like, wind, humidity, precipitation))
                        }
                        else {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
                            val outputFormat = SimpleDateFormat("EE")
                            val day_of_week = outputFormat.format(inputFormat.parse(current_date))

                            var mean_temperature = 0
                            var mean_feels_like = 0
                            var cnt = 0
                            for (hour in hours_list){
                                cnt++
                                mean_temperature += hour.temperature
                                mean_feels_like += hour.feels_like
                            }
                            mean_temperature /= cnt
                            mean_feels_like /= cnt

                            val weather_list : MutableList<String> = mutableListOf()
                            for (hour in hours_list) {
                                weather_list.add(hour.weather)
                            }
                            val mean_weather = weather_list.groupingBy { it }.eachCount().maxBy { it.value }.key

                            days.add(Day(current_date, day_of_week, hours_list, mean_temperature, mean_feels_like, mean_weather))
                            current_date = list.getJSONObject(i).getString("dt_txt").split("\\s+".toRegex())[0]
                            println(hours_list)
                            hours_list.clear()
                        }
                    }
                    setCurrentWeatherData()
                    adapter = DaysListAdapter(this, days.slice(1 until days.size),object : DaysListAdapter.OnClickListener {
                        override fun onClick(day: Day, position: Int) {
                            onClick(position)
                        }
                    })
                    recyclerView!!.setAdapter(adapter)

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
        val weather = days[0].hours_list[0].weather
        val temperature = days[0].hours_list[0].temperature
        val feels_like = days[0].hours_list[0].feels_like
        currentTemperatureView.text = "$temperature°C"
        currentWeatherView.text = "$weather, ощущается как $feels_like"

    }

    private fun onClick(index: Int) {
        val intent = Intent(this, DayActivity::class.java)
        intent.putExtra("index", index+3)
        startActivity(intent)
    }

    companion object {
        const val API_KEY = "8c195f5286cded5d2d2d91cf76330fbb"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        val days: MutableList<Day> = mutableListOf()
    }
}