package com.signaldesk.telerehab.domain.assignment

data class ExerciseAssignment(
    val id: String,
    val patientId: String,
    val exerciseId: String,
    val title: String,
    val instructions: String,
    val targetRepetitions: Int,
    val status: ExerciseAssignmentStatus,
) {
    init {
        require(id.isNotBlank()) {
            "Assignment ID must not be blank."
        }

        require(patientId.isNotBlank()) {
            "Patient ID must not be blank."
        }

        require(exerciseId.isNotBlank()) {
            "Exercise ID must not be blank."
        }

        require(title.isNotBlank()) {
            "Exercise title must not be blank."
        }

        require(instructions.isNotBlank()) {
            "Exercise instructions must not be blank."
        }

        require(targetRepetitions > 0) {
            "Target repetitions must be greater than zero."
        }
    }
}
