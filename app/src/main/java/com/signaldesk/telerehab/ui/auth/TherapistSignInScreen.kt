package com.signaldesk.telerehab.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun TherapistSignInScreen(
    state: TherapistSignInUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp),
    ) {
        Text("Therapist sign in")

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = {
                Text("Email")
            },
            singleLine = true,
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = {
                Text("Password")
            },
            visualTransformation =
                PasswordVisualTransformation(),
            singleLine = true,
        )

        Button(
            onClick = onSignIn,
            enabled = !state.isSigningIn,
        ) {
            Text(
                if (state.isSigningIn) {
                    "Signing in..."
                } else {
                    "Sign in"
                },
            )
        }

        state.errorMessage?.let { message ->
            Text(message)
        }
    }
}
