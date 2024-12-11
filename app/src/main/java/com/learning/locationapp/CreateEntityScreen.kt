package com.learning.locationapp

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getPathFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun CreateEntityScreen(
    viewModel: PlacesViewModel,
    onEntityCreated: () -> Unit,
    locationViewModel: LocationViewModel
) {
    val title = remember { mutableStateOf("") }
    val lat = remember { mutableStateOf("") }
    val lon = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val resultMessage = remember { mutableStateOf<String?>(null) }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val locationUtils = LocationUtils(context)

    // Permission request for location
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {
                Toast.makeText(context, "Location Permission Granted!", Toast.LENGTH_SHORT).show()
                locationUtils.requestLocationUpdates(locationViewModel)
            } else {
                Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Get the current location from the ViewModel
    val location = locationViewModel.location.value

    // Update the title based on the location's latitude and longitude using reverse geocoding
    LaunchedEffect(location) {
        location?.let {
            title.value = locationUtils.reverseGeocodeLocation(it)
            lat.value = it.latitude.toString()
            lon.value = it.longitude.toString()
        }
    }

    // Image picker launcher to select an image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri.value = uri
            Log.d("CreateEntityScreen", "Selected image URI: $uri")
        }
    )

    // Resize image function using Coil
    fun resizeImage(context: Context, uri: Uri, maxWidth: Int = 800, maxHeight: Int = 800): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val newWidth = if (originalBitmap.width > originalBitmap.height) maxWidth else (maxHeight * aspectRatio).toInt()
            val newHeight = if (originalBitmap.height > originalBitmap.width) maxHeight else (maxWidth / aspectRatio).toInt()

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

            // Save resized bitmap to a temporary file
            val file = File(context.cacheDir, "resized_image.jpg")
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Create Entity")

        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = lat.value,
            onValueChange = { lat.value = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = lon.value,
            onValueChange = { lon.value = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (locationUtils.hasLocationPermission(context)) {
                    locationUtils.requestLocationUpdates(locationViewModel)
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Get Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val latDouble = lat.value.toDoubleOrNull()
                val lonDouble = lon.value.toDoubleOrNull()
                val imageUri = selectedImageUri.value

                if (title.value.isNotBlank() && latDouble != null && lonDouble != null && imageUri != null) {
                    isLoading.value = true
                    resultMessage.value = null

                    // Resize the image before uploading
                    val resizedImageFile = resizeImage(context, imageUri)
                    if (resizedImageFile != null) {
                        // Call ViewModel to create the entity with resized image file path
                        viewModel.viewModelScope.launch {
                            try {
                                // Call createPlace in ViewModel
                                viewModel.createPlace(
                                    title = title.value,
                                    lat = latDouble,
                                    lon = lonDouble,
                                    imagePath = resizedImageFile.absolutePath
                                )
                                resultMessage.value = "Entity created successfully!"
                                onEntityCreated()
                            } catch (e: Exception) {
                                resultMessage.value = "Failed to create entity: ${e.message}"
                            } finally {
                                isLoading.value = false
                            }
                        }
                    } else {
                        resultMessage.value = "Failed to resize the selected image."
                        isLoading.value = false
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

        resultMessage.value?.let {
            Toast.makeText(
                context,
                it,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
