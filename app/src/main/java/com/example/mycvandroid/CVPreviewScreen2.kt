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


@Composable
fun CVPreviewScreen2(navController: NavController) {
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
                    profileDescription = snapshot.child("profileDescription").getValue(String::class.java) ?: ""
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
                tint = Color(0xFF4A90E2),
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
                tint = Color(0xFF4A90E2),
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
                        CVContent2(
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
fun CVContent2(
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
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.LightGray, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFB3E5FC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Icon",
                        tint = Color(0xFF4E6075),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (profileDescription.isNotBlank()) {
                Text("PROFILE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(profileDescription, fontSize = 12.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(20.dp))
            }


            Text("CONTACT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(Icons.Default.Email, email)
            InfoItem(Icons.Default.Phone, phone)
            InfoItem(Icons.Default.LocationOn, location)

            Spacer(modifier = Modifier.height(20.dp))



            if (skills.isNotEmpty()) {
                Text("SKILLS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                skills.forEach {
                    Text("• ${it.name}", fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {

            Text(name.uppercase(), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))


            if (experience.isNotEmpty()) {
                SectionTitle("WORK EXPERIENCE")
                experience.forEach {
                    SectionItem(it)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            if (education.isNotEmpty()) {
                SectionTitle("EDUCATION")
                education.forEach {
                    SectionItem(it)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (experience.isNotEmpty()) {
                SectionTitle("EXPerience")
                experience.forEach {
                    SectionItem(it)
                }
            }

            if (trainings.isNotEmpty()) {
                SectionTitle("TRAININGS")
                trainings.forEach {
                    SectionItem(it)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Column {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier
                .background(Color(0xFFA5B9C2))
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SectionItem(section: SectionData) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(section.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(section.content, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String) {
    if (text.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

