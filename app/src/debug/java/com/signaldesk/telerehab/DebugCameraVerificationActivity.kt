package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.signaldesk.telerehab.ui.session.GuidedExerciseSessionScreen
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme

class DebugCameraVerificationActivity :
    ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(
            savedInstanceState,
        )

        setContent {
            TeleRehabTheme {
                GuidedExerciseSessionScreen(
                    exerciseTitle =
                        "Knee Flexion and Extension",
                    targetRepetitions = 10,
                    sessionId =
                        "debug-camera-session",
                )
            }
        }
    }
}
