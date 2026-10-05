package com.signaldesk.telerehab.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.analysis.CalculateKneeAngle
import com.signaldesk.telerehab.domain.analysis.KneeRepetitionConfiguration
import com.signaldesk.telerehab.domain.analysis.KneeRepetitionTracker
import com.signaldesk.telerehab.domain.analysis.KneeSide
import com.signaldesk.telerehab.domain.analysis.PoseAnalysisEngine
import com.signaldesk.telerehab.domain.analysis.PoseFrame
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GuidedSessionAnalysisState(
    val framesSubmitted: Long = 0,
    val framesAnalyzed: Long = 0,
    val lastLandmarkCount: Int = 0,
    val leftKneeAngleDegrees: Double? = null,
    val rightKneeAngleDegrees: Double? = null,
    val repetitions: Int = 0,
    val minimumTrackedKneeAngleDegrees: Double? = null,
    val maximumTrackedKneeAngleDegrees: Double? = null,
    val targetRepetitions: Int = 0,
    val trackingConfigured: Boolean = false,
    val isAnalyzing: Boolean = false,
)

@HiltViewModel
class GuidedSessionViewModel @Inject constructor(
    private val poseAnalysisEngine: PoseAnalysisEngine,
    private val calculateKneeAngle: CalculateKneeAngle,
) : ViewModel() {

    private val _analysisState =
        MutableStateFlow(
            GuidedSessionAnalysisState(),
        )

    val analysisState:
        StateFlow<GuidedSessionAnalysisState> =
        _analysisState

    private var repetitionTracker:
        KneeRepetitionTracker? = null

    private var trackedSide:
        KneeSide = KneeSide.LEFT

    fun configureTracking(
        targetRepetitions: Int,
        flexedAtOrBelowDegrees: Double?,
        extendedAtOrAboveDegrees: Double?,
    ) {
        require(
            targetRepetitions > 0,
        )

        val configured =
            flexedAtOrBelowDegrees != null &&
                extendedAtOrAboveDegrees != null

        repetitionTracker =
            if (configured) {
                KneeRepetitionTracker(
                    configuration =
                        KneeRepetitionConfiguration(
                            flexedAtOrBelowDegrees =
                                requireNotNull(
                                    flexedAtOrBelowDegrees,
                                ),
                            extendedAtOrAboveDegrees =
                                requireNotNull(
                                    extendedAtOrAboveDegrees,
                                ),
                        ),
                )
            } else {
                null
            }

        trackedSide =
            KneeSide.LEFT

        _analysisState.value =
            _analysisState.value.copy(
                repetitions = 0,
                minimumTrackedKneeAngleDegrees = null,
                maximumTrackedKneeAngleDegrees = null,
                targetRepetitions =
                    targetRepetitions,
                trackingConfigured =
                    configured,
            )
    }

    fun submitFrame(
        frame: PoseFrame,
    ) {
        val current =
            _analysisState.value

        _analysisState.value =
            current.copy(
                framesSubmitted =
                    current.framesSubmitted + 1,
            )

        if (current.isAnalyzing) {
            return
        }

        _analysisState.value =
            _analysisState.value.copy(
                isAnalyzing = true,
            )

        viewModelScope.launch {
            try {
                val observation =
                    poseAnalysisEngine.analyze(
                        frame = frame,
                    )

                val left =
                    calculateKneeAngle.invoke(
                        observation = observation,
                        side = KneeSide.LEFT,
                    )

                val right =
                    calculateKneeAngle.invoke(
                        observation = observation,
                        side = KneeSide.RIGHT,
                    )

                val trackedMeasurement =
                    calculateKneeAngle.invoke(
                        observation = observation,
                        side = trackedSide,
                    )

                val repetitionState =
                    trackedMeasurement?.let {
                        repetitionTracker?.accept(
                            measurement = it,
                        )
                    }

                val currentState =
                    _analysisState.value

                val trackedAngleDegrees =
                    trackedMeasurement?.angleDegrees

                val updatedMinimumTrackedAngle =
                    trackedAngleDegrees?.let { angle ->
                        currentState
                            .minimumTrackedKneeAngleDegrees
                            ?.let { currentMinimum ->
                                minOf(
                                    currentMinimum,
                                    angle,
                                )
                            }
                            ?: angle
                    } ?: currentState
                        .minimumTrackedKneeAngleDegrees

                val updatedMaximumTrackedAngle =
                    trackedAngleDegrees?.let { angle ->
                        currentState
                            .maximumTrackedKneeAngleDegrees
                            ?.let { currentMaximum ->
                                maxOf(
                                    currentMaximum,
                                    angle,
                                )
                            }
                            ?: angle
                    } ?: currentState
                        .maximumTrackedKneeAngleDegrees

                _analysisState.value =
                    currentState.copy(
                        framesAnalyzed =
                            currentState.framesAnalyzed + 1,
                        lastLandmarkCount =
                            observation.landmarks.size,
                        leftKneeAngleDegrees =
                            left?.angleDegrees,
                        rightKneeAngleDegrees =
                            right?.angleDegrees,
                        repetitions =
                            repetitionState
                                ?.repetitions
                                ?: currentState.repetitions,
                        minimumTrackedKneeAngleDegrees =
                            updatedMinimumTrackedAngle,
                        maximumTrackedKneeAngleDegrees =
                            updatedMaximumTrackedAngle,
                        isAnalyzing = false,
                    )
            } catch (_: Throwable) {
                _analysisState.value =
                    _analysisState.value.copy(
                        isAnalyzing = false,
                    )
            }
        }
    }

    fun snapshotMetrics(): ExerciseSessionMetrics {
        val state =
            analysisState.value

        return ExerciseSessionMetrics(
            completedRepetitions =
                state.repetitions,
            minimumKneeAngleDegrees =
                state.minimumTrackedKneeAngleDegrees,
            maximumKneeAngleDegrees =
                state.maximumTrackedKneeAngleDegrees,
        )
    }
}
