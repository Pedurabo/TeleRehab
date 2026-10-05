package com.signaldesk.telerehab.domain.therapist

import javax.inject.Inject

class AddPatientForTherapist @Inject constructor(
    private val repository: TherapistPatientRepository,
) {

    suspend operator fun invoke(
        therapistId: String,
        email: String,
        displayName: String?,
    ): PatientCredentials {
        require(therapistId.isNotBlank())
        require(email.isNotBlank())

        return repository.addPatient(
            therapistId = therapistId,
            email = email.trim(),
            displayName = displayName,
        )
    }
}
