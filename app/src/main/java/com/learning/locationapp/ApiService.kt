package com.learning.locationapp

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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

    @Multipart
    @POST("api.php")
    suspend fun createPlace(
        @Part("title") title: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody,
        @Part image: MultipartBody.Part
    ): CreateResponse

}
