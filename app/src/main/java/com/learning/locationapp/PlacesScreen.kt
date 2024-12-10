package com.learning.locationapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import coil3.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition


const val BASE_URL = "https://labs.anontech.info/cse489/t3/"


@Composable
fun PlacesScreen(
    viewModel: PlacesViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.fetchPlaces()
    }

    val places by viewModel.places.collectAsState()

    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var isImageEnlarged by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.6850, 90.3563), 7f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState
        ) {
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.lat, place.lon)),
                    title = place.title,
                    onClick = {
                        selectedPlace = place
                        true
                    }
                )
            }
        }

        selectedPlace?.let { place ->
            PlaceDetailsDialog(
                place = place,
                onImageClick = { isImageEnlarged = true },
                onClose = { selectedPlace = null }
            )
        }
    }

    // Show enlarged image dialog
    if (isImageEnlarged && selectedPlace != null) {
        EnlargedImageDialog(
            imageUrl = selectedPlace!!.imageUrl,
            onDismiss = { isImageEnlarged = false }
        )
    }
}




@Composable
fun PlaceDetailsDialog(place: Place, onImageClick: () -> Unit, onClose: () -> Unit) {
    android.util.Log.d("MapEntriesItem", "Image URL: ${place.image}")
    val imageStr = place.image
    android.util.Log.d("ImageStr", "Image str: $imageStr")
    val fullImageUrl = "$BASE_URL${place.image}"

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(text = place.title)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if(place.image!="images/"){
                    Image(
                        painter = rememberAsyncImagePainter(fullImageUrl),
                        contentDescription = place.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable(onClick = onImageClick),
                        contentScale = ContentScale.FillHeight
                    )
                } else {
                    Text(text = "No Image Available")
                }

            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }
    )
}


@Composable
fun EnlargedImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxSize(), shape = MaterialTheme.shapes.medium) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Enlarged Image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
