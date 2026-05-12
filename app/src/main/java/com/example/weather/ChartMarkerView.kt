import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.weather.DayActivity
import com.example.weather.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class ChartMarkerView(context: Context) : MarkerView(context, R.layout.marker_view) {
    private val temperatureView: TextView = findViewById(R.id.temperature)
    private val feels_likeView: TextView = findViewById(R.id.feels_like)
    private val weatherView: ImageView = findViewById(R.id.weather)
    private val activity = context as DayActivity
    private val day = activity.day

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        temperatureView.text = String.format("%.0f", e?.y ?: 0.0)
        feels_likeView.text = day.hours_list[e!!.x.toInt()].feels_like.toString()
        val weatherImg = when (day.hours_list[e!!.x.toInt()].weather) {
            "Clear" -> R.drawable.clear
            "Clouds" -> R.drawable.cloudy
            "Rain" -> R.drawable.rain
            "Thunderstorm" -> R.drawable.thunder
            else -> null
        }
        weatherView.setImageDrawable(ContextCompat.getDrawable(context, weatherImg!!))

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}