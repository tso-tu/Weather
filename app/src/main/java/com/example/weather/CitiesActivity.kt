package com.example.weather

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.adapters.CitiesListAdapter
import com.example.weather.dataclasses.City
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.MainActivity.Companion.days
import com.example.weather.adapters.DaysListAdapter
import com.example.weather.adapters.SearchCitiesListAdapter
import com.example.weather.dataclasses.Day
import com.example.weather.dataclasses.Hour
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CitiesActivity : AppCompatActivity() {
    private val cities: MutableList<City> = mutableListOf()
    private var adapter: CitiesListAdapter? = null
    var recyclerView: RecyclerView? = null

    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cities)

        setCitiesList()
        recyclerView = findViewById(R.id.cities)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        adapter = CitiesListAdapter(this,cities, object :
            CitiesListAdapter.OnClickListener {
            override fun onClick(city: City, position: Int) {
                onClick(city)
            }
        })
        recyclerView!!.adapter = adapter

        setCitiesList()

        addButton = findViewById(R.id.add)
        addButton.setOnClickListener {
            openSearchDialog()
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
        searchDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val inputEditText = searchDialogView.findViewById<EditText>(R.id.input)
        val cancelButton = searchDialogView.findViewById<Button>(R.id.cancel)
        val addButton = searchDialogView.findViewById<Button>(R.id.add)
        val recyclerView = searchDialogView.findViewById<RecyclerView>(R.id.search_cities)

        recyclerView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.setHasFixedSize(true)

        val cities_list = mutableListOf<String>()
        var selected_city = ""

        var adapter = SearchCitiesListAdapter(cities_list, object : SearchCitiesListAdapter.OnClickListener {
            override fun onClick(city: String, position: Int) {
                selected_city = city
            }
        })

        recyclerView!!.adapter = adapter

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val input = inputEditText.text.toString()
                if (input.length >= 3) {
                    Thread {
                        val list = mutableListOf<String>()
                        val citiesFile = assets.open("cities.txt")
                        val reader = citiesFile.bufferedReader()

                        reader.forEachLine { line ->
                            if (line.startsWith(input)) {
                                list.add(line)
                                println(line)
                            }
                        }
                        reader.close()

                        runOnUiThread {
                            cities_list.clear()
                            cities_list.addAll(list)

                            adapter.notifyDataSetChanged()

                            println(cities_list.size)
                        }
                    }.start()
                }
            }
        })

        cancelButton.setOnClickListener {
            searchDialog.dismiss()
        }

        addButton.setOnClickListener {
            if (selected_city.isNotEmpty()) {
                addSelectedCity(selected_city)
                searchDialog.dismiss()
            } else {
                Toast.makeText(this, "Пожалуйста, выберите город", Toast.LENGTH_SHORT).show()
            }
        }
        searchDialog.show()
    }

    private fun setCitiesList(){

    }

    private fun addSelectedCity(city: String){
        val url = "https://nominatim.openstreetmap.org/search?q=$city&format=jsonv2"
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONArray(response)
                    val latitude = jsonResponse.getJSONObject(0).getJSONObject("lat")
                    val longitude = jsonResponse.getJSONObject(0).getJSONObject("lon")

                } catch (error: Exception) {
                    error.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        queue.add(request)
    }

    private fun onClick(city: City) {
        //val intent = Intent(this, DayActivity::class.java)
        //intent.putExtra("index",)
        //startActivity(intent)
    }
}