package com.learning.locationapp

import android.content.Context
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.learning.locationapp.ui.theme.LocationAppTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationApp(modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val locationViewModel: LocationViewModel = viewModel()
    val placesViewModel: PlacesViewModel = viewModel()

    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet {
            DrawerContent(navController = navController)
        }
    }, drawerState = drawerState) {
        Scaffold(
            topBar = {
                TopBar(
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    navController = navController
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavHost(navController = navController, startDestination = "placesScreen") {
                    composable("placesScreen") {
                        PlacesScreen()
                    }
                    composable("createEntity") {
                        CreateEntityScreen(viewModel = placesViewModel, onEntityCreated = { })
                    }
                    composable("viewEntities") {
                        PlacesListScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun DrawerContent(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Location App",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Divider()

        DrawerItem(
            label = "Home",
            icon = Icons.Default.Home,
            isSelected = currentRoute == "placesScreen",
            onClick = { navController.navigate("placesScreen") }
        )

        DrawerItem(
            label = "Create Entity",
            icon = Icons.Default.Build,
            isSelected = currentRoute == "createEntity",
            onClick = { navController.navigate("createEntity") }
        )

        DrawerItem(
            label = "View Entities",
            icon = Icons.Default.Menu,
            isSelected = currentRoute == "viewEntities",
            onClick = { navController.navigate("viewEntities") }
        )
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { Text(text = label, fontSize = 16.sp) },
        selected = isSelected,
        onClick = onClick
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onOpenDrawer: () -> Unit, navController: NavController) {
    TopAppBar(
        title = { Text(text = "Location App") },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate("placesScreen") {
                    popUpTo("placesScreen") { inclusive = true } // Pop all other backstack entries
                }
            }) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
            }

            IconButton(onClick = {
                navController.navigate("createEntity")
            }) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add New")
            }
        }
    )
}

@Composable
fun MyApp(viewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    val navController = rememberNavController()

    // Start the navigation host
    NavHost(navController = navController, startDestination = "location_display") {
        composable("location_display") {
            LocationDisplay(locationUtils, viewModel, context, navController)
        }
        composable("location_selection") {
            LocationSelectionScreen(
                location = LocationData(viewModel.location.value?.latitude ?: 23.6850,
                    viewModel.location.value?.longitude ?: 90.3563),
                onLocationSelected = { selectedLocation ->
                    viewModel.updateLocation(selectedLocation)
                    navController.popBackStack()
                }
            )
        }
    }
}


@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context: Context,
    navController: NavHostController
) {
    val location = viewModel.location.value

    // Recalculate the address whenever the location changes
    val address = location?.let { locationUtils.reverseGeocodeLocation(location) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {
                Toast.makeText(context, "Location Permission Granted!", Toast.LENGTH_SHORT).show()
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            } else {
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as ComponentActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationaleRequired) {
                    Toast.makeText(
                        context,
                        "Location Permission is required to access location features.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Permission Denied Permanently! Enable permissions in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (location != null) {
            // Display the updated location and address dynamically
            Text(text = "Latitude: ${location.latitude}")
            Text(text = "Longitude: ${location.longitude}")
            Text(text = "Address: $address")
        } else {
            Text(text = "Location not available")
        }

        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                Toast.makeText(context, "Getting your location, Please standby!", Toast.LENGTH_SHORT).show()
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) {
            Text(text = "Get Location")
        }

        Button(onClick = {
            navController.navigate("location_selection")
        }) {
            Text(text = "Set Location")
        }
    }
}


