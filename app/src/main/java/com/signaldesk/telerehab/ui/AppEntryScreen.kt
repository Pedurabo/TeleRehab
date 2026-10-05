package com.signaldesk.telerehab.ui

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
fun AppEntryScreen(
    onContinueAsPatient: () -> Unit,
    onTherapistSignIn: () -> Unit,
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
            text = "TeleRehab",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Choose how you want to continue.",
        )

        Button(
            onClick = onContinueAsPatient,
        ) {
            Text("Continue as patient")
        }

        Button(
            onClick = onTherapistSignIn,
        ) {
            Text("Therapist sign in")
        }
    }
}
