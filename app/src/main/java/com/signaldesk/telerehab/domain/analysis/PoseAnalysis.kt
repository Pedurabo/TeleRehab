package com.signaldesk.telerehab.domain.analysis

enum class BodyLandmark {
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_ANKLE,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_ANKLE,
}

data class NormalizedPosePoint(
    val x: Float,
    val y: Float,
    val confidence: Float,
) {
    init {
        require(x in 0f..1f)
        require(y in 0f..1f)
        require(confidence in 0f..1f)
    }
}

data class PoseObservation(
    val timestampNanos: Long,
    val landmarks: Map<BodyLandmark, NormalizedPosePoint>,
)

data class PoseFrame(
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val timestampNanos: Long,
    val pixels: ByteArray,
) {
    init {
        require(width > 0)
        require(height > 0)

        require(
            rotationDegrees == 0 ||
                rotationDegrees == 90 ||
                rotationDegrees == 180 ||
                rotationDegrees == 270,
        )
    }
}

interface PoseAnalysisEngine {

    suspend fun analyze(
        frame: PoseFrame,
    ): PoseObservation
}
