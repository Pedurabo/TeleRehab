package com.signaldesk.telerehab.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.therapist.WeeklyAdherenceStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun TherapistHomeScreen(
    state: TherapistHomeUiState,
    onPatientSelected: (String) -> Unit,
    onSaveAssignment: (
        ExerciseAssignment,
        Int,
        Int,
        Double?,
        Double?,
    ) -> Unit,
    onDismissSaveMessage: () -> Unit,
    onNewPatientEmailChanged: (String) -> Unit,
    onNewPatientDisplayNameChanged: (String) -> Unit,
    onAddPatient: () -> Unit,
    onCreateKneeFlexionAssignment: () -> Unit,
    onSignOut: () -> Unit,
) {
    LaunchedEffect(state.saveMessage) {
        if (state.saveMessage != null) {
            delay(2500)
            onDismissSaveMessage()
        }
    }

    var selectedSessionId by remember {
        mutableStateOf<String?>(null)
    }

    val clipboardManager =
        LocalClipboardManager.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Therapist dashboard",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedButton(
            onClick = onSignOut,
        ) {
            Text("Sign out")
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Add patient",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    OutlinedTextField(
                        value = state.newPatientEmail,
                        onValueChange = onNewPatientEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Patient email")
                        },
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = state.newPatientDisplayName,
                        onValueChange =
                            onNewPatientDisplayNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Display name")
                        },
                        singleLine = true,
                    )

                    Button(
                        onClick = onAddPatient,
                        enabled =
                            !state.isAddingPatient &&
                                state.newPatientEmail.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (state.isAddingPatient) {
                                "Adding..."
                            } else {
                                "Add patient"
                            },
                        )
                    }

                    state.addPatientMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }


                    state.generatedPatientCredentials?.let { credentials ->
                        var credentialsCopied by
                            remember(credentials) {
                                mutableStateOf(false)
                            }

                        LaunchedEffect(credentialsCopied) {
                            if (credentialsCopied) {
                                delay(2500)
                                credentialsCopied = false
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Temporary patient credentials",
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                )

                                Text(
                                    text = "Email: ${credentials.email}",
                                )

                                Text(
                                    text =
                                        "Temporary password: ${credentials.temporaryPassword}",
                                )

                                Button(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(
                                                credentials.email,
                                            ),
                                        )
                                        credentialsCopied = true
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                ) {
                                    Text("Copy email")
                                }

                                Button(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(
                                                credentials.temporaryPassword,
                                            ),
                                        )
                                        credentialsCopied = true
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                ) {
                                    Text("Copy password")
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString(
                                                "TeleRehab patient credentials\n" +
                                                    "Email: ${credentials.email}\n" +
                                                    "Temporary password: ${credentials.temporaryPassword}",
                                            ),
                                        )
                                        credentialsCopied = true
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                ) {
                                    Text("Copy all")
                                }

                                if (credentialsCopied) {
                                    Text(
                                        text = "Copied to clipboard",
                                        color =
                                            MaterialTheme.colorScheme.primary,
                                    )
                                }

                                Text(
                                    text =
                                        "Share these one-time credentials with the patient. " +
                                            "They will be required to create a new password on first sign in. " +
                                            "The temporary password is not stored by TeleRehab.",
                                )
                            }
                        }
                    }
                }
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
                            Text(
                                patient.displayName
                                    ?: patient.patientId.toPatientLabel(),
                            )
                        }
                    }
            }

            state.selectedPatientId?.let { patientId ->
                Text(
                    text = "Selected patient",
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    state.patients
                        .firstOrNull { it.patientId == patientId }
                        ?.displayName
                        ?: patientId.toPatientLabel(),
                )

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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "No assignments for this patient.",
                            )

                            Button(
                                onClick =
                                    onCreateKneeFlexionAssignment,
                                enabled =
                                    !state.isSavingAssignment,
                                modifier =
                                    Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (state.isSavingAssignment) {
                                        "Creating..."
                                    } else {
                                        "Create Knee Flexion assignment"
                                    },
                                )
                            }
                        }
                    }
                }

                state.assignments.forEach { assignment ->
                    AssignmentEditor(
                        assignment = assignment,
                        isSaving = state.isSavingAssignment,
                        onSave = onSaveAssignment,
                    )

                    state.assignmentWeeklyAdherence
                        .firstOrNull {
                            it.assignmentId == assignment.id
                        }
                        ?.let { adherence ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "This week",
                                        style =
                                            MaterialTheme.typography.titleSmall,
                                    )

                                    Text(
                                        "Completed: ${adherence.completedSessions} / ${adherence.targetSessions}",
                                    )

                                    Text(
                                        "Adherence: ${adherence.percent}%",
                                    )

                                    Text(
                                        "Status: ${
                                            when (adherence.status) {
                                                WeeklyAdherenceStatus.NOT_STARTED ->
                                                    "Not started"

                                                WeeklyAdherenceStatus.IN_PROGRESS ->
                                                    "In progress"

                                                WeeklyAdherenceStatus.COMPLETE ->
                                                    "Complete"
                                            }
                                        }",
                                    )
                                }
                            }
                        }
                }

                if (
                    state.latestCompletedRepetitions != null ||
                    state.repetitionTrend != null
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Recent performance",
                                style = MaterialTheme.typography.titleMedium,
                            )

                            state.latestCompletedRepetitions?.let {
                                Text(
                                    "Latest completed session: $it repetitions",
                                )
                            }

                            state.repetitionTrend?.let { trend ->
                                Text(
                                    "Change from previous session: ${
                                        if (trend > 0) "+$trend" else trend
                                    } repetitions",
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Recent sessions",
                    style = MaterialTheme.typography.titleMedium,
                )

                if (state.recentSessions.isEmpty()) {
                    Text("No completed sessions yet.")
                } else {
                    state.recentSessions.forEach { session ->
                        val isSelected =
                            selectedSessionId == session.id

                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSessionId =
                                            if (isSelected) {
                                                null
                                            } else {
                                                session.id
                                            }
                                    },
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text =
                                        session.completedAt
                                            .toDisplayDateTime(),
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                )

                                session.metrics?.let { metrics ->
                                    Text(
                                        "${metrics.completedRepetitions} repetitions",
                                    )

                                    if (isSelected) {
                                        Text(
                                            "Session ID: ${session.id}",
                                        )

                                        Text(
                                            "Assignment: ${session.assignmentId}",
                                        )

                                        Text(
                                            "Started: ${session.startedAt.toDisplayDateTime()}",
                                        )

                                        metrics.minimumKneeAngleDegrees
                                            ?.let { angle ->
                                                Text(
                                                    "Minimum knee angle: $angle degrees",
                                                )
                                            }

                                        metrics.maximumKneeAngleDegrees
                                            ?.let { angle ->
                                                Text(
                                                    "Maximum knee angle: $angle degrees",
                                                )
                                            }

                                        Text("Tap again to close")
                                    } else {
                                        Text("Tap for details")
                                    }
                                } ?: run {
                                    Text("No derived metrics recorded.")

                                    if (isSelected) {
                                        Text(
                                            "Session ID: ${session.id}",
                                        )

                                        Text(
                                            "Assignment: ${session.assignmentId}",
                                        )

                                        Text(
                                            "Started: ${session.startedAt.toDisplayDateTime()}",
                                        )

                                        Text("Tap again to close")
                                    } else {
                                        Text("Tap for details")
                                    }
                                }
                            }
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

    var sessionsPerWeek by remember(assignment) {
        mutableStateOf(assignment.targetSessionsPerWeek.toString())
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
    val parsedSessionsPerWeek = sessionsPerWeek.toIntOrNull()
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
            parsedSessionsPerWeek != null &&
            parsedSessionsPerWeek > 0 &&
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

                Text(
                    "Target sessions per week: ${assignment.targetSessionsPerWeek}",
                )

                assignment.flexedAtOrBelowDegrees?.let {
                    Text("Flexed at or below: $it degrees")
                }

                assignment.extendedAtOrAboveDegrees?.let {
                    Text("Extended at or above: $it degrees")
                }

                Button(
                    onClick = {
                        repetitions =
                            assignment.targetRepetitions.toString()
                        sessionsPerWeek =
                            assignment.targetSessionsPerWeek.toString()
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
                    value = sessionsPerWeek,
                    onValueChange = { sessionsPerWeek = it },
                    label = {
                        Text("Target sessions per week")
                    },
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
                            checkNotNull(parsedSessionsPerWeek),
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
                        sessionsPerWeek =
                            assignment.targetSessionsPerWeek.toString()
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


private fun String.toPatientLabel(): String =
    if (length > 10) {
        "Patient ${take(6)}..."
    } else {
        "Patient $this"
    }

private val sessionDateFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a")

private fun java.time.Instant?.toDisplayDateTime(): String =
    this
        ?.atZone(ZoneId.systemDefault())
        ?.format(sessionDateFormatter)
        ?: "Unknown"
