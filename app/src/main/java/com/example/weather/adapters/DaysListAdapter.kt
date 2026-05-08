package com.example.weather.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.dataclasses.Day

class DaysListAdapter(private val context: Context, private val list: List<Day>, private val onClickListener : OnClickListener) : RecyclerView.Adapter<DaysListAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onClick(day: Day, position: Int)
    }

    //private val onClickListener: OnClickListener
    //private val days: List<Day>

    /*init {
        this.days = list
        this.onClickListener = onClickListener
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.days_element, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = list[position]
        holder.dateView.text = day.date
        holder.temperatureView.text = day.mean_temperature.toString()
        holder.feelsLikeView.text = day.mean_feels_like.toString()

        val hoursAdapter = HoursListAdapter(day.hours_list)
        holder.hoursListView.layoutManager =  LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        holder.hoursListView.adapter = hoursAdapter

        holder.itemView.setOnClickListener{onClickListener.onClick(day, position)}

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val dateView: TextView = itemView.findViewById(R.id.date)
        val temperatureView : TextView = itemView.findViewById(R.id.temperature)
        val feelsLikeView : TextView = itemView.findViewById(R.id.feels_like)
        val hoursListView : RecyclerView = itemView.findViewById(R.id.hours)
    }
}