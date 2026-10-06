package com.signaldesk.telerehab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.signaldesk.telerehab.ui.session.GuidedExerciseSessionScreen
import com.signaldesk.telerehab.ui.session.GuidedSessionViewModel
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DebugCameraVerificationActivity :
    ComponentActivity() {

    private val viewModel:
        GuidedSessionViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            TeleRehabTheme {
                val analysisState by
                    viewModel
                        .analysisState
                        .collectAsState()

                LaunchedEffect(
                    analysisState.framesAnalyzed,
                    analysisState.lastLandmarkCount,
                ) {
                    if (
                        analysisState.framesAnalyzed > 0
                    ) {
                        Log.i(
                            "TeleRehabPose",
                            "TELEREHAB_MLKIT_INFERENCE_GREEN frames=${analysisState.framesAnalyzed}",
                        )
                    }

                    if (
                        analysisState.lastLandmarkCount > 0
                    ) {
                        Log.i(
                            "TeleRehabPose",
                            "TELEREHAB_MLKIT_LANDMARKS_GREEN landmarks=${analysisState.lastLandmarkCount} leftAngle=${analysisState.leftKneeAngleDegrees} rightAngle=${analysisState.rightKneeAngleDegrees}",
                        )
                    }
                }

                GuidedExerciseSessionScreen(
                    exerciseId =
                        "knee-flexion",
                    exerciseTitle =
                        "Knee Flexion and Extension",
                    targetRepetitions = 10,
                    sessionId =
                        "debug-camera-session",
                    analysisState =
                        analysisState,
                    onPoseObservation =
                        viewModel::submitObservation,
                    onPoseError =
                        viewModel::reportCameraAnalysisError,
                )
            }
        }
    }
}
