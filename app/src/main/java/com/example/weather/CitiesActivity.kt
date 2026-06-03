package com.example.weather

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapters.CitiesListAdapter
import com.example.weather.dataclasses.City
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.adapters.SearchCitiesListAdapter
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.example.weather.MainActivity.Companion.API_KEY
import androidx.core.graphics.drawable.toDrawable

class CitiesActivity : AppCompatActivity() {
    private val cities: MutableList<City> = mutableListOf()
    private val popularCities: MutableList<City> = mutableListOf()
    private lateinit var adapter: CitiesListAdapter
    private lateinit var popularCitiesAdapter: CitiesListAdapter
    private lateinit var citiesRecyclerView: RecyclerView
    private lateinit var popularCitiesRecyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var mapButton: ImageButton
    private lateinit var themeButton: ImageButton

    private lateinit var popularCitiesLayout: LinearLayout
    private lateinit var loader: ProgressBar
    private val popular_cities_coords = listOf(
        CityCoords("Москва", 55.75, 37.61),
        CityCoords("Лондон", 51.30, -0.12),
        CityCoords("Вашингтон", 38.90, -77.03),
        CityCoords("Пекин", 39.90, 116.40),
        CityCoords("Париж", 48.85, 2.35),
        CityCoords("Канберра", -35.28, 149.13),
        CityCoords("Бразилиа", -15.82, -47.92),
        CityCoords("Нью-Дели", 28.61, 77.20),
        CityCoords("Осло", 59.91, 10.75)
    )
    private val queue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cities)

        citiesRecyclerView = findViewById(R.id.cities)
        citiesRecyclerView.layoutManager = LinearLayoutManager(this)
        citiesRecyclerView.setHasFixedSize(true)
        adapter = CitiesListAdapter(this,cities, object :
            CitiesListAdapter.OnClickListener {
            override fun onClick(city: City, position: Int) {
                onClick(city)
            }
        })

        val swipeDeleteHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                deleteCity(position)
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (popularCitiesLayout.isVisible) 0  else super.getSwipeDirs(recyclerView, viewHolder)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchHelper.attachToRecyclerView(citiesRecyclerView)
        citiesRecyclerView.adapter = adapter

        popularCitiesLayout = findViewById(R.id.popular_cities_layout)

        loader = findViewById(R.id.loader)

        setCitiesList()

        addButton = findViewById(R.id.add)
        addButton.setOnClickListener {
            openSearchDialog()
        }

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        mapButton = findViewById(R.id.map_button)
        mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            addCityLauncher.launch(intent)
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

    private fun openSearchDialog() {
        val searchDialogView = layoutInflater.inflate(R.layout.search_city_dialog, null)

        val searchDialog = AlertDialog.Builder(this)
            .setView(searchDialogView)
            .create()
        searchDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val editText = searchDialogView.findViewById<EditText>(R.id.input)
        val cancelButton = searchDialogView.findViewById<Button>(R.id.cancel)
        val addButton = searchDialogView.findViewById<Button>(R.id.add)
        val recyclerView = searchDialogView.findViewById<RecyclerView>(R.id.search_cities)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)

        val citiesList = mutableListOf<String>()
        var selectedCity = ""

        val adapter = SearchCitiesListAdapter(citiesList, object : SearchCitiesListAdapter.OnClickListener {
            override fun onClick(city: String, position: Int) {
                selectedCity = city
            }
        })

        recyclerView!!.adapter = adapter

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val input = editText.text.toString()
                if (input.length >= 3) {
                    Thread {
                        val list = mutableListOf<String>()
                        val citiesFile = assets.open("cities.txt")
                        val reader = citiesFile.bufferedReader()

                        reader.forEachLine { line ->
                            if (line.startsWith(input)) {
                                list.add(line)
                            }
                        }
                        reader.close()

                        runOnUiThread {
                            citiesList.clear()
                            citiesList.addAll(list)
                            adapter.notifyDataSetChanged()

                        }
                    }.start()
                }
            }
        })

        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (editText.right - drawableEnd.bounds.width())) {
                    editText.text.clear()
                    return@setOnTouchListener true
                }
            }
            false
        }

        cancelButton.setOnClickListener {
            searchDialog.dismiss()
        }

        addButton.setOnClickListener {
            if (selectedCity.isNotEmpty()) {
                var dublicate = false
                for (i in cities){
                    if (selectedCity == i.city) {
                        Toast.makeText(this, "Этот город уже есть в списке", Toast.LENGTH_SHORT).show()
                        dublicate = true
                        break
                    }
                }
                if (!dublicate) {
                    addSelectedCity(selectedCity)
                    searchDialog.dismiss()
                }
            } else {
                Toast.makeText(this, "Пожалуйста, выберите город", Toast.LENGTH_SHORT).show()
            }
        }
        searchDialog.show()
    }

    private fun showLoader() {
        loader.visibility = View.VISIBLE
        citiesRecyclerView.visibility = View.GONE
        popularCitiesLayout.visibility = View.GONE
    }

    private fun showContent() {
        loader.visibility = View.GONE

        if (cities.isEmpty()) {
            citiesRecyclerView.visibility = View.GONE
            popularCitiesLayout.visibility = View.VISIBLE
        } else {
            citiesRecyclerView.visibility = View.VISIBLE
            popularCitiesLayout.visibility = View.GONE
        }
    }

    private fun setCitiesList() {
        showLoader()
        val citiesFromJson = loadJson()

        if (citiesFromJson.isNotEmpty()) {
            val loadedCities = mutableListOf<City>()
            var cnt = 0

            for (cityCoords in citiesFromJson) {
                val url = "https://api.openweathermap.org/data/2.5/weather?lat=${cityCoords.latitude}&lon=${cityCoords.longitude}&appid=$API_KEY&units=metric"
                val request = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        try {
                            val jsonResponse = JSONObject(response)
                            val temperature = jsonResponse.getJSONObject("main").getInt("temp")
                            val feelsLike = jsonResponse.getJSONObject("main").getInt("feels_like")
                            val weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main")

                            loadedCities.add(City(cityCoords.city, temperature, feelsLike, weather))
                        } catch (error: Exception) {
                            error.printStackTrace()
                        } finally {
                            cnt++
                            if (cnt == citiesFromJson.size) {
                                cities.clear()
                                cities.addAll(loadedCities)
                                adapter?.notifyDataSetChanged()
                                showContent()
                            }
                        }
                    },
                    { error ->
                        error.printStackTrace()
                    }
                )
                queue.add(request)
            }
        } else {
            val loadedCities = mutableListOf<City>()
            var cnt = 0

            popularCitiesRecyclerView = findViewById(R.id.popular_cities)
            popularCitiesAdapter = CitiesListAdapter(this,popularCities, object :
                CitiesListAdapter.OnClickListener {
                override fun onClick(city: City, position: Int) {
                    onClick(city)
                }
            })
            popularCitiesRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@CitiesActivity)
                setHasFixedSize(true)
                adapter = popularCitiesAdapter
            }

            for (cityCoords in popular_cities_coords) {
                val url = "https://api.openweathermap.org/data/2.5/weather?lat=${cityCoords.latitude}&lon=${cityCoords.longitude}&appid=$API_KEY&units=metric"
                val request = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        try {
                            val jsonResponse = JSONObject(response)
                            val temperature = jsonResponse.getJSONObject("main").getInt("temp")
                            val feelsLike = jsonResponse.getJSONObject("main").getInt("feels_like")
                            val weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main")

                            loadedCities.add(City(cityCoords.city, temperature, feelsLike, weather))
                        } catch (error: Exception) {
                            error.printStackTrace()
                        } finally {
                            cnt++
                            if (cnt == popular_cities_coords.size) {
                                popularCities.clear()
                                popularCities.addAll(loadedCities)
                                popularCitiesAdapter = CitiesListAdapter(this,popularCities, object :
                                    CitiesListAdapter.OnClickListener {
                                    override fun onClick(city: City, position: Int) {
                                        onClick(city)
                                    }
                                })
                                popularCitiesRecyclerView.apply {
                                    layoutManager = LinearLayoutManager(this@CitiesActivity)
                                    setHasFixedSize(true)
                                    adapter = popularCitiesAdapter
                                }
                                showContent()
                            }
                        }
                    },
                    { error ->
                        error.printStackTrace()
                    }
                )

                queue.add(request)
            }
        }
    }

    private fun addSelectedCity(city: String) {
        showLoader()
        val url = "https://api.openweathermap.org/geo/1.0/direct?q=$city&limit=1&appid=$API_KEY"
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONArray(response)
                    if (jsonResponse.length() > 0) {
                        val latitude = jsonResponse.getJSONObject(0).getDouble("lat")
                        val longitude = jsonResponse.getJSONObject(0).getDouble("lon")

                        val citiesJson = loadJson()
                        citiesJson.add(CityCoords(city, latitude, longitude))
                        saveJson(citiesJson)

                        setCitiesList()
                    } else {
                        loader.visibility = View.GONE
                    }
                } catch (error: Exception) {
                    loader.visibility = View.GONE
                    error.printStackTrace()
                }
            },
            { error ->
                loader.visibility = View.GONE
                error.printStackTrace()
            }
        )

        queue.add(request)
    }

    private fun loadJson(): MutableList<CityCoords> {
        val file = File(filesDir,"added_cities.json")
        if (!file.exists()){
            file.createNewFile()
        }
        val jsonString = file.readText()
        if (file.length() == 0L) {
            return mutableListOf()
        }
        return Json.decodeFromString(jsonString)
    }

    private fun saveJson(cities: List<CityCoords>) {
        val file = File(filesDir,"added_cities.json")
        if (!file.exists()){
            file.createNewFile()
        }
        val jsonString = Json.encodeToString(cities)
        file.writeText(jsonString)
        setCitiesList()
    }

    private fun deleteCity(position: Int) {
        val citiesFromJson = loadJson()
        val cityToDelete = cities[position]
        val updatedCities = citiesFromJson.filter {
            it.city != cityToDelete.city
        }.toMutableList()
        saveJson(updatedCities)
        cities.removeAt(position)
        adapter.notifyItemRemoved(position)
        if (cities.isEmpty()) {
            citiesRecyclerView.visibility = View.GONE
            setCitiesList()
        }
    }

    private fun onClick(city: City) {
        val url = "https://api.openweathermap.org/geo/1.0/direct?q=${city.city}&limit=1&appid=$API_KEY"
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONArray(response)
                    val latitude = jsonResponse.getJSONObject(0).getDouble("lat")
                    val longitude = jsonResponse.getJSONObject(0).getDouble("lon")
                    val intent = Intent().apply {
                        putExtra("city", city.city)
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        queue.add(request)
    }

    private val addCityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val city = data?.getStringExtra("city") ?: return@registerForActivityResult
            var dublicate = false
            for (i in cities){
                if (city == i.city) {
                    Toast.makeText(this, "Этот город уже есть в списке", Toast.LENGTH_SHORT).show()
                    dublicate = true
                    break
                }
            }
            if (!dublicate) {
                addSelectedCity(city)
            }
        }
    }
}

@Serializable
data class CityCoords(
    val city: String,
    val latitude: Double,
    val longitude: Double
)