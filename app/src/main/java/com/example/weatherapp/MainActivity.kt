package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // Use View Binding
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root) // Use binding.root to set the content view

        // Handle SearchView query
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // Fetch weather data for the entered city
                    fetchWeatherData(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // Function to fetch weather data for the entered city
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun fetchWeatherData(city: String) {
        val apiKey = "f3549eaf34e9b13bbd59e7bf288d74b6"
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)

                    // Get weather description
                    val weatherArray = jsonObject.getJSONArray("weather")
                    val weatherDescription = weatherArray.getJSONObject(0).getString("description")
                    val weatherIcon = weatherArray.getJSONObject(0).getString("icon")

                    // Get temperature (Kelvin to Celsius conversion)
                    val mainObject = jsonObject.getJSONObject("main")
                    val temperature = mainObject.getDouble("temp") - 273.15
                    val formattedTemperature = String.format("%.0f", temperature)

                    val humidity = mainObject.getInt("humidity")
                    val pressure = mainObject.getInt("pressure")
                    val seaLevel = mainObject.optInt("sea_level", -1)

                    val feelsLike = mainObject.getDouble("feels_like") - 273.15
                    val tempMin = mainObject.getDouble("temp_min") - 273.15
                    val tempMax = mainObject.getDouble("temp_max") - 273.15
                    val windObject = jsonObject.getJSONObject("wind")
                    val windSpeed = windObject.getDouble("speed")

                    // Get sunrise and sunset as long values (timestamps)
                    val sysObject = jsonObject.getJSONObject("sys")
                    val sunrise = sysObject.getLong("sunrise")
                    val sunset = sysObject.getLong("sunset")

                    // Convert sunrise and sunset timestamps to readable time
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val sunriseTime = dateFormat.format(Date(sunrise * 1000))
                    val sunsetTime = dateFormat.format(Date(sunset * 1000))

                    // Format temperature values to avoid decimals
                    val formattedFeelsLike = String.format("%.0f", feelsLike)
                    val formattedTempMin = String.format("%.0f", tempMin)
                    val formattedTempMax = String.format("%.0f", tempMax)

                    // Update the views using View Binding
                    binding.weatherTextView.text = "\uD83C\uDF0F $city"
                    binding.temp.text = "$formattedTemperature°C"
                    binding.view.text = weatherDescription
                    binding.all.text = "Humidity: $humidity%\n" +
                            "Sea Level: ${if (seaLevel == -1) "N/A" else "$seaLevel hPa"}\n" +
                            "Feels Like: $formattedFeelsLike°C\nTemp Min: $formattedTempMin°C\nTemp Max: $formattedTempMax°C\n" +
                            "Wind Speed: $windSpeed m/s\nSunrise: $sunriseTime\nSunset: $sunsetTime"

                    // Load the weather icon into the ImageView using Glide
                    val iconUrl = "https://openweathermap.org/img/wn/$weatherIcon@4x.png"
                    Glide.with(this)
                        .load(iconUrl)
                        .into(binding.weatherImageView)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: Could not parse response", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Location not exist", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(stringRequest)
    }
}
