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
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
) {
    init {
        require(imageWidth > 0)
        require(imageHeight > 0)
    }
}
