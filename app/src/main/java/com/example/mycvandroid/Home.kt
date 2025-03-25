import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout

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
                    popUpTo("home") { inclusive = true }  // 🔥 Kthen direkt te login dhe fshin historinë
                }
            }) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
            }
        }
    )
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("myfiles") },
            icon = { Icon(Icons.Filled.Folder, contentDescription = "My Files") },
            label = { Text("My Files") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("newfile") },
            icon = { Icon(Icons.Filled.Add, contentDescription = "New File") },
            label = { Text("New File") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
