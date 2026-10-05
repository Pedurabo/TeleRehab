package com.signaldesk.telerehab.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TherapistHomeScreen(
    state: TherapistHomeUiState,
    onPatientSelected: (String) -> Unit,
) {
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

        when {
            state.isLoading ->
                Text("Loading patients...")

            state.errorMessage != null ->
                Text(state.errorMessage)

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

            if (state.assignments.isEmpty()) {
                Text("No assignments for this patient.")
            } else {
                state.assignments.forEach { assignment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Text(assignment.instructions)

                            Text(
                                "Target repetitions: ${assignment.targetRepetitions}",
                            )

                            Text(
                                "Status: ${assignment.status}",
                            )

                            assignment.flexedAtOrBelowDegrees?.let {
                                Text("Flexed at or below: $it°")
                            }

                            assignment.extendedAtOrAboveDegrees?.let {
                                Text("Extended at or above: $it°")
                            }
                        }
                    }
                }
            }
        }
    }
}
