package com.signaldesk.telerehab.ui.therapist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
                .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp),
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
                            onPatientSelected(
                                patient.patientId,
                            )
                        },
                    ) {
                        Text(
                            text = patient.patientId,
                        )
                    }
                }
        }

        state.selectedPatientId?.let { patientId ->
            Text(
                text = "Selected patient: $patientId",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "Assignments: ${state.assignments.size}",
            )
        }
    }
}
