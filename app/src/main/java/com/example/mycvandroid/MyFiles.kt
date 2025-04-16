package com.example.mycvandroid

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun MyFilesScreen(navController: NavController, tabNavController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var userName by remember { mutableStateOf("Username") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        uid?.let {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(it)
            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    if (name != null) {
                        userName = name
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("cvdata").child("image")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val url = snapshot.getValue(String::class.java)
                            profileImageUrl = url
                        } catch (e: Exception) {
                            Log.e("FIREBASE", "Error reading profile image", e)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FIREBASE", "Cancelled: ${error.message}")
                    }
                })

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // User Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF7E6B9B), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Gray, shape = CircleShape)
                            .shadow(4.dp, shape = CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = "Hi, $userName",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C2C2C)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "My Files",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A2B5E)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = 1.dp,
            color = Color(0xFFE0DFF1)
        )

        // SCROLLABLE CARD LIST
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FileCardWithText(
                title = "Create New CV",
                backgroundColor = Color(0xFFF7F3FF),
                showIcon = true,
                onClick = { tabNavController.navigate("newfile") }
            )

            FileCardWithText(
                title = "Preview 1",
                backgroundColor = Color(0xFFF0F0F0),
                showIcon = false,
                onClick = {navController.navigate("cv_preview")}
            )

            FileCardWithText(
                title = "Preview 2",
                backgroundColor = Color(0xFFDFF5F3),
                showIcon = false,
                onClick = { /* Nav te CV 2 nëse e implementon */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

      /*  Button(onClick = { navController.navigate("list")}) {
            Text("List")
        }
*/
    }
}


@Composable
fun FileCardWithText(
    title: String,
    backgroundColor: Color,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FileCardItem(
            backgroundColor = backgroundColor,
            showIcon = showIcon,
            onClick = onClick
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C2C2C)
        )
    }
}

@Composable
fun FileCardItem(
    backgroundColor: Color,
    showIcon: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .padding(4.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Icon",
                tint = Color(0xFF3A2B5E),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
