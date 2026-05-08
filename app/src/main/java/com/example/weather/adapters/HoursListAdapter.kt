package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.dataclasses.Hour

class HoursListAdapter(private val list: List<Hour>) : RecyclerView.Adapter<HoursListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hours_element, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hour = list[position]
        holder.hourView.text = hour.hour
        holder.temperatureView.text = hour.temperature.toString()

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hourView: TextView = itemView.findViewById(R.id.hour)
        //val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val temperatureView : TextView = itemView.findViewById(R.id.temperature)

    }
}