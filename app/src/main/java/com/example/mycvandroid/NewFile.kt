package com.example.mycvandroid

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.Calendar
import java.util.Locale

data class EduData(var eduName: String = "", var description: String = "", var startDate: String = "", var endDate: String = "")
data class ExpData(var expName: String = "", var description: String = "", var startDate: String = "", var endDate: String = "")
data class TrainingData(var trainingName: String = "", var description: String = "", var startDate: String = "", var endDate: String = "")
data class SkillData(var name: String = "")

@Composable
fun NewFileScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val database: DatabaseReference = Firebase.database.reference

    if (userId == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please sign in to save your CV!", color = Color.Red, fontSize = 18.sp)
        }
        return
    }

    var fullname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }

    val educationList = remember { mutableStateListOf(mutableStateOf(EduData())) }
    val experienceList = remember { mutableStateListOf(mutableStateOf(ExpData())) }
    val trainingList = remember { mutableStateListOf(mutableStateOf(TrainingData())) }
    val skillsList = remember { mutableStateListOf(mutableStateOf(SkillData())) }

    LaunchedEffect(userId) {
        database.child("users/$userId/cvdata").get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.child("personalinfo").let {
                fullname = it.child("fullname").getValue(String::class.java) ?: ""
                email = it.child("email").getValue(String::class.java) ?: ""
                phonenumber = it.child("phonenumber").getValue(String::class.java) ?: ""
            }

            fun <T> clearAndFill(list: SnapshotStateList<MutableState<T>>, newItems: List<T>) {
                list.clear()
                list.addAll(newItems.map { mutableStateOf(it) })
            }

            clearAndFill(educationList, dataSnapshot.child("education").children.mapNotNull {
                it.getValue(EduData::class.java)
            })

            clearAndFill(experienceList, dataSnapshot.child("experience").children.mapNotNull {
                it.getValue(ExpData::class.java)
            })

            clearAndFill(trainingList, dataSnapshot.child("trainings").children.mapNotNull {
                it.getValue(TrainingData::class.java)
            })

            clearAndFill(skillsList, dataSnapshot.child("skills").children.mapNotNull {
                it.child("name").getValue(String::class.java)?.let { name -> SkillData(name) }
            })
        }
    }

    fun saveCVData() {
        val isAllEmpty = fullname.isBlank() && email.isBlank() && phonenumber.isBlank() &&
                educationList.all { it.value.eduName.isBlank() && it.value.description.isBlank() && it.value.startDate.isBlank() && it.value.endDate.isBlank() } &&
                experienceList.all { it.value.expName.isBlank() && it.value.description.isBlank() && it.value.startDate.isBlank() && it.value.endDate.isBlank() } &&
                trainingList.all { it.value.trainingName.isBlank() && it.value.description.isBlank() && it.value.startDate.isBlank() && it.value.endDate.isBlank() } &&
                skillsList.all { it.value.name.isBlank() }

        if (isAllEmpty) {
            Toast.makeText(context, "Please fill at least one section before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val cvData = mutableMapOf<String, Any>()

        if (fullname.isNotBlank() || email.isNotBlank() || phonenumber.isNotBlank()) {
            cvData["personalinfo"] = mapOf(
                "fullname" to fullname,
                "email" to email,
                "phonenumber" to phonenumber
            )
        }

        fun <T> buildListMap(list: SnapshotStateList<MutableState<T>>, keyPrefix: String, builder: (T) -> Map<String, String>): Map<String, Any> {
            return list.mapIndexedNotNull { index, item ->
                val data = builder(item.value)
                if (data.values.any { it.isNotBlank() }) "$keyPrefix${index + 1}" to data else null
            }.toMap()
        }


        cvData["education"] = buildListMap(educationList, "edu") {
            mapOf(
                "eduName" to it.eduName,
                "description" to it.description,
                "startDate" to it.startDate,
                "endDate" to it.endDate
            )
        }

        cvData["experience"] = buildListMap(experienceList, "exp") {
            mapOf(
                "expName" to it.expName,
                "description" to it.description,
                "startDate" to it.startDate,
                "endDate" to it.endDate
            )
        }

        cvData["trainings"] = buildListMap(trainingList, "tra") {
            mapOf(
                "trainingName" to it.trainingName,
                "description" to it.description,
                "startDate" to it.startDate,
                "endDate" to it.endDate
            )
        }

        cvData["skills"] = buildListMap(skillsList, "skills") {
            mapOf("name" to it.name)
        }

        database.child("users/$userId/cvdata").updateChildren(cvData).addOnSuccessListener {
            Toast.makeText(context, "CV saved successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to save CV!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create New CV", fontSize = 20.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader("Personal Info")
        FormField("Full Name", fullname) { fullname = it }
        FormField("Email", email) { email = it }
        FormField("Phone Number", phonenumber) { phonenumber = it }

        Section("Education", educationList, { EduData() }) { edu, onChange ->
            FormField("Education Name", edu.eduName) { onChange(edu.copy(eduName = it)) }
            FormField("Description", edu.description) { onChange(edu.copy(description = it)) }
            DatePickerField("Start Date", edu.startDate) { onChange(edu.copy(startDate = it)) }
            DatePickerField("End Date", edu.endDate) { onChange(edu.copy(endDate = it)) }
        }

        Section("Experience", experienceList, { ExpData() }) { exp, onChange ->
            FormField("Experience Name", exp.expName) { onChange(exp.copy(expName = it)) }
            FormField("Description", exp.description) { onChange(exp.copy(description = it)) }
            DatePickerField("Start Date", exp.startDate) { onChange(exp.copy(startDate = it)) }
            DatePickerField("End Date", exp.endDate) { onChange(exp.copy(endDate = it)) }
        }

        Section("Trainings", trainingList, { TrainingData() }) { tr, onChange ->
            FormField("Training Name", tr.trainingName) { onChange(tr.copy(trainingName = it)) }
            FormField("Description", tr.description) { onChange(tr.copy(description = it)) }
            DatePickerField("Start Date", tr.startDate) { onChange(tr.copy(startDate = it)) }
            DatePickerField("End Date", tr.endDate) { onChange(tr.copy(endDate = it)) }
        }

        Section("Skills", skillsList, { SkillData() }) { skill, onChange ->
            FormField("Skill", skill.name) { onChange(SkillData(it)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { saveCVData() }, modifier = Modifier.fillMaxWidth()) {
            Text("Save CV")
        }
    }
}

@Composable
fun <T> Section(
    title: String,
    list: SnapshotStateList<MutableState<T>>,
    createNew: () -> T,
    itemContent: @Composable (T, (T) -> Unit) -> Unit
) {
    SectionHeader(title)
    list.forEach { state ->
        itemContent(state.value) { state.value = it }
        Spacer(modifier = Modifier.height(8.dp))
    }
    TextButton(onClick = { list.add(mutableStateOf(createNew())) }) {
        Text("➕ Add more $title", color = Color(0xFF00796B))
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun FormField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
        Text(text = title, fontSize = 18.sp, color = Color(0xFF00796B))
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 1.dp, color = Color(0xFF00796B))
    }
}

@Composable
fun DatePickerField(label: String, date: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val formatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val (day, month, year) = if (date.isNotEmpty()) {
        val parts = date.split("/")
        Triple(
            parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH),
            parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH),
            parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        )
    } else Triple(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
        val datePicker = android.app.DatePickerDialog(context, { _, y, m, d ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(y, m, d)
            onDateSelected(formatter.format(selectedDate.time))
        }, year, month, day)
        datePicker.show()
    }) {
        OutlinedTextField(
            value = date,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color(0xFF00796B)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
