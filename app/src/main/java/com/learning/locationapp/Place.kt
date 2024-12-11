package com.learning.locationapp


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
