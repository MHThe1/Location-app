package com.learning.locationapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
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
            PlaceDetailsBottomSheet(
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
            title = selectedPlace!!.title,
            onDismiss = { isImageEnlarged = false }
        )
    }
}

@Composable
fun PlaceDetailsBottomSheet(place: Place, onImageClick: () -> Unit, onClose: () -> Unit) {
    val imageStr = place.image
    val fullImageUrl = "$BASE_URL${place.image}"

    // Bottom sheet-like view for place details
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = place.title, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                if (place.image != "images/") {
                    Image(
                        painter = rememberAsyncImagePainter(fullImageUrl),
                        contentDescription = place.title,
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .graphicsLayer(
                                scaleX = 0.5f,  // Initially scaled down
                                scaleY = 0.5f   // Initially scaled down
                            )
                            .clickable(onClick = onImageClick),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = "No Image Available")
                }
            }
        }
    }
}

@Composable
fun EnlargedImageDialog(imageUrl: String, title: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Enlarged Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnlargedImageDialogPreview() {
    // Simulating an image URL for preview purposes
    EnlargedImageDialog(imageUrl = "https://labs.anontech.info/cse489/t3/images/resized_image.jpg", title = "Sample Title", onDismiss = {})
}
