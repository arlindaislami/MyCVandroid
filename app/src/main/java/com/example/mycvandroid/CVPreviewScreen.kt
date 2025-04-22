package com.example.mycvandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person


import androidx.compose.ui.platform.LocalContext

@Composable
fun CVPreviewScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    var name by remember { mutableStateOf("Name Lastname") }
    var title by remember { mutableStateOf("Software Engineer") }
    var email by remember { mutableStateOf("example@gmail.com") }
    var phone by remember { mutableStateOf("0713903012484") }
    var location by remember { mutableStateOf("Malmö, Sweden") }
    var photoUrl by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        uid?.let {
            val userRef = Firebase.database.getReference("users").child(it)

            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("cvdata").child("image").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    photoUrl = snapshot.getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    val context = LocalContext.current
    val view = LocalView.current

    Row(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(Color(0xFFB28B72))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {

                            exportScreenAsPDF(context, view)
                        }
                )
            }

            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 8.dp)
                        .background(Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Icon",
                        tint = Color.LightGray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }



            Spacer(modifier = Modifier.height(16.dp))
            Text("CONTACT", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))
            Text(email, color = Color.White, fontSize = 10.sp)
            Text(phone, color = Color.White, fontSize = 10.sp)
            Text(location, color = Color.White, fontSize = 10.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Skills", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Skill 1", color = Color.White)
            Text("• Skill 2", color = Color.White)
            Text("• Skill 3", color = Color.White)
            Text("• Skill 4", color = Color.White)
            Text("• Skill 5", color = Color.White)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Languages", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("• Language 1", color = Color.White)
            Text("• Language 2", color = Color.White)
            Text("• Language 3", color = Color.White)
        }


        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F5))
                .padding(24.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Section("Profile", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt...")

            Section("Educations", """
                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...

                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...
            """.trimIndent())

            Section("Experience", """
                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...

                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...
            """.trimIndent())

            Section("Trainings", """
                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...

                Title (2010 - 2012)
                Lorem ipsum dolor sit amet...
            """.trimIndent())
        }
    }
}

@Composable
fun Section(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, fontSize = 14.sp)
    }
}