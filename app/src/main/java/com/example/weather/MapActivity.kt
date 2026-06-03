package com.example.weather

import CityMarker
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.Semaphore
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var cityView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var myGeoButton: Button
    private lateinit var mapView: MapView

    private lateinit var maplibreMap: MapLibreMap
    private var isMapReady = false

    private lateinit var mapMarkerView: MapMarkerView
    private val citiesList = mutableListOf<CityMarker>()
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private val cache = mutableMapOf<String, UpdatedWeatherData>()
    private val loadingCities = mutableSetOf<String>()
    private var updateJob: Job? = null
    private var currentVisibleBounds: LatLngBounds? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_map)

        mapMarkerView = MapMarkerView(this)
        cityView = findViewById(R.id.city)

        cityView.text = MainActivity.city
        currentLatitude = MainActivity.latitude
        currentLongitude = MainActivity.longitude

        myGeoButton = findViewById(R.id.my_geo)
        myGeoButton.setOnClickListener {
            maplibreMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLatitude, currentLongitude),
                    5.0
                )
            )
        }

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        loadCitiesFromGeoJson()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadCitiesFromGeoJson() {
        try {
            val inputStream: InputStream = assets.open("citiesgeojson.geojson")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val jsonString = String(buffer, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)
            val featuresArray = jsonObject.getJSONArray("features")

            for (i in 0 until featuresArray.length()) {
                try {
                    val feature = featuresArray.getJSONObject(i)
                    val coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates")

                    val longitude = coordinates.getDouble(0)
                    val latitude = coordinates.getDouble(1) //в этом формате координаты хранятся наоборот

                    val id = feature.getJSONObject("properties").getString("id")
                    val name = feature.getJSONObject("properties").getString("name")
                    val temperature = feature.getJSONObject("properties").getInt("temperature")
                    val feelsLike = feature.getJSONObject("properties").getInt("feels_like")
                    val weather = feature.getJSONObject("properties").getString("weather")

                    citiesList.add(
                        CityMarker(
                            id = id,
                            name = name,
                            latitude = latitude,
                            longitude = longitude,
                            temperature = temperature,
                            feelsLike = feelsLike,
                            weather = weather
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        maplibreMap = map
        maplibreMap.setMinZoomPreference(5.0)
        maplibreMap.setMaxZoomPreference(12.0)
        maplibreMap.setStyle(
            Style.Builder().fromUri("https://api.maptiler.com/maps/basic-v2/style.json?key=nTSEuIP0S43AeEqVPaZy")
        ) {
            isMapReady = true
            maplibreMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLatitude, currentLongitude),
                    5.0
                )
            )
            maplibreMap.addOnCameraIdleListener {
                updateWeather()
            }
            updateWeather()
            setMarkerClickListener()
        }
    }

    private fun setMarkerClickListener() {
        maplibreMap.addOnMapClickListener { latLng ->
            var clickedMarker: CityMarker? = null
            var minPixelDistance = 100.0

            val clickPoint = maplibreMap.projection.toScreenLocation(latLng)

            val visibleBounds = currentVisibleBounds
            if (visibleBounds != null) {
                val visibleCities = citiesList.filter { city ->
                    visibleBounds.contains(LatLng(city.latitude, city.longitude))
                }

                if (visibleCities.isNotEmpty()) {
                    visibleCities.forEach { city ->
                        val markerPoint = maplibreMap.projection.toScreenLocation(
                            LatLng(city.latitude, city.longitude)
                        )
                        val dx = clickPoint.x - markerPoint.x
                        val dy = clickPoint.y - markerPoint.y
                        val pixelDistance = sqrt((dx * dx + dy * dy).toDouble())

                        if (pixelDistance < minPixelDistance) {
                            minPixelDistance = pixelDistance
                            val cached = cache[city.id]
                            clickedMarker = CityMarker(
                                id = city.id,
                                name = city.name,
                                latitude = city.latitude,
                                longitude = city.longitude,
                                temperature = cached?.temperature ?: city.temperature,
                                feelsLike = cached?.feelsLike ?: city.feelsLike,
                                weather = cached?.weather ?: city.weather
                            )
                        }
                    }
                }
            }

            if (clickedMarker != null) {
                val intent = Intent().apply {
                    putExtra("city", clickedMarker.name)
                    putExtra("latitude", clickedMarker.latitude)
                    putExtra("longitude", clickedMarker.longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
                return@addOnMapClickListener true
            }
            false
        }
    }

    private fun updateWeather() {
        if (!isMapReady || citiesList.isEmpty()) return

        try {
            val visibleBounds = maplibreMap.projection.visibleRegion.latLngBounds
            if (currentVisibleBounds != null) {
                val latDiff = abs(currentVisibleBounds!!.latitudeNorth - visibleBounds.latitudeNorth) + abs(currentVisibleBounds!!.latitudeSouth - visibleBounds.latitudeSouth)
                val lonDiff = abs(currentVisibleBounds!!.longitudeEast - visibleBounds.longitudeEast) + abs(currentVisibleBounds!!.longitudeWest - visibleBounds.longitudeWest)

                if (latDiff < 0.1 && lonDiff < 0.1) return
            }

            currentVisibleBounds = visibleBounds

            val visibleCities = citiesList.filter { city ->
                visibleBounds.contains(LatLng(city.latitude, city.longitude))
            }

            if (visibleCities.isNotEmpty()) {
                lifecycleScope.launch {
                    loadWeatherForCities(visibleCities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadWeatherForCities(cities: List<CityMarker>) {
        val citiesToUpdate = cities.filter { city ->
            val cached = cache[city.id]
            cached == null || !cached.isNotNeedToUpdate()
        }.distinctBy { it.id }

        if (citiesToUpdate.isEmpty()) return
        citiesToUpdate.forEach { loadingCities.add(it.id) }

        try {
            val updates = fetchWeather(citiesToUpdate)
            updates.forEach { update ->
                cache[update.id] = update

                val city = citiesList.find { it.id == update.id }
                city?.let {
                    it.temperature = update.temperature
                    it.feelsLike = update.feelsLike
                    it.weather = update.weather
                }
            }

            val visibleBounds = currentVisibleBounds
            if (maplibreMap.style != null && visibleBounds != null) {
                val visibleCities = citiesList.filter { city ->
                    visibleBounds.contains(LatLng(city.latitude, city.longitude))
                }
                if (visibleCities.isEmpty()) return
                println(visibleCities.size)

                val markers = visibleCities.map { city ->
                    val cached = cache[city.id]
                    CityMarker(
                        id = city.id,
                        name = city.name,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        temperature = cached?.temperature ?: city.temperature,
                        feelsLike = cached?.feelsLike ?: city.feelsLike,
                        weather = cached?.weather ?: city.weather
                    )
                }

                mapMarkerView.addMarkersToMap(
                    map = maplibreMap,
                    markers = markers
                )
                setMarkerClickListener()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            citiesToUpdate.forEach { loadingCities.remove(it.id) }
        }
    }

    private suspend fun fetchWeather(cities: List<CityMarker>): List<UpdatedWeatherData> =
        withContext(Dispatchers.IO) {
            val semaphore = Semaphore(10)

            return@withContext cities.map { city ->
                async {
                    semaphore.acquire()
                    try {
                        val url = "https://api.openweathermap.org/data/2.5/weather?id=${city.id}&units=metric&appid=${MainActivity.Companion.API_KEY}&lang=ru"
                        val request = Request.Builder().url(url).build()

                        val okHttpClient = OkHttpClient.Builder().build()
                        val response = okHttpClient.newCall(request).execute()

                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()

                            val jsonObject = JSONObject(responseBody)
                            val main = jsonObject.getJSONObject("main")
                            val temperature = main.getDouble("temp").toInt()
                            val feelsLike = main.getDouble("feels_like").toInt()
                            val weatherArray = jsonObject.getJSONArray("weather")
                            val weatherObject = weatherArray.getJSONObject(0)
                            val weather = weatherObject.getString("main")

                            UpdatedWeatherData(
                                id = city.id,
                                temperature = temperature,
                                feelsLike = feelsLike,
                                weather = weather,
                                timestamp = System.currentTimeMillis()
                            )
                        } else null
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll().filterNotNull()
        }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        updateJob?.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        updateJob?.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}

data class UpdatedWeatherData(
    val id: String,
    val temperature: Int,
    val feelsLike: Int,
    val weather: String,
    val timestamp: Long
) {
    fun isNotNeedToUpdate(): Boolean =
        System.currentTimeMillis() - timestamp < (30 * 60 * 1000)
}
