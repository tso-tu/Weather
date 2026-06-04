package com.example.weather

import CityMarker
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import androidx.core.graphics.createBitmap

class MapMarkerView(private val context: Context) {
    private val addedMarkers = mutableMapOf<String, CityMarker>()
    private var onMarkerClickListener: ((CityMarker) -> Unit)? = null

    fun createCustomMarker(
        temperature: Int,
        feelsLike: Int,
        weather: String
    ): Bitmap {
        val markerView = LayoutInflater.from(context)
            .inflate(R.layout.marker_view, null)

        val temperatureView = markerView.findViewById<TextView>(R.id.temperature)
        val feelsLikeView = markerView.findViewById<TextView>(R.id.feels_like)
        val weatherView = markerView.findViewById<ImageView>(R.id.weather)

        temperatureView.text = "$temperature°"
        feelsLikeView.text = "$feelsLike°"

        val weatherImg = when (weather.lowercase()) {
            "clear" -> R.drawable.clear
            "clouds" -> R.drawable.cloudy
            "rain" -> R.drawable.rain
            "thunderstorm" -> R.drawable.thunder
            "drizzle" -> R.drawable.rain
            "snow" -> R.drawable.rain
            else -> R.drawable.clear
        }
        weatherView.setImageDrawable(ContextCompat.getDrawable(context, weatherImg))

        markerView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = createBitmap(markerView.measuredWidth, markerView.measuredHeight)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)

        return bitmap
    }

    fun addMarkersToMap(
        map: MapLibreMap,
        markers: List<CityMarker>,
        onMarkerClick: ((CityMarker) -> Unit)? = null
    ) {
        this.onMarkerClickListener = onMarkerClick

        map.getStyle { style ->
            markers.forEach { marker ->
                val markerId = "custom_marker_${marker.name}"

                if (!addedMarkers.containsKey(markerId)) {
                    val bitmap = createCustomMarker(
                        marker.temperature,
                        marker.feelsLike,
                        marker.weather
                    )

                    style.addImage(markerId, bitmap)

                    val sourceId = "source_${marker.name}"
                    val source = GeoJsonSource(
                        sourceId,
                        Feature.fromGeometry(Point.fromLngLat(marker.longitude, marker.latitude))
                    )
                    style.addSource(source)

                    val layerId = "layer_${marker.name}"
                    val layer = SymbolLayer(layerId, sourceId)
                    layer.setProperties(
                        PropertyFactory.iconImage(markerId),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconSize(1.0f)
                    )
                    style.addLayer(layer)

                    addedMarkers[markerId] = marker
                }
            }
        }
    }
}
