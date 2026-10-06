package com.signaldesk.telerehab.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PatientChangePasswordScreen(
    state: PatientChangePasswordUiState,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp),
    ) {
        Text("Create your password")

        Text(
            "Your therapist gave you a temporary password. " +
                "Create a new password before continuing.",
        )

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = onNewPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("New password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true,
        )

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Confirm password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true,
        )

        Button(
            onClick = onChangePassword,
            enabled = !state.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.isWorking) {
                    "Updating..."
                } else {
                    "Save new password"
                },
            )
        }

        OutlinedButton(
            onClick = onSignOut,
            enabled = !state.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign out")
        }

        state.errorMessage?.let { message ->
            Text(message)
        }
    }
}
