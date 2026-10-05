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
            data.requireString(
                key = "id",
            )

        require(id == documentId) {
            "Assignment document ID must match assignment ID."
        }

        return ExerciseAssignment(
            id = id,
            patientId =
                data.requireString(
                    key = "patientId",
                ),
            exerciseId =
                data.requireString(
                    key = "exerciseId",
                ),
            title =
                data.requireString(
                    key = "title",
                ),
            instructions =
                data.requireString(
                    key = "instructions",
                ),
            targetRepetitions =
                data.requireNumber(
                    key = "targetRepetitions",
                ).toInt(),
            flexedAtOrBelowDegrees =
                (
                    data["flexedAtOrBelowDegrees"]
                        as? Number
                )?.toDouble(),
            extendedAtOrAboveDegrees =
                (
                    data["extendedAtOrAboveDegrees"]
                        as? Number
                )?.toDouble(),
            status =
                ExerciseAssignmentStatus.valueOf(
                    data.requireString(
                        key = "status",
                    ),
                ),
        )
    }

    private fun Map<String, Any?>.requireString(
        key: String,
    ): String {
        val value =
            this[key] as? String

        require(
            !value.isNullOrBlank(),
        ) {
            "Missing or invalid Firestore string field: $key"
        }

        return value
    }

    private fun Map<String, Any?>.requireNumber(
        key: String,
    ): Number =
        requireNotNull(
            this[key] as? Number,
        ) {
            "Missing or invalid Firestore numeric field: $key"
        }
}
