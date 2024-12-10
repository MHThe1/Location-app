package com.learning.locationapp

import retrofit2.http.GET

interface ApiService {
    @GET("api.php")
    suspend fun getPlaces(): List<Place>
}
