package com.learning.locationapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun LocationSelectionScreen(
    location: LocationData = LocationData(latitude = 23.6850, longitude = 90.3563),
    onLocationSelected: (LocationData) -> Unit
) {
    val userLocation = remember {
        mutableStateOf(LatLng(location.latitude, location.longitude))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation.value, 7f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                userLocation.value = latLng
            }
        ) {
            // Display a marker at the selected location
            Marker(state = MarkerState(position = userLocation.value))
        }

        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = {
                val newLocation = LocationData(userLocation.value.latitude, userLocation.value.longitude)
                onLocationSelected(newLocation)
            }
        ) {
            Text(text = "Select Location")
        }
    }
}

