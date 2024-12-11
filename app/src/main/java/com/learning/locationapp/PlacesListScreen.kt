package com.learning.locationapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter


@Composable
fun PlacesListScreen(
    viewModel: PlacesViewModel = viewModel(),
) {
    // Fetch places on first launch
    LaunchedEffect(Unit) {
        viewModel.fetchPlaces()
    }

    val places by viewModel.places.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var isDialogOpen by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    // Get context for showing Toast messages
    val context = LocalContext.current

    // Show success/failure message using Toast
    LaunchedEffect(successMessage) {
        successMessage?.let {
            if (it == "Place updated successfully!") {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            } else if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            // Show a loading spinner while updating
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(places) { place ->
                    PlaceListItem(
                        place = place,
                        onClick = {
                            selectedPlace = place
                            isDialogOpen = true
                        }
                    )
                }
            }
        }
    }

    // Show the Edit Place Dialog when an item is clicked
    if (isDialogOpen && selectedPlace != null) {
        EditPlaceDialog(
            place = selectedPlace!!,
            onDismiss = { isDialogOpen = false },
            onSave = { updatedPlace ->
                // Call the ViewModel's updatePlace function
                viewModel.updatePlace(updatedPlace)
                isDialogOpen = false
            }
        )
    }
}

@Composable
fun PlaceListItem(place: Place, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        val imageUrl = "$BASE_URL${place.image}"
        val painter = rememberAsyncImagePainter(imageUrl)

        Image(
            painter = painter,
            contentDescription = place.title,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = place.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
    }
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}



@Composable
fun EditPlaceDialog(
    place: Place,
    onDismiss: () -> Unit,
    onSave: (Place) -> Unit
) {
    var title by remember { mutableStateOf(place.title) }
    var lat by remember { mutableStateOf(place.lat) }
    var lon by remember { mutableStateOf(place.lon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Place")
        },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = lat.toString(),
                    onValueChange = { lat = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = lon.toString(),
                    onValueChange = { lon = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // When the user clicks "Save", create an updated place and send it to the ViewModel
                    val updatedPlace = place.copy(title = title, lat = lat, lon = lon)
                    onSave(updatedPlace)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}




