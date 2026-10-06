package com.signaldesk.telerehab.domain.auth

import javax.inject.Inject

class SendPatientPasswordReset @Inject constructor(
    private val authSession: AuthSession,
) {

    suspend operator fun invoke(
        email: String,
    ) {
        require(email.isNotBlank()) {
            "Enter your email address first."
        }

        authSession.sendPasswordResetEmail(
            email = email.trim(),
        )
    }
}
