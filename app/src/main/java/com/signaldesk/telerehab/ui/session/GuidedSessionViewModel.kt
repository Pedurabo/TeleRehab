package com.signaldesk.telerehab.ui.session

import androidx.lifecycle.ViewModel
import com.signaldesk.telerehab.domain.analysis.CalculateKneeAngle
import com.signaldesk.telerehab.domain.analysis.KneeMovementPhase
import com.signaldesk.telerehab.domain.analysis.KneeRepetitionConfiguration
import com.signaldesk.telerehab.domain.analysis.KneeRepetitionTracker
import com.signaldesk.telerehab.domain.analysis.KneeSide
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    val flexedAtOrBelowDegrees: Double? = null,
    val extendedAtOrAboveDegrees: Double? = null,
    val trackedKneeAngleDegrees: Double? = null,
    val movementPhase: KneeMovementPhase = KneeMovementPhase.UNKNOWN,
    val trackingConfigured: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analysisErrorMessage: String? = null,
)

@HiltViewModel
class GuidedSessionViewModel @Inject constructor(
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
        KneeSide? = null

    private var missingTrackedSideFrames:
        Int = 0

    private val sideReacquisitionFrameThreshold:
        Int = 5

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
            null

        missingTrackedSideFrames =
            0

        _analysisState.value =
            _analysisState.value.copy(
                repetitions = 0,
                minimumTrackedKneeAngleDegrees = null,
                maximumTrackedKneeAngleDegrees = null,
                targetRepetitions =
                    targetRepetitions,
                flexedAtOrBelowDegrees =
                    flexedAtOrBelowDegrees,
                extendedAtOrAboveDegrees =
                    extendedAtOrAboveDegrees,
                trackedKneeAngleDegrees = null,
                movementPhase = KneeMovementPhase.UNKNOWN,
                trackingConfigured =
                    configured,
            )
    }

    fun submitObservation(
        observation: PoseObservation,
    ) {
        val left =
            calculateKneeAngle.invoke(
                observation = observation,
                side = KneeSide.LEFT,
                minimumConfidence = 0.0f,
            )

        val right =
            calculateKneeAngle.invoke(
                observation = observation,
                side = KneeSide.RIGHT,
                minimumConfidence = 0.0f,
            )

        val selectedMeasurement =
            when (trackedSide) {
                KneeSide.LEFT -> {
                    if (left != null) {
                        missingTrackedSideFrames = 0
                        left
                    } else {
                        missingTrackedSideFrames += 1

                        if (
                            missingTrackedSideFrames >=
                            sideReacquisitionFrameThreshold
                        ) {
                            trackedSide =
                                right?.side

                            missingTrackedSideFrames = 0

                            right
                        } else {
                            null
                        }
                    }
                }

                KneeSide.RIGHT -> {
                    if (right != null) {
                        missingTrackedSideFrames = 0
                        right
                    } else {
                        missingTrackedSideFrames += 1

                        if (
                            missingTrackedSideFrames >=
                            sideReacquisitionFrameThreshold
                        ) {
                            trackedSide =
                                left?.side

                            missingTrackedSideFrames = 0

                            left
                        } else {
                            null
                        }
                    }
                }

                null -> {
                    val selected =
                        when {
                            left == null ->
                                right

                            right == null ->
                                left

                            left.confidence >=
                                right.confidence ->
                                left

                            else ->
                                right
                        }

                    trackedSide =
                        selected?.side

                    missingTrackedSideFrames = 0

                    selected
                }
            }

        val repetitionState =
            selectedMeasurement?.let {
                repetitionTracker?.accept(
                    measurement = it,
                )
            }

        val current =
            _analysisState.value

        val angle =
            selectedMeasurement?.angleDegrees

        val minimum =
            angle?.let {
                current
                    .minimumTrackedKneeAngleDegrees
                    ?.let { old ->
                        minOf(old, it)
                    }
                    ?: it
            } ?: current.minimumTrackedKneeAngleDegrees

        val maximum =
            angle?.let {
                current
                    .maximumTrackedKneeAngleDegrees
                    ?.let { old ->
                        maxOf(old, it)
                    }
                    ?: it
            } ?: current.maximumTrackedKneeAngleDegrees

        _analysisState.value =
            current.copy(
                framesSubmitted =
                    current.framesSubmitted + 1,
                framesAnalyzed =
                    current.framesAnalyzed + 1,
                lastLandmarkCount =
                    observation.landmarks.size,
                leftKneeAngleDegrees =
                    left?.angleDegrees,
                rightKneeAngleDegrees =
                    right?.angleDegrees,
                trackedKneeAngleDegrees =
                    angle,
                repetitions =
                    repetitionState
                        ?.repetitions
                        ?: current.repetitions,
                movementPhase =
                    repetitionState
                        ?.phase
                        ?: current.movementPhase,
                minimumTrackedKneeAngleDegrees =
                    minimum,
                maximumTrackedKneeAngleDegrees =
                    maximum,
                analysisErrorMessage = null,
            )
    }

    fun reportCameraAnalysisError(
        error: Throwable,
    ) {
        _analysisState.value =
            _analysisState.value.copy(
                analysisErrorMessage =
                    error.message
                        ?: "Camera pose analysis failed.",
            )
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
