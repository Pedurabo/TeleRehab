package com.signaldesk.telerehab.domain.therapist

data class PatientCredentials(
    val patientId: String,
    val email: String,
    val temporaryPassword: String,
)
