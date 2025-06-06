package com.example.mycvandroid

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val database = Firebase.database.reference
    val storage = Firebase.storage

    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var emailVerified by remember { mutableStateOf(currentUser?.isEmailVerified == true) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

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
                snapshot.child("image").getValue(String::class.java)?.let {
                    imageUrl = it
                }

                val personalInfo = snapshot.child("personalinfo")
                fullName = personalInfo.child("fullname").getValue(String::class.java) ?: ""
                phoneNumber = personalInfo.child("phonenumber").getValue(String::class.java) ?: ""

                currentUser?.reload()?.await()
                emailVerified = currentUser?.isEmailVerified == true

                val pendingEmail = sharedPrefs.getString("pending_email", null)
                if (emailVerified && pendingEmail != null && pendingEmail != currentUser.email) {
                    currentUser?.updateEmail(pendingEmail)?.await()
                    email = pendingEmail
                    database.child("users").child(uid).child("email").setValue(pendingEmail).await()
                    sharedPrefs.edit().remove("pending_email").apply()
                }

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

            if (email != currentUser?.email) {
                currentUser?.verifyBeforeUpdateEmail(email)?.await()
                sharedPrefs.edit().putString("pending_email", email).apply()
                auth.signOut()
                saveMessage = "A verification link has been sent to your new email. Please verify it and log in again to complete the update."
            } else {
                saveMessage = "Changes saved successfully."
            }

        } catch (e: Exception) {
            e.printStackTrace()
            saveMessage = "An error occurred while saving: ${e.localizedMessage}"
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
                                saveMessage = "Image saved successfully."
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
        Spacer(modifier = Modifier.height(20.dp))

        ReadOnlyCardField(label = "Full Name", value = fullName)
        ReadOnlyCardField(label = "Phone Number", value = phoneNumber)

        FormFieldProfile(
            label = "Email",
            value = email,
            onChange = { email = it },
            enabled = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    saveChanges()
                }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF58B994),
                contentColor = Color.White
            )
        ) {
            Text(text = if (isSaving) "Saving..." else "Save Changes")
        }

        if (saveMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = saveMessage)
        }

        Spacer(modifier = Modifier.height(27.dp))
        Text(
            text = "To delete your account, please send a request to: arlindaaislamii@gmail.com",
            fontSize = 14.sp,
            color = Color.Red
        )
    }
}

@Composable
fun ReadOnlyCardField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun FormFieldProfile(label: String, value: String, onChange: (String) -> Unit, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00796B),
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color(0xFF00796B),
            cursorColor = Color(0xFF00796B)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
