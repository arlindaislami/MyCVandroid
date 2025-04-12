package com.example.mycvandroid

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider

data class EduData(
    var eduName: String = "",
    var description: String = "",
    var startDate: String = "",
    var endDate: String = ""
)

data class ExpData(
    var expName: String = "",
    var description: String = "",
    var startDate: String = "",
    var endDate: String = ""
)

data class TrainingData(
    var trainingName: String = "",
    var description: String = "",
    var startDate: String = "",
    var endDate: String = ""
)

data class SkillData(
    var name: String = ""
)

@Composable
fun NewFileScreen() {
    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }

    val educationList = remember { mutableStateListOf(mutableStateOf(EduData())) }
    val experienceList = remember { mutableStateListOf(mutableStateOf(ExpData())) }
    val trainingList = remember { mutableStateListOf(mutableStateOf(TrainingData())) }
    val skillsList = remember { mutableStateListOf(mutableStateOf(SkillData())) }

    val database: DatabaseReference = Firebase.database.reference
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    if (userId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please sign in to save your CV!", color = Color.Red, fontSize = 18.sp)
        }
        return
    }

    LaunchedEffect(userId) {
        Firebase.database.reference
            .child("users")
            .child(userId)
            .child("cvdata")
            .get()
            .addOnSuccessListener { dataSnapshot ->
                dataSnapshot.child("personalinfo").let { personalInfo ->
                    fullname = personalInfo.child("fullname").getValue(String::class.java) ?: ""
                    email = personalInfo.child("email").getValue(String::class.java) ?: ""
                    phonenumber = personalInfo.child("phonenumber").getValue(String::class.java) ?: ""
                }

                dataSnapshot.child("education").children.forEach { eduSnap ->
                    val edu = EduData(
                        eduName = eduSnap.child("eduName").getValue(String::class.java) ?: "",
                        description = eduSnap.child("description").getValue(String::class.java) ?: "",
                        startDate = eduSnap.child("startdate").getValue(String::class.java) ?: "",
                        endDate = eduSnap.child("enddate").getValue(String::class.java) ?: ""
                    )
                    educationList.add(mutableStateOf(edu))
                }

                dataSnapshot.child("experience").children.forEach { expSnap ->
                    val exp = ExpData(
                        expName = expSnap.child("expName").getValue(String::class.java) ?: "",
                        description = expSnap.child("description").getValue(String::class.java) ?: "",
                        startDate = expSnap.child("startdate").getValue(String::class.java) ?: "",
                        endDate = expSnap.child("enddate").getValue(String::class.java) ?: ""
                    )
                    experienceList.add(mutableStateOf(exp))
                }

                dataSnapshot.child("trainings").children.forEach { trainSnap ->
                    val training = TrainingData(
                        trainingName = trainSnap.child("trainingName").getValue(String::class.java) ?: "",
                        description = trainSnap.child("description").getValue(String::class.java) ?: "",
                        startDate = trainSnap.child("startdate").getValue(String::class.java) ?: "",
                        endDate = trainSnap.child("enddate").getValue(String::class.java) ?: ""
                    )
                    trainingList.add(mutableStateOf(training))
                }

                dataSnapshot.child("skills").children.forEach { skillSnap ->
                    val skill = skillSnap.child("name").getValue(String::class.java) ?: ""
                    skillsList.add(mutableStateOf(SkillData(name = skill)))
                }
            }
    }


    fun saveCVData() {
        // Verifikojmë nëse të gjitha fushat janë bosh
        val isPersonalInfoEmpty = fullname.isBlank() && email.isBlank() && phonenumber.isBlank()
        val isEducationEmpty = educationList.all {
            it.value.eduName.isBlank() &&
                    it.value.description.isBlank() &&
                    it.value.startDate.isBlank() &&
                    it.value.endDate.isBlank()
        }
        val isExperienceEmpty = experienceList.all {
            it.value.expName.isBlank() &&
                    it.value.description.isBlank() &&
                    it.value.startDate.isBlank() &&
                    it.value.endDate.isBlank()
        }
        val isTrainingEmpty = trainingList.all {
            it.value.trainingName.isBlank() &&
                    it.value.description.isBlank() &&
                    it.value.startDate.isBlank() &&
                    it.value.endDate.isBlank()
        }
        val isSkillsEmpty = skillsList.all {
            it.value.name.isBlank()
        }

        val allEmpty = isPersonalInfoEmpty && isEducationEmpty && isExperienceEmpty && isTrainingEmpty && isSkillsEmpty

        if (allEmpty) {
            Toast.makeText(context, "Please fill at least one section before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val educationMap = hashMapOf<String, Any>()
        educationList.forEachIndexed { index, state ->
            val edu = state.value
            if (edu.eduName.isNotBlank() || edu.description.isNotBlank() || edu.startDate.isNotBlank() || edu.endDate.isNotBlank()) {
                educationMap["edu${index + 1}"] = mapOf(
                    "eduName" to edu.eduName,
                    "description" to edu.description,
                    "startdate" to edu.startDate,
                    "enddate" to edu.endDate
                )
            }
        }

        val experienceMap = hashMapOf<String, Any>()
        experienceList.forEachIndexed { index, state ->
            val exp = state.value
            if (exp.expName.isNotBlank() || exp.description.isNotBlank() || exp.startDate.isNotBlank() || exp.endDate.isNotBlank()) {
                experienceMap["exp${index + 1}"] = mapOf(
                    "expName" to exp.expName,
                    "description" to exp.description,
                    "startdate" to exp.startDate,
                    "enddate" to exp.endDate
                )
            }
        }

        val trainingMap = hashMapOf<String, Any>()
        trainingList.forEachIndexed { index, state ->
            val training = state.value
            if (training.trainingName.isNotBlank() || training.description.isNotBlank() || training.startDate.isNotBlank() || training.endDate.isNotBlank()) {
                trainingMap["tra${index + 1}"] = mapOf(
                    "trainingName" to training.trainingName,
                    "description" to training.description,
                    "startdate" to training.startDate,
                    "enddate" to training.endDate
                )
            }
        }

        val skillsMap = hashMapOf<String, Any>()
        skillsList.forEachIndexed { index, state ->
            if (state.value.name.isNotBlank()) {
                skillsMap["skills${index + 1}"] = mapOf("name" to state.value.name)
            }
        }

        val personalInfoMap = hashMapOf<String, String>()
        if (fullname.isNotBlank()) personalInfoMap["fullname"] = fullname
        if (email.isNotBlank()) personalInfoMap["email"] = email
        if (phonenumber.isNotBlank()) personalInfoMap["phonenumber"] = phonenumber

        val cvData = hashMapOf<String, Any>()
        if (personalInfoMap.isNotEmpty()) cvData["personalinfo"] = personalInfoMap
        if (educationMap.isNotEmpty()) cvData["education"] = educationMap
        if (experienceMap.isNotEmpty()) cvData["experience"] = experienceMap
        if (trainingMap.isNotEmpty()) cvData["trainings"] = trainingMap
        if (skillsMap.isNotEmpty()) cvData["skills"] = skillsMap

        database.child("users").child(userId).child("cvdata").setValue(cvData)
            .addOnCompleteListener {
                coroutineScope.launch {
                    Toast.makeText(context, "CV saved successfully!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                coroutineScope.launch {
                    Toast.makeText(context, "Failed to save CV!", Toast.LENGTH_SHORT).show()
                }
            }
    }


    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader("Personal Information")
        FormField("Full Name", fullname) { fullname = it }
        FormField("Email", email) { email = it }
        FormField("Phone Number", phonenumber) { phonenumber = it }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Education")
        educationList.forEach { eduState ->
            val edu = eduState.value
            FormField("Education Name", edu.eduName) { eduState.value = edu.copy(eduName = it) }
            FormField("Description", edu.description) { eduState.value = edu.copy(description = it) }
            FormField("Start Date", edu.startDate) { eduState.value = edu.copy(startDate = it) }
            FormField("End Date", edu.endDate) { eduState.value = edu.copy(endDate = it) }
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextButton(onClick = { educationList.add(mutableStateOf(EduData())) }) {
            Text("➕ Add more education", color = Color(0xFF00796B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Experience")
        experienceList.forEach { expState ->
            val exp = expState.value
            FormField("Experience Name", exp.expName) { expState.value = exp.copy(expName = it) }
            FormField("Description", exp.description) { expState.value = exp.copy(description = it) }
            FormField("Start Date", exp.startDate) { expState.value = exp.copy(startDate = it) }
            FormField("End Date", exp.endDate) { expState.value = exp.copy(endDate = it) }
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextButton(onClick = { experienceList.add(mutableStateOf(ExpData())) }) {
            Text("➕ Add more experience", color = Color(0xFF00796B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Trainings")
        trainingList.forEach { trainState ->
            val training = trainState.value
            FormField("Training Name", training.trainingName) { trainState.value = training.copy(trainingName = it) }
            FormField("Description", training.description) { trainState.value = training.copy(description = it) }
            FormField("Start Date", training.startDate) { trainState.value = training.copy(startDate = it) }
            FormField("End Date", training.endDate) { trainState.value = training.copy(endDate = it) }
            Spacer(modifier = Modifier.height(8.dp))
        }
        TextButton(onClick = { trainingList.add(mutableStateOf(TrainingData())) }) {
            Text("➕ Add more trainings", color = Color(0xFF00796B))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Skills")
        skillsList.forEach { skillState ->
            FormField("Skill Name", skillState.value.name) {
                skillState.value = SkillData(name = it)
            }
        }
        TextButton(onClick = { skillsList.add(mutableStateOf(SkillData())) }) {
            Text("➕ Add more skills", color = Color(0xFF00796B))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { saveCVData() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
        ) {
            Text("Save CV", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun FormField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
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

@Composable
fun SectionHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            color = Color(0xFF00796B)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 1.dp,
            color = Color(0xFF00796B)
        )
    }
}
