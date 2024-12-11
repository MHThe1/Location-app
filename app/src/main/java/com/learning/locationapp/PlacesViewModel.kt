package com.learning.locationapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException

class PlacesViewModel : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        fetchPlaces()
    }

    fun fetchPlaces() {
        viewModelScope.launch {
            try {
                val placesList = RetrofitInstance.api.getPlaces()
                _places.value = placesList
            } catch (e: IOException) {
                // Handle network error
            } catch (e: HttpException) {
                // Handle HTTP error
            }
        }
    }


    fun updatePlace(place: Place) {
        viewModelScope.launch {
            _loading.value = true

            try {
                val response = RetrofitInstance.api.updatePlace(
                    id = place.id,
                    title = place.title,
                    lat = place.lat,
                    lon = place.lon,
                    image = place.image // Optional image parameter
                )

                if (response.status == "success") {
                    _successMessage.value = "Place updated successfully!"
                    fetchPlaces()
                } else {
                    _successMessage.value = "Failed to update the place."
                }
            } catch (e: IOException) {
                _successMessage.value = "Network error. Please try again."
            } catch (e: HttpException) {
                _successMessage.value = "Server error. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }
}
