package com.signaldesk.telerehab.ui.session

import com.signaldesk.telerehab.domain.analysis.KneeMovementPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionGuidanceTest {

    @Test
    fun kneeFlexionGuidesPatientFromExtendedIntoBend() {
        val state =
            configuredState(
                angle = 145.0,
                phase = KneeMovementPhase.EXTENDED,
            )

        assertEquals(
            "Bend your knee a little more.",
            sessionGuidance(
                exerciseId = "knee-flexion",
                state = state,
            ),
        )
    }

    @Test
    fun kneeFlexionGuidesPatientToStraightenAfterFlexion() {
        val state =
            configuredState(
                angle = 120.0,
                phase = KneeMovementPhase.FLEXED,
            )

        assertEquals(
            "Straighten your knee to complete the repetition.",
            sessionGuidance(
                exerciseId = "knee-flexion",
                state = state,
            ),
        )
    }

    @Test
    fun seatedKneeExtensionGuidesPatientToStraightenFromBentPosition() {
        val state =
            configuredState(
                angle = 120.0,
                phase = KneeMovementPhase.FLEXED,
            )

        assertEquals(
            "Keep straightening your knee.",
            sessionGuidance(
                exerciseId = "seated-knee-extension",
                state = state,
            ),
        )
    }

    @Test
    fun seatedKneeExtensionGuidesPatientBackToBentPositionAfterExtension() {
        val state =
            configuredState(
                angle = 165.0,
                phase = KneeMovementPhase.EXTENDED,
            )

        assertEquals(
            "Bend your knee a little more to reset.",
            sessionGuidance(
                exerciseId = "seated-knee-extension",
                state = state,
            ),
        )
    }

    @Test
    fun missingPoseUsesSharedVisibilityGuidance() {
        val state =
            configuredState(
                angle = null,
                phase = KneeMovementPhase.UNKNOWN,
            )

        assertEquals(
            "Move so your hip, knee, and ankle are clearly visible.",
            sessionGuidance(
                exerciseId = "seated-knee-extension",
                state = state,
            ),
        )
    }

    private fun configuredState(
        angle: Double?,
        phase: KneeMovementPhase,
    ): GuidedSessionAnalysisState =
        GuidedSessionAnalysisState(
            trackedKneeAngleDegrees = angle,
            movementPhase = phase,
            targetRepetitions = 10,
            flexedAtOrBelowDegrees = 100.0,
            extendedAtOrAboveDegrees = 160.0,
            trackingConfigured = true,
        )
}
