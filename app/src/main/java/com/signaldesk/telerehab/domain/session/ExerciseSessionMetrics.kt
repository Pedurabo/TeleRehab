package com.signaldesk.telerehab.domain.session

data class ExerciseSessionMetrics(
    val completedRepetitions: Int,
    val minimumKneeAngleDegrees: Double? = null,
    val maximumKneeAngleDegrees: Double? = null,
) {
    init {
        require(completedRepetitions >= 0)

        require(
            (minimumKneeAngleDegrees == null) ==
                (maximumKneeAngleDegrees == null),
        ) {
            "Knee-angle summary values must both be present or both be absent."
        }

        if (
            minimumKneeAngleDegrees != null &&
            maximumKneeAngleDegrees != null
        ) {
            require(
                minimumKneeAngleDegrees in 0.0..180.0,
            )

            require(
                maximumKneeAngleDegrees in 0.0..180.0,
            )

            require(
                minimumKneeAngleDegrees <=
                    maximumKneeAngleDegrees,
            )
        }
    }
}
