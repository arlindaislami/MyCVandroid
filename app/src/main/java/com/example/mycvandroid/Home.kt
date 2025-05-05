package com.example.mycvandroid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Home(navController: NavController) {
    val tabNavController = rememberNavController()
    Scaffold(
        topBar = { TopAppBar(navController) },
        bottomBar = { BottomNavigationBar(tabNavController) }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "myfiles",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("myfiles") { MyFilesScreen(navController = navController, tabNavController = tabNavController  ) }
            composable("newfile") { NewFileScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("main") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    CenterAlignedTopAppBar(
        title = { Text("My CV") },
        actions = {
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
            }
        }
    )
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("myfiles", "My Files", Icons.Filled.Folder),
        BottomNavItem("newfile", "New File", Icons.Filled.Add),
        BottomNavItem("profile", "Profile", Icons.Filled.AccountCircle)
    )
    val currentRoute = currentRoute(navController)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Surface(
            color = Color(0xFFE9F3EB),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    if (!isSelected) {
                        NavigationIcon(item, isSelected = false) {
                            navController.navigate(item.route)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            }
        }


        val activeIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(items.size) { index ->
                if (index == activeIndex) {
                    FloatingActionButton(
                        onClick = { navController.navigate(items[index].route) },
                        containerColor = Color.White,
                        contentColor = Color(0xFFED7161),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = items[index].icon,
                            contentDescription = items[index].label
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }
            }
        }
    }
}
@Composable
fun NavigationIcon(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) Color(0xFFED7161) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            color = if (isSelected) Color(0xFFED7161) else Color.Gray,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
