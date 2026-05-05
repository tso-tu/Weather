package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.dataclasses.Day

class DaysListAdapter(private val list: List<Day>) : RecyclerView.Adapter<DaysListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.days_element, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = list[position]
        //holder.textView.text = day.text
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val imageView: ImageView = itemView.findViewById(R.id.imageview)
        //val textView: TextView = itemView.findViewById(R.id.textView)
    }
}