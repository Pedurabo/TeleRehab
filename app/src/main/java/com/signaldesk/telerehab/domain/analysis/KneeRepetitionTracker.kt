package com.signaldesk.telerehab.domain.analysis

data class KneeRepetitionConfiguration(
    val flexedAtOrBelowDegrees: Double,
    val extendedAtOrAboveDegrees: Double,
    val stableFramesRequired: Int = 3,
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

        require(
            stableFramesRequired > 0,
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

    private var candidatePhase:
        KneeMovementPhase? = null

    private var candidateFrameCount:
        Int = 0

    fun currentState():
        KneeRepetitionState =
        state

    fun reset() {
        state =
            KneeRepetitionState()

        candidatePhase =
            null

        candidateFrameCount =
            0
    }

    fun accept(
        measurement: KneeAngleMeasurement,
    ): KneeRepetitionState {
        val observedPhase =
            when {
                measurement.angleDegrees <=
                    configuration.flexedAtOrBelowDegrees ->
                    KneeMovementPhase.FLEXED

                measurement.angleDegrees >=
                    configuration.extendedAtOrAboveDegrees ->
                    KneeMovementPhase.EXTENDED

                else ->
                    null
            }

        if (
            observedPhase == null ||
            observedPhase == state.phase
        ) {
            candidatePhase =
                null

            candidateFrameCount =
                0

            return state
        }

        if (candidatePhase == observedPhase) {
            candidateFrameCount += 1
        } else {
            candidatePhase =
                observedPhase

            candidateFrameCount =
                1
        }

        if (
            candidateFrameCount <
            configuration.stableFramesRequired
        ) {
            return state
        }

        val completedRepetition =
            state.phase ==
                KneeMovementPhase.FLEXED &&
                observedPhase ==
                KneeMovementPhase.EXTENDED

        state =
            KneeRepetitionState(
                repetitions =
                    state.repetitions +
                        if (completedRepetition) {
                            1
                        } else {
                            0
                        },
                phase =
                    observedPhase,
            )

        candidatePhase =
            null

        candidateFrameCount =
            0

        return state
    }
}
