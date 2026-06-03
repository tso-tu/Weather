data class CityMarker(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    var temperature: Int,
    var feelsLike: Int,
    var weather: String
)