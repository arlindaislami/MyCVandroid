package com.example.mycvandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun CVPreviewScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        uid?.let {
            val userRef = Firebase.database.getReference("users").child(it)

            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("email").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    email = snapshot.getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("phone").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    phone = snapshot.getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Ikona X
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Preview",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clickable {
                    navController.popBackStack()
                }
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Curriculum Vitae", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Emri: $name", fontSize = 18.sp)
            Text("Email: $email", fontSize = 18.sp)
            Text("Tel: $phone", fontSize = 18.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {

            }) {
                Text("Export as PDF")
            }
        }
    }
}
