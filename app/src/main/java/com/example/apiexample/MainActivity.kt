package com.example.apiexample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apiexample.api.UserApi
import com.example.apiexample.ui.theme.ApiExampleTheme
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : ComponentActivity() {
    private val weather = mutableStateOf(
        WeatherModel(
            id = 2,
            city = "PHILADELPHIA",
            high = 72,
            low = 53,
            current = 68,
            icon = "cloudy",
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApiExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(weather)
                }
            }
        }
    }
}

@Composable
fun MainScreen(weather: MutableState<WeatherModel>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Proxyman Weather API",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            )
        },
        content = { padding ->
            val weatherIconPair = weather.value.icon?.let { getWeatherIcon(it) }
            val weatherIcon = weatherIconPair
            val backgroundImg = when(weather.value.city?.uppercase()){
                "PHILADELPHIA" -> R.drawable.philadelphia
                "NEW YORK" -> R.drawable.newyork
                "DALLAS" -> R.drawable.dallas
                "SAN DIEGO" -> R.drawable.sandiego
                "SEATTLE" -> R.drawable.seattle
                else -> R.drawable.morning
            }

            Box() {
                Image(
                    painter = painterResource(id = backgroundImg),
                    contentDescription = null,
                    alpha = 0.2f,
                    contentScale = ContentScale.FillHeight
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    weather.value.city?.let { it ->
                        Text(text = it.uppercase(Locale.getDefault()), fontSize = 40.sp, textAlign = TextAlign.Center)
                    }
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weatherIcon?.let {
                            WeatherIcon(weatherIcon, weather.value.icon)
                        }
                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            weather.value.current?.let { it ->
                                Text(
                                    text = it.toString() + "\u00B0",
                                    fontSize = 40.sp
                                )
                            }
                            Row() {
                                weather.value.low?.let { it ->
                                    Text(
                                        text = "L:$it\u00B0",
                                        fontSize = 25.sp,
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }
                                weather.value.high?.let { it ->
                                    Text(
                                        text = "H:$it\u00B0",
                                        fontSize = 25.sp,
                                        modifier = Modifier.padding(5.dp)
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        onClick = {
                            val data = sendRequest(
                                weatherState = weather
                            )
                            weather.value = data.value
                        }
                    ) {
                        Text(text = "GET WEATHER", fontFamily= FontFamily.Monospace, fontSize = 20.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun getWeatherIcon(icon: String): Painter? {
    val id = when(icon.lowercase()){
        "sunny" -> R.drawable.sun
        "sun" -> R.drawable.sun
        "partly cloudy" -> R.drawable.partly_cloudy
        "cloudy" -> R.drawable.cloudy
        "rain" -> R.drawable.rain
        "snow" -> R.drawable.snow
        "high winds" -> R.drawable.high_winds
        "storms" -> R.drawable.storms
        else -> null
    }
    val iconId = id?.let { painterResource(it) }
    return(iconId)
}

@Composable
fun WeatherIcon(icon: Painter, iconDescription: String?) {
    Image(
        painter = icon,
        contentDescription = iconDescription,
        modifier = Modifier.size(150.dp)
            .padding(horizontal = 10.dp),
        contentScale = ContentScale.FillWidth
    )
}

fun sendRequest(
    weatherState: MutableState<WeatherModel>
): MutableState<WeatherModel> {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    val retrofit = Retrofit.Builder()
        .baseUrl("https://proxyman-weather-api.herokuapp.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api = retrofit.create(UserApi::class.java)

    val call = api.getWeather((1..5).shuffled().last())

    call!!.enqueue(object: Callback<WeatherModel?> {
        override fun onResponse(call: Call<WeatherModel?>, response: Response<WeatherModel?>) {
            if(response.isSuccessful) {
                Log.d("Main", "success!" + response.body().toString())
                weatherState.value = response.body()!!
            }
        }

        override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {
            Log.e("Main", "Failed mate " + t.message.toString())
        }
    })

    return weatherState
}

data class WeatherModel(
    var id: Int?,
    var city: String?,
    var high: Int?,
    var low: Int?,
    var current: Int?,
    var icon: String?
)

@Preview
@Composable
fun WeatherPreview() {
    MainScreen(remember {mutableStateOf(WeatherModel(1, "Philadelphia", 70, 50, 62, "sun"))})
}