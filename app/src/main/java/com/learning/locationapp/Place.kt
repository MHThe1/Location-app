package com.learning.locationapp

data class Place(
    val id: Int,
    val title: String,
    val lat: Double,
    val lon: Double,
    val image: String
) {
    val imageUrl: String
        get() = "https://labs.anontech.info/cse489/t3/$image"
}

data class UpdateResponse(
    val status: String
)