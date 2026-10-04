package com.signaldesk.telerehab.domain.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateKneeAngleTest {

    private val calculator =
        CalculateKneeAngle()

    @Test
    fun calculatesStraightLegAs180Degrees() {
        val observation =
            PoseObservation(
                timestampNanos = 1L,
                landmarks =
                    mapOf(
                        BodyLandmark.LEFT_HIP to
                            point(
                                x = 0.5f,
                                y = 0.2f,
                            ),
                        BodyLandmark.LEFT_KNEE to
                            point(
                                x = 0.5f,
                                y = 0.5f,
                            ),
                        BodyLandmark.LEFT_ANKLE to
                            point(
                                x = 0.5f,
                                y = 0.8f,
                            ),
                    ),
            )

        val measurement =
            calculator.invoke(
                observation = observation,
                side = KneeSide.LEFT,
            )

        requireNotNull(measurement)

        assertEquals(
            180.0,
            measurement.angleDegrees,
            0.001,
        )
    }

    @Test
    fun calculatesRightAngleAs90Degrees() {
        val observation =
            PoseObservation(
                timestampNanos = 2L,
                landmarks =
                    mapOf(
                        BodyLandmark.RIGHT_HIP to
                            point(
                                x = 0.5f,
                                y = 0.2f,
                            ),
                        BodyLandmark.RIGHT_KNEE to
                            point(
                                x = 0.5f,
                                y = 0.5f,
                            ),
                        BodyLandmark.RIGHT_ANKLE to
                            point(
                                x = 0.8f,
                                y = 0.5f,
                            ),
                    ),
            )

        val measurement =
            calculator.invoke(
                observation = observation,
                side = KneeSide.RIGHT,
            )

        requireNotNull(measurement)

        assertEquals(
            90.0,
            measurement.angleDegrees,
            0.001,
        )
    }

    @Test
    fun rejectsLowConfidenceLandmarks() {
        val observation =
            PoseObservation(
                timestampNanos = 3L,
                landmarks =
                    mapOf(
                        BodyLandmark.LEFT_HIP to
                            point(
                                x = 0.5f,
                                y = 0.2f,
                            ),
                        BodyLandmark.LEFT_KNEE to
                            point(
                                x = 0.5f,
                                y = 0.5f,
                                confidence = 0.2f,
                            ),
                        BodyLandmark.LEFT_ANKLE to
                            point(
                                x = 0.5f,
                                y = 0.8f,
                            ),
                    ),
            )

        assertNull(
            calculator.invoke(
                observation = observation,
                side = KneeSide.LEFT,
                minimumConfidence = 0.5f,
            ),
        )
    }

    @Test
    fun returnsNullWhenRequiredLandmarkIsMissing() {
        val observation =
            PoseObservation(
                timestampNanos = 4L,
                landmarks =
                    mapOf(
                        BodyLandmark.LEFT_HIP to
                            point(
                                x = 0.5f,
                                y = 0.2f,
                            ),
                        BodyLandmark.LEFT_KNEE to
                            point(
                                x = 0.5f,
                                y = 0.5f,
                            ),
                    ),
            )

        assertNull(
            calculator.invoke(
                observation = observation,
                side = KneeSide.LEFT,
            ),
        )
    }

    private fun point(
        x: Float,
        y: Float,
        confidence: Float = 1f,
    ) =
        NormalizedPosePoint(
            x = x,
            y = y,
            confidence = confidence,
        )
}
