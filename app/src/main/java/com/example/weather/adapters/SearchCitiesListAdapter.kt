package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R

class SearchCitiesListAdapter(private val list: List<String>, private val onClickListener : OnClickListener) : RecyclerView.Adapter<SearchCitiesListAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(city: String, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_cities_element, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cityView.text = list[position]
        holder.itemView.setOnClickListener{onClickListener.onClick(list[position], position)}
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cityView: TextView = itemView.findViewById(R.id.city)
    }
}