package com.bright.sunriseset

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import com.bright.sunriseset.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class PlanetInfoActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private lateinit var  languageList: ArrayList<Language>
    private var isTouched: Boolean = false

    private lateinit var binding: ActivityMainBinding

    /**
     * Called when the activity is first created. This function initializes the activity, inflates the layout,
     * retrieves the current time, and asynchronously fetches sunrise and sunset times from an API.
     * The fetched times are then dynamically localized and displayed in Chinese on TextViews.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get language list
        languageList = MainViewModel().loadLanguages()
        binding.spinner.adapter = SpinnerAdapter(this, languageList)
        binding.spinner.setOnTouchListener(this)
        binding.spinner.onItemSelectedListener = this

        // Get the current time
//        val currentTime = LocalDateTime.now()
//        loadCurrentTime()
        loadCurrentTime()

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        isTouched = true
        return false
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!isTouched) return
        val language = languageList[position]
        changeLanguage(language.code)
        isTouched = false

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun loadCurrentTime(){
        // Asynchronously fetch sunrise and sunset times
        GlobalScope.launch(Dispatchers.Main) {
            val sunriseDeferred = async(Dispatchers.IO) { fetchTime("sunrise") }
            val sunsetDeferred = async(Dispatchers.IO) { fetchTime("sunset") }

            // Await the results of asynchronous tasks
            val sunriseTime = sunriseDeferred.await()
            val sunsetTime = sunsetDeferred.await()

            // If both sunrise and sunset times are available, localize and display them in Chinese
            if (sunriseTime != null && sunsetTime != null) {
                // Localize sunrise and sunset times
                val localizedSunrise = getLocalizedTime(sunriseTime, this@PlanetInfoActivity)
                val localizedSunset = getLocalizedTime(sunsetTime, this@PlanetInfoActivity)

                // Display localized times on TextViews
                binding.textViewSunrise.text =
                    "${getString(R.string.SunriseTime)} $localizedSunrise"
                binding.textViewSunset.text =
                    "${getString(R.string.SunsetTime)} $localizedSunset"
            }
        }
    }

    /**
     * Retrieves a localized time string based on the user's preferred language.
     *
     * @param time The LocalDateTime to be formatted.
     * @param context The application context to access resources and preferences.
     * @return A string representation of the localized time.
     */
    private fun getLocalizedTime(time: LocalDateTime, context: Context): String {
        // Retrieve the user's preferred language from the device settings
        println(binding.spinner.selectedItem)
        val userPreferredLanguage = Locale.getDefault().language

        // Create a SimpleDateFormat with the user's preferred language
        val sdf = SimpleDateFormat("hh:mm a", Locale(userPreferredLanguage))

        // Format the LocalDateTime into a string using the specified SimpleDateFormat
        return sdf.format(
            time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }


    // Coroutine function to fetch sunrise or sunset time from the Sunrise-Sunset API
    private suspend fun fetchTime(type: String): LocalDateTime? {
        return try {
            val apiUrl =
                URL("https://api.sunrise-sunset.org/json?lat=37.7749&lng=-122.4194&formatted=0")
            val urlConnection: HttpURLConnection = apiUrl.openConnection() as HttpURLConnection
            try {
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                val jsonResponse = JSONObject(response.toString())
                val timeUTC = jsonResponse.getJSONObject("results").getString(type)
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                val dateTime = formatter.parse(timeUTC)
                LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault())
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun changeLanguage(code: String) {
        // return the resource's current configuration.
        val config = resources.configuration
        //Construct a locale from language and country.
        val locale = Locale(code)
        Locale.setDefault(locale)
        config.setLocale(locale)
        /* Return the current display metrics that are in effect for this resource object
        * A structure describing general information about a display, such as its size, density, and font scaling.*/
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }
}