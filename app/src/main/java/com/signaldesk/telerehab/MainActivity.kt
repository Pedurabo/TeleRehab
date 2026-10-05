package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.signaldesk.telerehab.ui.AppDestination
import com.signaldesk.telerehab.ui.AppEntryViewModel
import com.signaldesk.telerehab.ui.patient.PatientHomeScreen
import com.signaldesk.telerehab.ui.patient.PatientHomeViewModel
import com.signaldesk.telerehab.ui.session.GuidedSessionViewModel
import com.signaldesk.telerehab.ui.therapist.TherapistHomeScreen
import com.signaldesk.telerehab.ui.therapist.TherapistHomeViewModel
import com.signaldesk.telerehab.ui.theme.TeleRehabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appEntryViewModel:
        AppEntryViewModel by viewModels()

    private val patientHomeViewModel:
        PatientHomeViewModel by viewModels()

    private val therapistHomeViewModel:
        TherapistHomeViewModel by viewModels()

    private val guidedSessionViewModel:
        GuidedSessionViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TeleRehabTheme {
                val appEntryState by
                    appEntryViewModel
                        .uiState
                        .collectAsState()

                when {
                    appEntryState.isLoading -> {
                        CircularProgressIndicator()
                    }

                    appEntryState.errorMessage != null -> {
                        Text(
                            text = appEntryState.errorMessage
                                ?: "Unable to load TeleRehab.",
                        )
                    }

                    appEntryState.destination == AppDestination.THERAPIST -> {
                        val therapistState by
                            therapistHomeViewModel
                                .uiState
                                .collectAsState()

                        TherapistHomeScreen(
                            state = therapistState,
                            onPatientSelected =
                                therapistHomeViewModel::selectPatient,
                        )
                    }

                    else -> {
                        val patientState by
                            patientHomeViewModel
                                .uiState
                                .collectAsState()

                        val guidedAnalysisState by
                            guidedSessionViewModel
                                .analysisState
                                .collectAsState()

                        PatientHomeScreen(
                            state = patientState,
                            onRefresh =
                                patientHomeViewModel::refresh,
                            onAssignmentSelected =
                                patientHomeViewModel::selectAssignment,
                            onCloseAssignment =
                                patientHomeViewModel::closeAssignment,
                            onStartSession =
                                patientHomeViewModel::startSelectedAssignment,
                            onFinishSession = {
                                patientHomeViewModel.finishActiveSession(
                                    guidedSessionViewModel.snapshotMetrics(),
                                )
                            },
                            guidedAnalysisState =
                                guidedAnalysisState,
                            onPoseFrame =
                                guidedSessionViewModel::submitFrame,
                            onConfigureTracking =
                                guidedSessionViewModel::configureTracking,
                        )
                    }
                }
            }
        }
    }
}
