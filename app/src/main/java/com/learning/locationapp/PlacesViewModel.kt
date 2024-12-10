package com.learning.locationapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlacesViewModel : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    fun fetchPlaces() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPlaces()
                _places.value = response
            } catch (e: Exception) {
                // Handle error (e.g., log or show a message)
            }
        }
    }
}
