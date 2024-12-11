package com.learning.locationapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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

data class CreateResponse(
    val id: Int // The ID of the created entity
)

@Composable
fun PlaceForm(
    title: String,
    onTitleChange: (String) -> Unit,
    lat: String,
    onLatChange: (String) -> Unit,
    lon: String,
    onLonChange: (String) -> Unit,
) {
    Column {
        // Title Input
        TextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Latitude Input
        TextField(
            value = lat,
            onValueChange = onLatChange,
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Longitude Input
        TextField(
            value = lon,
            onValueChange = onLonChange,
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun CreateEntityScreen(
    viewModel: PlacesViewModel, // Pass the ViewModel instance
    onEntityCreated: () -> Unit
) {
    val title = remember { mutableStateOf("") }
    val lat = remember { mutableStateOf("") }
    val lon = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val resultMessage = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Create Entity")

        // Form fields for input
        PlaceForm(
            title = title.value,
            onTitleChange = { title.value = it },
            lat = lat.value,
            onLatChange = { lat.value = it },
            lon = lon.value,
            onLonChange = { lon.value = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show loading indicator when creating the entity
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        // Submit button
        Button(
            onClick = {
                val latDouble = lat.value.toDoubleOrNull()
                val lonDouble = lon.value.toDoubleOrNull()

                if (title.value.isNotBlank() && latDouble != null && lonDouble != null) {
                    isLoading.value = true
                    resultMessage.value = null

                    // Call ViewModel to create the entity
                    viewModel.viewModelScope.launch {
                        try {
                            viewModel.createPlace(
                                title = title.value,
                                lat = latDouble,
                                lon = lonDouble
                            )
                            resultMessage.value = "Entity created successfully!"
                            onEntityCreated() // Trigger callback
                        } catch (e: Exception) {
                            resultMessage.value = "Failed to create entity: ${e.message}"
                        } finally {
                            isLoading.value = false
                        }
                    }
                } else {
                    resultMessage.value = "Please fill out all fields."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value
        ) {
            Text(if (isLoading.value) "Creating..." else "Create Entity")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show result message
        resultMessage.value?.let {
            Text(text = it, color = if (it.contains("successfully")) Color.Green else Color.Red)
        }
    }
}


