package com.signaldesk.telerehab.domain.analysis

import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.sqrt

enum class KneeSide {
    LEFT,
    RIGHT,
}

data class KneeAngleMeasurement(
    val side: KneeSide,
    val angleDegrees: Double,
    val confidence: Float,
) {
    init {
        require(angleDegrees in 0.0..180.0)
        require(confidence in 0f..1f)
    }
}

class CalculateKneeAngle @Inject constructor() {

    fun invoke(
        observation: PoseObservation,
        side: KneeSide,
        minimumConfidence: Float = 0.5f,
    ): KneeAngleMeasurement? {
        require(minimumConfidence in 0f..1f)

        val landmarks =
            when (side) {
                KneeSide.LEFT ->
                    Triple(
                        BodyLandmark.LEFT_HIP,
                        BodyLandmark.LEFT_KNEE,
                        BodyLandmark.LEFT_ANKLE,
                    )

                KneeSide.RIGHT ->
                    Triple(
                        BodyLandmark.RIGHT_HIP,
                        BodyLandmark.RIGHT_KNEE,
                        BodyLandmark.RIGHT_ANKLE,
                    )
            }

        val hip =
            observation.landmarks[landmarks.first]
                ?: return null

        val knee =
            observation.landmarks[landmarks.second]
                ?: return null

        val ankle =
            observation.landmarks[landmarks.third]
                ?: return null

        val confidence =
            minOf(
                hip.confidence,
                knee.confidence,
                ankle.confidence,
            )

        if (confidence < minimumConfidence) {
            return null
        }

        val width =
            observation.imageWidth.toDouble()

        val height =
            observation.imageHeight.toDouble()

        val hipX =
            hip.x * width

        val hipY =
            hip.y * height

        val kneeX =
            knee.x * width

        val kneeY =
            knee.y * height

        val ankleX =
            ankle.x * width

        val ankleY =
            ankle.y * height

        val upperX =
            hipX - kneeX

        val upperY =
            hipY - kneeY

        val lowerX =
            ankleX - kneeX

        val lowerY =
            ankleY - kneeY

        val upperMagnitude =
            sqrt(
                upperX * upperX +
                    upperY * upperY,
            )

        val lowerMagnitude =
            sqrt(
                lowerX * lowerX +
                    lowerY * lowerY,
            )

        if (
            upperMagnitude == 0.0 ||
            lowerMagnitude == 0.0
        ) {
            return null
        }

        val cosine =
            (
                (
                    upperX * lowerX +
                        upperY * lowerY
                ) /
                    (
                        upperMagnitude *
                            lowerMagnitude
                    )
            ).coerceIn(
                -1.0,
                1.0,
            )

        return KneeAngleMeasurement(
            side = side,
            angleDegrees =
                Math.toDegrees(
                    acos(cosine),
                ),
            confidence = confidence,
        )
    }
}
