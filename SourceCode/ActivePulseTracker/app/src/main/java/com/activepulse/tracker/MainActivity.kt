package com.activepulse.tracker

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.activepulse.tracker.ui.theme.ActivePulseTrackerTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isHealthConnectAvailable = HealthConnectUtils.init(this)
        setContent {
            ActivePulseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitnessTrackerScreen(isHealthConnectAvailable)
                }
            }
        }
    }
}

@Composable
fun FitnessTrackerScreen(isHealthConnectAvailable: Boolean) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("goals", Context.MODE_PRIVATE) }

    var stepsGoal by remember { mutableStateOf(sharedPrefs.getLong("steps_goal", 10000L)) }
    var workoutGoal by remember { mutableStateOf(sharedPrefs.getLong("workout_goal", 30L)) }
    var currentSteps by remember { mutableStateOf(0L) }
    var currentWorkout by remember { mutableStateOf(0L) }
    var permissionsGranted by remember { mutableStateOf(false) }

    if (!isHealthConnectAvailable) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Health Connect is not available on this device. This may be due to a system issue. Please check for OS updates.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        HealthConnectUtils.requestPermissionsActivityContract()
    ) { granted ->
        coroutineScope.launch {
            permissionsGranted = granted.containsAll(HealthConnectUtils.PERMISSIONS)
            if (permissionsGranted) {
                loadData { steps, workout ->
                    currentSteps = steps
                    currentWorkout = workout
                }
            } else {
                Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionsGranted = HealthConnectUtils.checkPermissions()
        if (permissionsGranted) {
            loadData { steps, workout ->
                currentSteps = steps
                currentWorkout = workout
            }
        } else {
            permissionsLauncher.launch(HealthConnectUtils.PERMISSIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- Header ---
        Text(
            text = "ActivePulse",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Your Daily Goal Tracker",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Progress Cards ---
        ProgressCard(
            title = "Steps",
            current = currentSteps,
            goal = stepsGoal,
            unit = "steps"
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProgressCard(
            title = "Workout",
            current = currentWorkout,
            goal = workoutGoal,
            unit = "minutes"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Goal Setting Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                "Set Your Goals",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            var stepsInput by remember { mutableStateOf("") }
            var workoutInput by remember { mutableStateOf("") }

            GoalTextField(
                value = stepsInput,
                onValueChange = { stepsInput = it },
                label = "Daily Steps Goal",
                onSave = {
                    stepsGoal = stepsInput.toLongOrNull() ?: 10000L
                    sharedPrefs.edit().putLong("steps_goal", stepsGoal).apply()
                    stepsInput = ""
                    Toast.makeText(context, "Steps goal saved!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoalTextField(
                value = workoutInput,
                onValueChange = { workoutInput = it },
                label = "Daily Workout Goal (minutes)",
                onSave = {
                    workoutGoal = workoutInput.toLongOrNull() ?: 30L
                    sharedPrefs.edit().putLong("workout_goal", workoutGoal).apply()
                    workoutInput = ""
                    Toast.makeText(context, "Workout goal saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Refresh Button ---
        Button(
            onClick = {
                coroutineScope.launch {
                    loadData { steps, workout ->
                        currentSteps = steps
                        currentWorkout = workout
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Refresh Data", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ProgressCard(title: String, current: Long, goal: Long, unit: String) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).roundToInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text("$progressPercent%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("$current / $goal $unit", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
fun GoalTextField(value: String, onValueChange: (String) -> Unit, label: String, onSave: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            Button(
                onClick = onSave,
                enabled = value.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Save")
            }
        }
    )
}


private suspend fun loadData(update: (Long, Long) -> Unit) {
    val now = ZonedDateTime.now()
    val startOfDay = now.toLocalDate().atStartOfDay(now.zone).toInstant()
    val endOfDay = Instant.now()

    val steps = HealthConnectUtils.readDailySteps(startOfDay, endOfDay)
    val workoutMinutes = HealthConnectUtils.readDailyWorkoutMinutes(startOfDay, endOfDay)
    update(steps, workoutMinutes)
}