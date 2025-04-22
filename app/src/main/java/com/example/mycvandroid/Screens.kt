package com.example.mycvandroid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("home") { Home(navController) }
        composable ( "cv_preview"){ CVPreviewScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser


    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("main") { inclusive = true }
            }
        }
    }


    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.user_logo),
                contentDescription = "Logo User",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "MY CV",
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFB6693D)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Create and manage your CV easily with our app.",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF58B994), Color(0xFF467A9F))
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Log In", color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { navController.navigate("signup") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF58B994), Color(0xFF467A9F))
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Sign Up", color = Color.White)
            }
        }
    }
}

