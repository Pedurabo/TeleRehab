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
            onFrame(
                PoseFrame(
                    width = image.width,
                    height = image.height,
                    rotationDegrees =
                        image.imageInfo.rotationDegrees,
                    timestampNanos =
                        image.imageInfo.timestamp,
                    pixels =
                        image.toNv21(),
                ),
            )
        } finally {
            image.close()
        }
    }

    private fun ImageProxy.toNv21():
        ByteArray {
        require(planes.size >= 3) {
            "YUV camera frame must contain three planes."
        }

        val result =
            ByteArray(
                width * height +
                    width * height / 2,
            )

        var outputIndex = 0

        val yPlane =
            planes[0]

        val yBuffer =
            yPlane.buffer.duplicate()

        for (row in 0 until height) {
            val rowStart =
                row * yPlane.rowStride

            for (column in 0 until width) {
                result[outputIndex++] =
                    yBuffer.get(
                        rowStart +
                            column *
                            yPlane.pixelStride,
                    )
            }
        }

        val uPlane =
            planes[1]

        val vPlane =
            planes[2]

        val uBuffer =
            uPlane.buffer.duplicate()

        val vBuffer =
            vPlane.buffer.duplicate()

        val chromaHeight =
            height / 2

        val chromaWidth =
            width / 2

        for (row in 0 until chromaHeight) {
            val uRowStart =
                row * uPlane.rowStride

            val vRowStart =
                row * vPlane.rowStride

            for (column in 0 until chromaWidth) {
                result[outputIndex++] =
                    vBuffer.get(
                        vRowStart +
                            column *
                            vPlane.pixelStride,
                    )

                result[outputIndex++] =
                    uBuffer.get(
                        uRowStart +
                            column *
                            uPlane.pixelStride,
                    )
            }
        }

        return result
    }
}
