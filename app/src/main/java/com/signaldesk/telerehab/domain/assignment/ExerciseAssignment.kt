package com.signaldesk.telerehab.domain.assignment

data class ExerciseAssignment(
    val id: String,
    val patientId: String,
    val exerciseId: String,
    val title: String,
    val instructions: String,
    val targetRepetitions: Int,
    val flexedAtOrBelowDegrees: Double? = null,
    val extendedAtOrAboveDegrees: Double? = null,
    val status: ExerciseAssignmentStatus,
) {
    init {
        require(id.isNotBlank())
        require(patientId.isNotBlank())
        require(exerciseId.isNotBlank())
        require(title.isNotBlank())
        require(instructions.isNotBlank())
        require(targetRepetitions > 0)

        require(
            (flexedAtOrBelowDegrees == null) ==
                (extendedAtOrAboveDegrees == null),
        ) {
            "Movement thresholds must both be present or both be absent."
        }

        if (
            flexedAtOrBelowDegrees != null &&
            extendedAtOrAboveDegrees != null
        ) {
            require(
                flexedAtOrBelowDegrees in 0.0..180.0,
            )

            require(
                extendedAtOrAboveDegrees in 0.0..180.0,
            )

            require(
                flexedAtOrBelowDegrees <
                    extendedAtOrAboveDegrees,
            )
        }
    }
}
