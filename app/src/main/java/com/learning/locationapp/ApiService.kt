package com.learning.locationapp

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT
import java.io.File

interface ApiService {
    @GET("api.php")
    suspend fun getPlaces(): List<Place>

    @FormUrlEncoded
    @PUT("api.php")
    suspend fun updatePlace(
        @Field("id") id: Int,
        @Field("title") title: String,
        @Field("lat") lat: Double,
        @Field("lon") lon: Double,
        @Field("image") image: String
    ): UpdateResponse
}
