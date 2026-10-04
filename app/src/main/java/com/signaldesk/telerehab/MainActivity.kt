package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.signaldesk.telerehab.ui.patient.PatientHomeScreen
import com.signaldesk.telerehab.ui.patient.PatientHomeViewModel
import com.signaldesk.telerehab.ui.session.GuidedSessionViewModel
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel:
        PatientHomeViewModel by viewModels()

    private val guidedSessionViewModel:
        GuidedSessionViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TeleRehabTheme {
                val uiState by
                    viewModel
                        .uiState
                        .collectAsState()

                val guidedAnalysisState by
                    guidedSessionViewModel
                        .analysisState
                        .collectAsState()

                PatientHomeScreen(
                    state = uiState,
                    onRefresh = viewModel::refresh,
                    onAssignmentSelected =
                        viewModel::selectAssignment,
                    onCloseAssignment =
                        viewModel::closeAssignment,
                    onStartSession =
                        viewModel::startSelectedAssignment,
                    guidedAnalysisState =
                        guidedAnalysisState,
                    onPoseFrame =
                        guidedSessionViewModel::submitFrame,
                )
            }
        }
    }
}
