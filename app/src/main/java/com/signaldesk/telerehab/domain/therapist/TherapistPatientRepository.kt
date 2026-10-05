package com.signaldesk.telerehab.domain.therapist

interface TherapistPatientRepository {

    suspend fun findPatientsForTherapist(
        therapistId: String,
    ): List<TherapistPatient>

    suspend fun isAssigned(
        therapistId: String,
        patientId: String,
    ): Boolean
}
