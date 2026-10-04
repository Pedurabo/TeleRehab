package com.signaldesk.telerehab.domain.analysis

import org.junit.Assert.assertEquals
import org.junit.Test

class KneeRepetitionTrackerTest {

    private val configuration =
        KneeRepetitionConfiguration(
            flexedAtOrBelowDegrees = 90.0,
            extendedAtOrAboveDegrees = 160.0,
        )

    @Test
    fun countsFlexedToExtendedTransitionAsOneRepetition() {
        val tracker =
            KneeRepetitionTracker(
                configuration = configuration,
            )

        tracker.accept(
            measurement(
                angle = 80.0,
            ),
        )

        val result =
            tracker.accept(
                measurement(
                    angle = 170.0,
                ),
            )

        assertEquals(
            1,
            result.repetitions,
        )

        assertEquals(
            KneeMovementPhase.EXTENDED,
            result.phase,
        )
    }

    @Test
    fun doesNotDoubleCountRepeatedExtendedFrames() {
        val tracker =
            KneeRepetitionTracker(
                configuration = configuration,
            )

        tracker.accept(
            measurement(
                angle = 80.0,
            ),
        )

        tracker.accept(
            measurement(
                angle = 170.0,
            ),
        )

        val result =
            tracker.accept(
                measurement(
                    angle = 175.0,
                ),
            )

        assertEquals(
            1,
            result.repetitions,
        )
    }

    @Test
    fun ignoresIntermediateAnglesForPhaseChanges() {
        val tracker =
            KneeRepetitionTracker(
                configuration = configuration,
            )

        val result =
            tracker.accept(
                measurement(
                    angle = 120.0,
                ),
            )

        assertEquals(
            0,
            result.repetitions,
        )

        assertEquals(
            KneeMovementPhase.UNKNOWN,
            result.phase,
        )
    }

    @Test
    fun thresholdConfigurationIsExternal() {
        val tracker =
            KneeRepetitionTracker(
                configuration =
                    KneeRepetitionConfiguration(
                        flexedAtOrBelowDegrees = 100.0,
                        extendedAtOrAboveDegrees = 150.0,
                    ),
            )

        tracker.accept(
            measurement(
                angle = 95.0,
            ),
        )

        val result =
            tracker.accept(
                measurement(
                    angle = 155.0,
                ),
            )

        assertEquals(
            1,
            result.repetitions,
        )
    }

    private fun measurement(
        angle: Double,
    ) =
        KneeAngleMeasurement(
            side = KneeSide.LEFT,
            angleDegrees = angle,
            confidence = 1f,
        )
}
