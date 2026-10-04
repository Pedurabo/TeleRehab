package com.signaldesk.telerehab.domain.analysis

data class KneeRepetitionConfiguration(
    val flexedAtOrBelowDegrees: Double,
    val extendedAtOrAboveDegrees: Double,
) {
    init {
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

enum class KneeMovementPhase {
    UNKNOWN,
    FLEXED,
    EXTENDED,
}

data class KneeRepetitionState(
    val repetitions: Int = 0,
    val phase: KneeMovementPhase =
        KneeMovementPhase.UNKNOWN,
)

class KneeRepetitionTracker(
    private val configuration:
        KneeRepetitionConfiguration,
) {
    private var state =
        KneeRepetitionState()

    fun currentState():
        KneeRepetitionState =
        state

    fun reset() {
        state =
            KneeRepetitionState()
    }

    fun accept(
        measurement: KneeAngleMeasurement,
    ): KneeRepetitionState {
        val nextPhase =
            when {
                measurement.angleDegrees <=
                    configuration.flexedAtOrBelowDegrees ->
                    KneeMovementPhase.FLEXED

                measurement.angleDegrees >=
                    configuration.extendedAtOrAboveDegrees ->
                    KneeMovementPhase.EXTENDED

                else ->
                    state.phase
            }

        val completedRepetition =
            state.phase ==
                KneeMovementPhase.FLEXED &&
                nextPhase ==
                KneeMovementPhase.EXTENDED

        state =
            KneeRepetitionState(
                repetitions =
                    state.repetitions +
                        if (completedRepetition) 1 else 0,
                phase = nextPhase,
            )

        return state
    }
}
