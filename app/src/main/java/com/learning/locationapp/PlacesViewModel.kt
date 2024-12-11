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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    fun createPlace(title: String, lat: Double, lon: Double, imagePath: String) {
        // Create request bodies for the text fields
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val latBody = lat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val lonBody = lon.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        // Create a file part for the image
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        viewModelScope.launch {
            _createEntityState.value = ApiState.Loading

            try {
                // Call the API
                val response = RetrofitInstance.api.createPlace(
                    title = titleBody,
                    lat = latBody,
                    lon = lonBody,
                    image = imagePart
                )

                // Handle the API response
                if (response.id > 0) { // Check if 'id' is returned
                    _createEntityState.value = ApiState.Success("Place created with ID: ${response.id}")
                    fetchPlaces() // Refresh the list
                } else {
                    _createEntityState.value = ApiState.Error("Failed to create place.")
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