package com.signaldesk.telerehab.data.analysis

import com.signaldesk.telerehab.domain.analysis.PoseAnalysisEngine
import com.signaldesk.telerehab.domain.analysis.PoseFrame
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpPoseAnalysisEngine @Inject constructor() :
    PoseAnalysisEngine {

    override suspend fun analyze(
        frame: PoseFrame,
    ): PoseObservation =
        PoseObservation(
            timestampNanos = frame.timestampNanos,
            landmarks = emptyMap(),
        )
}
