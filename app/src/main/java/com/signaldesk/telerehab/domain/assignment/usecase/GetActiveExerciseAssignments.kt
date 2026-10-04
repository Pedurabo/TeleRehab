package com.signaldesk.telerehab.domain.assignment.usecase

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import javax.inject.Inject

class GetActiveExerciseAssignments @Inject constructor(
    private val repository: ExerciseAssignmentRepository,
) {

    suspend operator fun invoke(
        patientId: String,
    ): List<ExerciseAssignment> {
        require(patientId.isNotBlank()) {
            "Patient ID must not be blank."
        }

        return repository.findActiveByPatient(
            patientId = patientId,
        )
    }
}
