package com.example.weather.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.dataclasses.City
import com.example.weather.dataclasses.Day

class CitiesListAdapter(private val context: Context, private val list: List<City>, private val onClickListener : OnClickListener) : RecyclerView.Adapter<CitiesListAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(city: City, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cities_element, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = list[position]
        holder.cityView.text = city.city
        holder.temperatureView.text = city.temperature.toString()
        holder.feelsLikeView.text = city.feels_like.toString()

        val weather = city.weather
        val weatherImage = when (weather) {
            "Clear" -> R.drawable.clear
            "Clouds" -> R.drawable.cloudy
            "Rain" -> R.drawable.rain
            "Thunderstorm" -> R.drawable.thunder
            else -> null
        }
        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, weatherImage!!))
        holder.itemView.setOnClickListener{onClickListener.onClick(city, position)}

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val cityView: TextView = itemView.findViewById(R.id.city)
        val temperatureView : TextView = itemView.findViewById(R.id.temperature)
        val feelsLikeView : TextView = itemView.findViewById(R.id.feels_like)
        val imageView: ImageView = itemView.findViewById(R.id.weather)
    }
}