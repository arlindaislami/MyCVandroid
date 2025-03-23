package com.example.mycvandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mycvandroid.ui.theme.MyCVandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCVandroidTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.user_logo), // Vendosni emrin e logos që e keni
            contentDescription = "Logo User",
            modifier = Modifier
                .size(80.dp)  // Mund ta rregulloni madhësinë e logos
                .padding(bottom = 16.dp)  // Hapësirë nën logo
        )

        // Titulli
        Text(
            text = "MY CV",
            fontSize = 30.sp,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Përshkrimi
        Text(
            text = "Një aplikacion për të krijuar dhe menaxhuar CV-në tënde në mënyrë të lehtë.",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Butoni Log In
        Button(onClick = { /* Shko te Log In */ }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text(text = "Log In")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Butoni Sign Up
        Button(onClick = { /* Shko te Sign Up */ }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text(text = "Sign Up")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MyCVandroidTheme {
        MainScreen()
    }
}
