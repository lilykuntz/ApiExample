package com.example.apiexample.api


import com.example.apiexample.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

public interface UserApi {
    @Headers(
        "Accept: application/json"
    )
    @GET("api/v1/weather/{id}")
    abstract fun getWeather(@Path("id") id: Int): Call<WeatherModel>?
}