package com.learning.locationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    onItemClick = {
                        // Close the drawer when an item is clicked
                        scope.launch { drawerState.close() }
                    }
                )
            }
        },
        drawerState = drawerState,
        gesturesEnabled = false
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.apply { if (isClosed) open() else close() }
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
                        CreateEntityScreen(
                            viewModel = placesViewModel,
                            onEntityCreated = { },
                            locationViewModel = locationViewModel
                        )
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
fun DrawerContent(navController: NavController, onItemClick: () -> Unit) {
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
            onClick = { navController.navigate("placesScreen")
                onItemClick()}
        )

        DrawerItem(
            label = "Create Entity",
            icon = Icons.Default.Build,
            isSelected = currentRoute == "createEntity",
            onClick = { navController.navigate("createEntity")
                onItemClick()}
        )

        DrawerItem(
            label = "View Entities",
            icon = Icons.Default.Menu,
            isSelected = currentRoute == "viewEntities",
            onClick = { navController.navigate("viewEntities")
                onItemClick()}
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
                    popUpTo("placesScreen") { inclusive = true }
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
