package com.signaldesk.telerehab.data.analysis

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.signaldesk.telerehab.domain.analysis.BodyLandmark
import com.signaldesk.telerehab.domain.analysis.NormalizedPosePoint
import com.signaldesk.telerehab.domain.analysis.PoseAnalysisEngine
import com.signaldesk.telerehab.domain.analysis.PoseFrame
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class MlKitPoseAnalysisEngine @Inject constructor() :
    PoseAnalysisEngine {

    private val detector =
        PoseDetection.getClient(
            PoseDetectorOptions
                .Builder()
                .setDetectorMode(
                    PoseDetectorOptions.STREAM_MODE,
                )
                .build(),
        )

    override suspend fun analyze(
        frame: PoseFrame,
    ): PoseObservation {
        val inputImage =
            InputImage.fromByteArray(
                frame.pixels,
                frame.width,
                frame.height,
                frame.rotationDegrees,
                InputImage.IMAGE_FORMAT_NV21,
            )

        val pose =
            detector
                .process(inputImage)
                .await()

        val rotated =
            frame.rotationDegrees == 90 ||
                frame.rotationDegrees == 270

        val imageWidth =
            if (rotated) {
                frame.height
            } else {
                frame.width
            }

        val imageHeight =
            if (rotated) {
                frame.width
            } else {
                frame.height
            }

        return PoseObservation(
            timestampNanos =
                frame.timestampNanos,
            landmarks =
                pose.toDomainLandmarks(
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                ),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
        )
    }

    private fun Pose.toDomainLandmarks(
        imageWidth: Int,
        imageHeight: Int,
    ): Map<BodyLandmark, NormalizedPosePoint> {
        val mapping =
            listOf(
                BodyLandmark.LEFT_HIP to
                    PoseLandmark.LEFT_HIP,
                BodyLandmark.LEFT_KNEE to
                    PoseLandmark.LEFT_KNEE,
                BodyLandmark.LEFT_ANKLE to
                    PoseLandmark.LEFT_ANKLE,
                BodyLandmark.RIGHT_HIP to
                    PoseLandmark.RIGHT_HIP,
                BodyLandmark.RIGHT_KNEE to
                    PoseLandmark.RIGHT_KNEE,
                BodyLandmark.RIGHT_ANKLE to
                    PoseLandmark.RIGHT_ANKLE,
            )

        return mapping
            .mapNotNull { (domainType, mlKitType) ->
                val landmark =
                    getPoseLandmark(
                        mlKitType,
                    )
                        ?: return@mapNotNull null

                val normalizedX =
                    (
                        landmark.position.x /
                            imageWidth.toFloat()
                    ).coerceIn(
                        0f,
                        1f,
                    )

                val normalizedY =
                    (
                        landmark.position.y /
                            imageHeight.toFloat()
                    ).coerceIn(
                        0f,
                        1f,
                    )

                domainType to
                    NormalizedPosePoint(
                        x = normalizedX,
                        y = normalizedY,
                        confidence =
                            landmark
                                .inFrameLikelihood
                                .coerceIn(
                                    0f,
                                    1f,
                                ),
                    )
            }
            .toMap()
    }
}
