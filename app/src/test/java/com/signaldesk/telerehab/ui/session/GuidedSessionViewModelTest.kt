package com.signaldesk.telerehab.ui.session

import com.signaldesk.telerehab.domain.analysis.BodyLandmark
import com.signaldesk.telerehab.domain.analysis.CalculateKneeAngle
import com.signaldesk.telerehab.domain.analysis.NormalizedPosePoint
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuidedSessionViewModelTest {

    @Test
    fun transientLossDoesNotSwitchTrackedLeg() {
        val viewModel =
            newViewModel()

        viewModel.configureTracking(
            targetRepetitions = 10,
            flexedAtOrBelowDegrees = 90.0,
            extendedAtOrAboveDegrees = 160.0,
        )

        // Left is selected initially because its confidence is higher.
        viewModel.submitObservation(
            observationWithBothLegs(
                leftConfidence = 0.9f,
                rightConfidence = 0.8f,
            ),
        )

        assertEquals(
            180.0,
            requireNotNull(
                viewModel.analysisState.value
                    .trackedKneeAngleDegrees,
            ),
            0.01,
        )

        repeat(4) {
            viewModel.submitObservation(
                rightLegOnlyObservation(),
            )
        }

        // Four missing frames are deliberately tolerated.
        assertNull(
            viewModel.analysisState.value
                .trackedKneeAngleDegrees,
        )

        // If the old left-leg lock had switched early,
        // this would already report the right-leg angle.
        assertEquals(
            0,
            viewModel.analysisState.value.repetitions,
        )
    }

    @Test
    fun sustainedLossReacquiresOtherVisibleLeg() {
        val viewModel =
            newViewModel()

        viewModel.configureTracking(
            targetRepetitions = 10,
            flexedAtOrBelowDegrees = 90.0,
            extendedAtOrAboveDegrees = 160.0,
        )

        viewModel.submitObservation(
            observationWithBothLegs(
                leftConfidence = 0.9f,
                rightConfidence = 0.8f,
            ),
        )

        repeat(5) {
            viewModel.submitObservation(
                rightLegOnlyObservation(),
            )
        }

        // The fifth consecutive missing-left frame releases
        // the left-leg lock and reacquires the visible right leg.
        assertEquals(
            90.0,
            requireNotNull(
                viewModel.analysisState.value
                    .trackedKneeAngleDegrees,
            ),
            0.01,
        )
    }

    @Test
    fun configuringNewSessionClearsPreviousTrackedSide() {
        val viewModel =
            newViewModel()

        viewModel.configureTracking(
            targetRepetitions = 10,
            flexedAtOrBelowDegrees = 90.0,
            extendedAtOrAboveDegrees = 160.0,
        )

        // First session locks onto left.
        viewModel.submitObservation(
            observationWithBothLegs(
                leftConfidence = 0.9f,
                rightConfidence = 0.8f,
            ),
        )

        assertEquals(
            180.0,
            requireNotNull(
                viewModel.analysisState.value
                    .trackedKneeAngleDegrees,
            ),
            0.01,
        )

        // Starting/configuring another exercise must release that lock.
        viewModel.configureTracking(
            targetRepetitions = 8,
            flexedAtOrBelowDegrees = 100.0,
            extendedAtOrAboveDegrees = 160.0,
        )

        viewModel.submitObservation(
            observationWithBothLegs(
                leftConfidence = 0.7f,
                rightConfidence = 0.95f,
            ),
        )

        // Right should now win initial selection.
        assertEquals(
            90.0,
            requireNotNull(
                viewModel.analysisState.value
                    .trackedKneeAngleDegrees,
            ),
            0.01,
        )

        assertEquals(
            8,
            viewModel.analysisState.value
                .targetRepetitions,
        )
    }

    private fun newViewModel(): GuidedSessionViewModel =
        GuidedSessionViewModel(
            calculateKneeAngle =
                CalculateKneeAngle(),
        )

    private fun observationWithBothLegs(
        leftConfidence: Float,
        rightConfidence: Float,
    ): PoseObservation =
        PoseObservation(
            timestampNanos = 1L,
            imageWidth = 100,
            imageHeight = 100,
            landmarks =
                leftStraightLandmarks(
                    confidence = leftConfidence,
                ) +
                    rightBentLandmarks(
                        confidence = rightConfidence,
                    ),
        )

    private fun rightLegOnlyObservation(): PoseObservation =
        PoseObservation(
            timestampNanos = 2L,
            imageWidth = 100,
            imageHeight = 100,
            landmarks =
                rightBentLandmarks(
                    confidence = 0.95f,
                ),
        )

    private fun leftStraightLandmarks(
        confidence: Float,
    ): Map<BodyLandmark, NormalizedPosePoint> =
        mapOf(
            BodyLandmark.LEFT_HIP to
                point(
                    x = 0.25f,
                    y = 0.20f,
                    confidence = confidence,
                ),
            BodyLandmark.LEFT_KNEE to
                point(
                    x = 0.25f,
                    y = 0.50f,
                    confidence = confidence,
                ),
            BodyLandmark.LEFT_ANKLE to
                point(
                    x = 0.25f,
                    y = 0.80f,
                    confidence = confidence,
                ),
        )

    private fun rightBentLandmarks(
        confidence: Float,
    ): Map<BodyLandmark, NormalizedPosePoint> =
        mapOf(
            BodyLandmark.RIGHT_HIP to
                point(
                    x = 0.55f,
                    y = 0.50f,
                    confidence = confidence,
                ),
            BodyLandmark.RIGHT_KNEE to
                point(
                    x = 0.75f,
                    y = 0.50f,
                    confidence = confidence,
                ),
            BodyLandmark.RIGHT_ANKLE to
                point(
                    x = 0.75f,
                    y = 0.80f,
                    confidence = confidence,
                ),
        )

    private fun point(
        x: Float,
        y: Float,
        confidence: Float,
    ): NormalizedPosePoint =
        NormalizedPosePoint(
            x = x,
            y = y,
            confidence = confidence,
        )
}
