package com.example.mycvandroid

import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Skill(val name: String)
data class SectionData(val title: String, val content: String)

@Composable
fun CVPreviewScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profileDescription by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    var skills by remember { mutableStateOf(listOf<Skill>()) }
    var educationSections by remember { mutableStateOf(listOf<SectionData>()) }
    var experienceSections by remember { mutableStateOf(listOf<SectionData>()) }
    var trainingSections by remember { mutableStateOf(listOf<SectionData>()) }

    var isExporting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cvContainer = remember { mutableStateOf<View?>(null) }

    LaunchedEffect(uid) {
        uid?.let {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(it).child("cvdata")

            userRef.child("personalinfo").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.child("fullname").getValue(String::class.java) ?: ""
                    title = snapshot.child("profession").getValue(String::class.java) ?: ""
                    email = snapshot.child("email").getValue(String::class.java) ?: ""
                    phone = snapshot.child("phonenumber").getValue(String::class.java) ?: ""
                    address = snapshot.child("address").getValue(String::class.java) ?: ""
                    profileDescription = snapshot.child("profileDescription").getValue(String::class.java) ?: "" // Marrim përshkrimin e profilit
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("image").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    photoUrl = snapshot.getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("skills").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val skillList = mutableListOf<Skill>()
                    for (child in snapshot.children) {
                        val name = child.child("name").getValue(String::class.java)
                        name?.let { skillList.add(Skill(it)) }
                    }
                    skills = skillList
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("education").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<SectionData>()
                    for (child in snapshot.children) {
                        val title = child.child("eduName").getValue(String::class.java) ?: continue
                        val desc = child.child("description").getValue(String::class.java) ?: ""
                        val start = child.child("startDate").getValue(String::class.java) ?: ""
                        val end = child.child("endDate").getValue(String::class.java) ?: ""
                        val content = "$desc\n($start - $end)"
                        list.add(SectionData(title, content))
                    }
                    educationSections = list
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("experience").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<SectionData>()
                    for (child in snapshot.children) {
                        val title = child.child("expName").getValue(String::class.java) ?: continue
                        val desc = child.child("description").getValue(String::class.java) ?: ""
                        val start = child.child("startDate").getValue(String::class.java) ?: ""
                        val end = child.child("endDate").getValue(String::class.java) ?: ""
                        val content = "$desc\n($start - $end)"
                        list.add(SectionData(title, content))
                    }
                    experienceSections = list
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            userRef.child("trainings").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<SectionData>()
                    for (child in snapshot.children) {
                        val title = child.child("trainingName").getValue(String::class.java) ?: continue
                        val desc = child.child("description").getValue(String::class.java) ?: ""
                        val start = child.child("startDate").getValue(String::class.java) ?: ""
                        val end = child.child("endDate").getValue(String::class.java) ?: ""
                        val content = "$desc\n($start - $end)"
                        list.add(SectionData(title, content))
                    }
                    trainingSections = list
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Export",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        isExporting = true
                        cvContainer.value?.let { view ->
                            exportScreenAsPDF(context, view) {
                                isExporting = false
                                Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
        }

        AndroidView(
            factory = { context ->
                ComposeView(context).apply {
                    setContent {
                        CVContent(
                            name = name,
                            title = title,
                            email = email,
                            phone = phone,
                            location = address,
                            photoUrl = photoUrl,
                            skills = skills,
                            education = educationSections,
                            experience = experienceSections,
                            trainings = trainingSections,
                            profileDescription = profileDescription
                        )
                    }
                }.also { cvContainer.value = it }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}

@Composable
fun CVContent(
    name: String,
    title: String,
    email: String,
    phone: String,
    location: String,
    photoUrl: String,
    skills: List<Skill>,
    education: List<SectionData>,
    experience: List<SectionData>,
    trainings: List<SectionData>,
    profileDescription: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {


        Column(
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
                .background(Color(0xFFB28B72))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, shape = CircleShape)
            ) {
                if (photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center),
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text("Contact", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ContactInfo(Icons.Default.Email, email)
            ContactInfo(Icons.Default.Phone, phone)
            ContactInfo(Icons.Default.LocationOn, location)

            if (skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(36.dp))
                Text("Skills", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                skills.forEach {
                    Text("• ${it.name}", fontSize = 12.sp, color = Color.White)
                }
            }

            if (trainings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(36.dp))
                Text("Trainings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                trainings.forEach {
                    Text("• ${it.title}", fontSize = 12.sp, color = Color.White)
                }
            }
        }


        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB28B72))
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(14.dp))

            if (profileDescription.isNotEmpty()) {
                Text("About Me", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    profileDescription,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (education.isNotEmpty()) {
                SectionHeader("Education")
                education.forEach {
                    SectionItem(title = it.title, content = it.content)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (experience.isNotEmpty()) {
                SectionHeader("Experience")
                experience.forEach {
                    SectionItem(title = it.title, content = it.content)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



@Composable
fun ContactInfo(icon: ImageVector, info: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(info, fontSize = 12.sp, color = Color.White)
    }
}


@Composable
fun SectionItem(title: String, content: String) {
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    Text(content, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(8.dp))
}
