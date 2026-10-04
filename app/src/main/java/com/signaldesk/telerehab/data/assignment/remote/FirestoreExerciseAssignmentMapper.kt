package com.signaldesk.telerehab.data.assignment.remote

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import javax.inject.Inject

class FirestoreExerciseAssignmentMapper @Inject constructor() {

    fun toDomain(
        documentId: String,
        data: Map<String, Any?>,
    ): ExerciseAssignment {
        val id =
            data.requiredString(
                field = "id",
            )

        require(id == documentId) {
            "Assignment document ID must match its id field."
        }

        val targetRepetitions =
            (data["targetRepetitions"] as? Number)
                ?.toInt()
                ?: error(
                    "Assignment field targetRepetitions is required.",
                )

        return ExerciseAssignment(
            id = id,
            patientId =
                data.requiredString(
                    field = "patientId",
                ),
            exerciseId =
                data.requiredString(
                    field = "exerciseId",
                ),
            title =
                data.requiredString(
                    field = "title",
                ),
            instructions =
                data.requiredString(
                    field = "instructions",
                ),
            targetRepetitions = targetRepetitions,
            status =
                ExerciseAssignmentStatus.valueOf(
                    data.requiredString(
                        field = "status",
                    ),
                ),
        )
    }

    private fun Map<String, Any?>.requiredString(
        field: String,
    ): String =
        (this[field] as? String)
            ?.takeIf(String::isNotBlank)
            ?: error(
                "Assignment field $field is required.",
            )
}
