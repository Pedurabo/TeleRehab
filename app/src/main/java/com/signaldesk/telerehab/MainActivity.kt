package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.signaldesk.telerehab.ui.patient.PatientHomeScreen
import com.signaldesk.telerehab.ui.patient.PatientHomeViewModel
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel:
        PatientHomeViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TeleRehabTheme {
                val uiState by
                    viewModel.uiState.collectAsState()

                PatientHomeScreen(
                    state = uiState,
                    onRefresh = viewModel::refresh,
                    onAssignmentSelected =
                        viewModel::selectAssignment,
                    onCloseAssignment =
                        viewModel::closeAssignment,
                    onStartSession =
                        viewModel::startSelectedAssignment,
                )
            }
        }
    }
}
