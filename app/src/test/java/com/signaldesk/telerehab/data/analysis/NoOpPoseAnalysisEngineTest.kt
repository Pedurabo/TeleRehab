package com.signaldesk.telerehab.data.analysis

import com.signaldesk.telerehab.domain.analysis.PoseFrame
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NoOpPoseAnalysisEngineTest {

    @Test
    fun preservesFrameTimestampAndReturnsNoLandmarks() =
        runTest {
            val engine =
                NoOpPoseAnalysisEngine()

            val frame =
                PoseFrame(
                    width = 640,
                    height = 480,
                    rotationDegrees = 90,
                    timestampNanos = 12345L,
                    pixels =
                        byteArrayOf(
                            1,
                            2,
                            3,
                        ),
                )

            val observation =
                engine.analyze(
                    frame = frame,
                )

            assertEquals(
                12345L,
                observation.timestampNanos,
            )

            assertEquals(
                emptyMap<Any, Any>(),
                observation.landmarks,
            )
        }
}
