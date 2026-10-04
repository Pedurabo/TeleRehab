package com.signaldesk.telerehab.ui.session

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.signaldesk.telerehab.domain.analysis.PoseFrame

class CameraPoseFrameAnalyzer(
    private val onFrame: (PoseFrame) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(
        image: ImageProxy,
    ) {
        try {
            val luminancePlane =
                image.planes.firstOrNull()
                    ?: return

            val buffer =
                luminancePlane.buffer

            val pixels =
                ByteArray(
                    buffer.remaining(),
                )

            buffer.get(
                pixels,
            )

            onFrame(
                PoseFrame(
                    width = image.width,
                    height = image.height,
                    rotationDegrees =
                        image.imageInfo.rotationDegrees,
                    timestampNanos =
                        image.imageInfo.timestamp,
                    pixels = pixels,
                ),
            )
        } finally {
            image.close()
        }
    }
}
