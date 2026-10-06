package com.signaldesk.telerehab.ui.session

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.signaldesk.telerehab.domain.analysis.BodyLandmark
import com.signaldesk.telerehab.domain.analysis.NormalizedPosePoint
import com.signaldesk.telerehab.domain.analysis.PoseObservation

class CameraPoseFrameAnalyzer(
    private val onObservation: (PoseObservation) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector =
        PoseDetection.getClient(
            PoseDetectorOptions
                .Builder()
                .setDetectorMode(
                    PoseDetectorOptions.STREAM_MODE,
                )
                .build(),
        )

    override fun analyze(
        image: ImageProxy,
    ) {
        val mediaImage =
            image.image

        if (mediaImage == null) {
            image.close()
            return
        }

        val rotation =
            image.imageInfo.rotationDegrees

        val inputImage =
            InputImage.fromMediaImage(
                mediaImage,
                rotation,
            )

        detector
            .process(inputImage)
            .addOnSuccessListener { pose ->
                val rotated =
                    rotation == 90 ||
                        rotation == 270

                val imageWidth =
                    if (rotated) {
                        image.height
                    } else {
                        image.width
                    }

                val imageHeight =
                    if (rotated) {
                        image.width
                    } else {
                        image.height
                    }

                onObservation(
                    PoseObservation(
                        timestampNanos =
                            image.imageInfo.timestamp,
                        landmarks =
                            pose.toDomainLandmarks(
                                imageWidth = imageWidth,
                                imageHeight = imageHeight,
                            ),
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                    ),
                )
            }
            .addOnFailureListener { error ->
                onError(error)
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    fun close() {
        detector.close()
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
                    getPoseLandmark(mlKitType)
                        ?: return@mapNotNull null

                val x =
                    landmark.position.x /
                        imageWidth.toFloat()

                val y =
                    landmark.position.y /
                        imageHeight.toFloat()

                if (
                    !x.isFinite() ||
                    !y.isFinite()
                ) {
                    return@mapNotNull null
                }

                domainType to
                    NormalizedPosePoint(
                        x = x.coerceIn(0f, 1f),
                        y = y.coerceIn(0f, 1f),
                        confidence =
                            landmark
                                .inFrameLikelihood
                                .coerceIn(0f, 1f),
                    )
            }
            .toMap()
    }
}
