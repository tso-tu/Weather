package com.example.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.current_temperature)

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


        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=52.43&lon=41.26&units=metric&appid=$API_KEY"
                fetchWeatherData(weatherUrl)
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

                    println("Weather API URL: $weatherUrl")

                    fetchWeatherData(weatherUrl)
                } else {
                    textView.text = "Could not get location."
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val main = jsonResponse.getJSONObject("main")
                    val temperature = main.getString("temp")
                    val city = jsonResponse.getString("name")

                    textView.text = "$temperature°C in $city"
                } catch (e: Exception) {
                    textView.text = "Error parsing data!"
                    e.printStackTrace()
                }
            },
            { error ->
                textView.text = "Error fetching weather!"
                error.printStackTrace()
            })

        queue.add(request)
    }

    companion object {
        const val API_KEY = "8c195f5286cded5d2d2d91cf76330fbb"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}