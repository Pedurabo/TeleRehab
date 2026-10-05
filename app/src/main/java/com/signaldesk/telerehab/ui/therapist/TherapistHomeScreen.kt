package com.signaldesk.telerehab.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import kotlinx.coroutines.delay

@Composable
fun TherapistHomeScreen(
    state: TherapistHomeUiState,
    onPatientSelected: (String) -> Unit,
    onSaveAssignment: (
        ExerciseAssignment,
        Int,
        Double?,
        Double?,
    ) -> Unit,
    onDismissSaveMessage: () -> Unit,
) {
    LaunchedEffect(state.saveMessage) {
        if (state.saveMessage != null) {
            delay(2500)
            onDismissSaveMessage()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Therapist dashboard",
            style = MaterialTheme.typography.headlineMedium,
        )

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
            )
        }

        when {
            state.isLoading ->
                Text("Loading patients...")

            state.patients.isEmpty() ->
                Text("No assigned patients.")

            else ->
                state.patients.forEach { patient ->
                    Button(
                        onClick = {
                            onPatientSelected(patient.patientId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(patient.patientId)
                    }
                }
        }

        state.selectedPatientId?.let { patientId ->
            Text(
                text = "Selected patient",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(patientId)

            Text(
                text = "Assignments",
                style = MaterialTheme.typography.titleMedium,
            )

            state.saveMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (state.assignments.isEmpty()) {
                Text("No assignments for this patient.")
            }

            state.assignments.forEach { assignment ->
                AssignmentEditor(
                    assignment = assignment,
                    isSaving = state.isSavingAssignment,
                    onSave = onSaveAssignment,
                )
            }

            Text(
                text = "Recent sessions",
                style = MaterialTheme.typography.titleMedium,
            )

            if (state.recentSessions.isEmpty()) {
                Text("No completed sessions yet.")
            } else {
                state.recentSessions.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Session ${session.id}",
                                style = MaterialTheme.typography.titleSmall,
                            )

                            Text("Completed: ${session.completedAt}")

                            session.metrics?.let { metrics ->
                                Text(
                                    "Repetitions: ${metrics.completedRepetitions}",
                                )

                                metrics.minimumKneeAngleDegrees?.let { angle ->
                                    Text(
                                        "Minimum knee angle: $angle degrees",
                                    )
                                }

                                metrics.maximumKneeAngleDegrees?.let { angle ->
                                    Text(
                                        "Maximum knee angle: $angle degrees",
                                    )
                                }
                            } ?: Text(
                                "No derived metrics recorded.",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentEditor(
    assignment: ExerciseAssignment,
    isSaving: Boolean,
    onSave: (
        ExerciseAssignment,
        Int,
        Double?,
        Double?,
    ) -> Unit,
) {
    var isEditing by remember(assignment.id) {
        mutableStateOf(false)
    }

    var repetitions by remember(assignment) {
        mutableStateOf(assignment.targetRepetitions.toString())
    }

    var flexedDegrees by remember(assignment) {
        mutableStateOf(
            assignment.flexedAtOrBelowDegrees?.toString().orEmpty(),
        )
    }

    var extendedDegrees by remember(assignment) {
        mutableStateOf(
            assignment.extendedAtOrAboveDegrees?.toString().orEmpty(),
        )
    }

    val parsedRepetitions = repetitions.toIntOrNull()
    val parsedFlexed = flexedDegrees.toDoubleOrNull()
    val parsedExtended = extendedDegrees.toDoubleOrNull()

    val thresholdsValid =
        (flexedDegrees.isBlank() && extendedDegrees.isBlank()) ||
            (
                parsedFlexed != null &&
                    parsedExtended != null &&
                    parsedFlexed in 0.0..180.0 &&
                    parsedExtended in 0.0..180.0 &&
                    parsedFlexed < parsedExtended
            )

    val canSave =
        parsedRepetitions != null &&
            parsedRepetitions > 0 &&
            thresholdsValid &&
            !isSaving

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(assignment.instructions)
            Text("Status: ${assignment.status}")

            if (!isEditing) {
                Text(
                    "Target repetitions: ${assignment.targetRepetitions}",
                )

                assignment.flexedAtOrBelowDegrees?.let {
                    Text("Flexed at or below: $it°")
                }

                assignment.extendedAtOrAboveDegrees?.let {
                    Text("Extended at or above: $it°")
                }

                Button(
                    onClick = {
                        repetitions =
                            assignment.targetRepetitions.toString()
                        flexedDegrees =
                            assignment.flexedAtOrBelowDegrees
                                ?.toString()
                                .orEmpty()
                        extendedDegrees =
                            assignment.extendedAtOrAboveDegrees
                                ?.toString()
                                .orEmpty()
                        isEditing = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Edit assignment")
                }
            } else {
                OutlinedTextField(
                    value = repetitions,
                    onValueChange = { repetitions = it },
                    label = { Text("Target repetitions") },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = flexedDegrees,
                    onValueChange = { flexedDegrees = it },
                    label = {
                        Text("Flexed at or below (degrees)")
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = extendedDegrees,
                    onValueChange = { extendedDegrees = it },
                    label = {
                        Text("Extended at or above (degrees)")
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        onSave(
                            assignment,
                            checkNotNull(parsedRepetitions),
                            parsedFlexed,
                            parsedExtended,
                        )
                        isEditing = false
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (isSaving) {
                            "Saving..."
                        } else {
                            "Save changes"
                        },
                    )
                }

                OutlinedButton(
                    onClick = {
                        repetitions =
                            assignment.targetRepetitions.toString()
                        flexedDegrees =
                            assignment.flexedAtOrBelowDegrees
                                ?.toString()
                                .orEmpty()
                        extendedDegrees =
                            assignment.extendedAtOrAboveDegrees
                                ?.toString()
                                .orEmpty()
                        isEditing = false
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
