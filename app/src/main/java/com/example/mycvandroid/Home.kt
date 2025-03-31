import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.*

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
            composable("myfiles") { MyFilesScreen() }
            composable("newfile") { NewFileScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("My CV") },
        actions = {
            IconButton(onClick = {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
