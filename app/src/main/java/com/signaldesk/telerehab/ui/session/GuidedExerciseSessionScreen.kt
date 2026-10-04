package com.signaldesk.telerehab.ui.session

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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

@Composable
fun GuidedExerciseSessionScreen(
    exerciseTitle: String,
    targetRepetitions: Int,
    sessionId: String,
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
            }
        }

    DisposableEffect(
        cameraController,
        lifecycleOwner,
    ) {
        cameraController.bindToLifecycle(
            lifecycleOwner,
        )

        onDispose {
            cameraController.unbind()
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
            Text(
                text = "Position yourself so your hip, knee, and ankle are visible.",
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Pose tracking foundation ready",
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
