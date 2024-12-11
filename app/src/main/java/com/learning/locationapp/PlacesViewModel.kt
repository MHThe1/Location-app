package com.learning.locationapp

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class PlacesViewModel : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _createEntityState = MutableStateFlow<ApiState?>(null)
    val createEntityState: StateFlow<ApiState?> = _createEntityState

    init {
        fetchPlaces()
    }

    fun fetchPlaces() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val placesList = RetrofitInstance.api.getPlaces()
                _places.value = placesList
                _errorMessage.value = null
            } catch (e: IOException) {
                _errorMessage.value = "Network error. Please check your connection."
            } catch (e: HttpException) {
                _errorMessage.value = "Server error: ${e.code()}. Please try again later."
            } finally {
                _loading.value = false
            }
        }
    }

    fun createPlace(title: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _createEntityState.value = ApiState.Loading

            try {
                // Call API to create a place (no image handling)
                val response = RetrofitInstance.api.createPlace(
                    title = title,
                    lat = lat,
                    lon = lon
                )

                // Check if the response contains a valid id
                if (response.id > 0) {
                    _createEntityState.value = ApiState.Success("Entity created successfully!")
                    fetchPlaces() // Refresh the list after creation
                } else {
                    _createEntityState.value = ApiState.Error("Failed to create entity.")
                }

            } catch (e: IOException) {
                _createEntityState.value = ApiState.Error("Network error. Please try again.")
            } catch (e: HttpException) {
                _createEntityState.value = ApiState.Error("Server error: ${e.code()}. Please try again.")
            } catch (e: Exception) {
                _createEntityState.value = ApiState.Error("An unexpected error occurred: ${e.message}")
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
                _successMessage.value = "Server error: ${e.code()}. Please try again."
            } finally {
                _loading.value = false
            }
        }
    }
}

sealed class ApiState {
    object Loading : ApiState()
    data class Success(val message: String) : ApiState()
    data class Error(val message: String) : ApiState()
}

private fun encodeImageToBase64(imageFile: File): String {
    val bytes = imageFile.readBytes()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}