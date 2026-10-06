package com.signaldesk.telerehab.ui.session

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.signaldesk.telerehab.domain.analysis.KneeMovementPhase
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import java.util.concurrent.Executors

@Composable
fun GuidedExerciseSessionScreen(
    exerciseTitle: String,
    targetRepetitions: Int,
    sessionId: String,
    analysisState: GuidedSessionAnalysisState,
    onPoseObservation: (PoseObservation) -> Unit,
    onPoseError: (Throwable) -> Unit,
    onFinishSession: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasCameraPermission =
                granted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA,
            )
        }
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .semantics {
                    contentDescription =
                        "TeleRehab guided exercise session"
                },
    ) {
        if (hasCameraPermission) {
            CameraSessionContent(
                exerciseTitle = exerciseTitle,
                targetRepetitions = targetRepetitions,
                sessionId = sessionId,
                lifecycleOwner = lifecycleOwner,
                analysisState = analysisState,
                onPoseObservation = onPoseObservation,
                onPoseError = onPoseError,
                onFinishSession = onFinishSession,
            )
        } else {
            CameraPermissionContent(
                onRequestPermission = {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA,
                    )
                },
            )
        }
    }
}

@Composable
private fun CameraSessionContent(
    exerciseTitle: String,
    targetRepetitions: Int,
    sessionId: String,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    analysisState: GuidedSessionAnalysisState,
    onPoseObservation: (PoseObservation) -> Unit,
    onPoseError: (Throwable) -> Unit,
    onFinishSession: () -> Unit,
) {
    val context =
        LocalContext.current

    val cameraController =
        remember {
            LifecycleCameraController(
                context,
            ).apply {
                cameraSelector =
                    CameraSelector.DEFAULT_FRONT_CAMERA

                setEnabledUseCases(
                    CameraController.IMAGE_ANALYSIS,
                )
            }
        }

    val analyzer =
        remember(
            onPoseObservation,
            onPoseError,
        ) {
            CameraPoseFrameAnalyzer(
                onObservation =
                    onPoseObservation,
                onError =
                    onPoseError,
            )
        }

    val analysisExecutor =
        remember {
            Executors.newSingleThreadExecutor()
        }

    DisposableEffect(
        cameraController,
        lifecycleOwner,
        analyzer,
        analysisExecutor,
    ) {
        cameraController.bindToLifecycle(
            lifecycleOwner,
        )

        cameraController.setImageAnalysisAnalyzer(
            analysisExecutor,
            analyzer,
        )

        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
            cameraController.unbind()
            analyzer.close()
            analysisExecutor.shutdown()
        }
    }

    Box(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        AndroidView(
            modifier =
                Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription =
                            "TeleRehab camera preview"
                    },
            factory = { previewContext ->
                PreviewView(
                    previewContext,
                ).apply {
                    scaleType =
                        PreviewView.ScaleType.FILL_CENTER

                    controller =
                        cameraController

                    contentDescription =
                        "TeleRehab camera preview"
                }
            },
        )

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter,
                    )
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(
                            alpha = 0.55f,
                        ),
                    )
                    .padding(20.dp),
        ) {
            Text(
                text = exerciseTitle,
                color = Color.White,
                style =
                    MaterialTheme.typography.titleLarge,
            )

            Text(
                text =
                    "Target: $targetRepetitions repetitions",
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyLarge,
            )


        }

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter,
                    )
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(
                            alpha = 0.55f,
                        ),
                    )
                    .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp,
                ),
        ) {
            Button(
                onClick = onFinishSession,
            ) {
                Text("Finish session")
            }

            Text(
                text =
                    "${analysisState.repetitions} / $targetRepetitions repetitions",
                color = Color.White,
                style =
                    MaterialTheme.typography.titleMedium,
            )

            Text(
                text =
                    sessionGuidance(
                        state = analysisState,
                    ),
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyLarge,
            )

            analysisState.trackedKneeAngleDegrees?.let { angle ->
                Text(
                    text =
                        "Tracked knee angle: ${angle.toInt()}?",
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                text =
                    "Tracking: ${
                        if (
                            analysisState.trackedKneeAngleDegrees != null
                        ) {
                            "leg detected"
                        } else {
                            "waiting for hip, knee, and ankle"
                        }
                    }",
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "Session ${sessionId.take(8)}",
                color = Color.White,
                style =
                    MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun sessionGuidance(
    state: GuidedSessionAnalysisState,
): String {
    val angle =
        state.trackedKneeAngleDegrees

    if (angle == null) {
        return "Move so your hip, knee, and ankle are clearly visible."
    }

    if (!state.trackingConfigured) {
        return "Keep the leg visible and move slowly through the exercise."
    }

    val flexed =
        state.flexedAtOrBelowDegrees

    val extended =
        state.extendedAtOrAboveDegrees

    return when (state.movementPhase) {
        KneeMovementPhase.UNKNOWN -> {
            if (
                extended != null &&
                angle < extended
            ) {
                "Straighten your knee to begin the repetition."
            } else {
                "Ready. Bend your knee slowly."
            }
        }

        KneeMovementPhase.EXTENDED -> {
            if (
                flexed != null &&
                angle > flexed
            ) {
                "Bend your knee a little more."
            } else {
                "Good bend. Now straighten your knee."
            }
        }

        KneeMovementPhase.FLEXED -> {
            if (
                extended != null &&
                angle < extended
            ) {
                "Straighten your knee to complete the repetition."
            } else {
                "Repetition complete. Bend again when ready."
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text = "Camera access is needed for the guided exercise view.",
            style =
                MaterialTheme.typography.bodyLarge,
        )

        Row(
            modifier =
                Modifier.padding(
                    top = 16.dp,
                ),
        ) {
            Button(
                onClick =
                    onRequestPermission,
            ) {
                Text(
                    text = "Allow camera",
                )
            }
        }
    }
}
