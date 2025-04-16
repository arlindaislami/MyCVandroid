package com.example.mycvandroid

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val currentUser = auth.currentUser
    val database = Firebase.database.reference
    val storage = Firebase.storage

    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }


    LaunchedEffect(userId) {
        userId?.let { uid ->
            try {
                val snapshot = database.child("users").child(uid).child("cvdata").get().await()
                snapshot.child("personalinfo").let { personal ->
                    fullname = personal.child("fullname").getValue(String::class.java) ?: ""
                    email = personal.child("email").getValue(String::class.java) ?: ""
                    phonenumber = personal.child("phonenumber").getValue(String::class.java) ?: ""
                }
                imageUrl = snapshot.child("image").getValue(String::class.java) ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveChanges() {
        isSaving = true
        saveMessage = ""

        try {
       
            selectedImageUri?.let { uri ->
                val fileName = UUID.randomUUID().toString()
                val imageRef = storage.reference.child("profile_images/$userId/$fileName.jpg")
                imageRef.putFile(uri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                userId?.let { uid ->
                    database.child("users").child(uid).child("cvdata").child("image").setValue(downloadUrl).await()
                    imageUrl = downloadUrl
                    selectedImageUri = null
                }
            }


            userId?.let { uid ->
                val personalRef = database.child("users").child(uid).child("cvdata").child("personalinfo")
                personalRef.child("fullname").setValue(fullname).await()
                personalRef.child("email").setValue(email).await()
                personalRef.child("phonenumber").setValue(phonenumber).await()
            }

            currentUser?.updateEmail(email)?.await()
            val profileUpdates = userProfileChangeRequest {
                displayName = fullname
            }
            currentUser?.updateProfile(profileUpdates)?.await()

            saveMessage = "Changes saved successfully"
        } catch (e: Exception) {
            e.printStackTrace()
            saveMessage = "Error saving changes: ${e.localizedMessage}"
        } finally {
            isSaving = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))


        Image(
            painter = rememberAsyncImagePainter(
                model = selectedImageUri ?: imageUrl.ifEmpty { "https://via.placeholder.com/150" }
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { imagePicker.launch("image/*") }
        )

        Text(text = "Tap to change photo", fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))


        if (selectedImageUri != null) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            isSaving = true
                            val fileName = UUID.randomUUID().toString()
                            val imageRef = storage.reference.child("profile_images/$userId/$fileName.jpg")
                            selectedImageUri?.let { uri ->
                                imageRef.putFile(uri).await()
                                val downloadUrl = imageRef.downloadUrl.await().toString()
                                userId?.let { uid ->
                                    database.child("users").child(uid).child("cvdata").child("image").setValue(downloadUrl).await()
                                    imageUrl = downloadUrl
                                    selectedImageUri = null
                                }
                                saveMessage = "Image saved successfully"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            saveMessage = "Error saving image: ${e.localizedMessage}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving
            ) {
                Text(text = if (isSaving) "Saving..." else "Save Image")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(text = "Profile", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = fullname,
            onValueChange = { fullname = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = phonenumber,
            onValueChange = { phonenumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    saveChanges()
                }
            },
            enabled = !isSaving
        ) {
            Text(text = if (isSaving) "Saving..." else "Save Changes")
        }

        if (saveMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = saveMessage)
        }
    }
}
