package com.signaldesk.telerehab.domain.therapist

import javax.inject.Inject

class GetAssignedPatients @Inject constructor(
    private val repository: TherapistPatientRepository,
) {

    suspend operator fun invoke(
        therapistId: String,
    ): List<TherapistPatient> {
        require(therapistId.isNotBlank())

        return repository.findPatientsForTherapist(therapistId)
    }
}
