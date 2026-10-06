package com.signaldesk.telerehab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.signaldesk.telerehab.ui.AppDestination
import com.signaldesk.telerehab.ui.AppEntryScreen
import com.signaldesk.telerehab.ui.AppEntryViewModel
import com.signaldesk.telerehab.ui.auth.PatientChangePasswordScreen
import com.signaldesk.telerehab.ui.auth.PatientChangePasswordViewModel
import com.signaldesk.telerehab.ui.auth.PatientSignInScreen
import com.signaldesk.telerehab.ui.auth.PatientSignInViewModel
import com.signaldesk.telerehab.ui.auth.TherapistSignInScreen
import com.signaldesk.telerehab.ui.auth.TherapistSignInViewModel
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

    private val patientSignInViewModel:
        PatientSignInViewModel by viewModels()

    private val patientChangePasswordViewModel:
        PatientChangePasswordViewModel by viewModels()

    private val therapistSignInViewModel:
        TherapistSignInViewModel by viewModels()

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
                            text =
                                appEntryState.errorMessage
                                    ?: "Unable to load TeleRehab.",
                        )
                    }

                    appEntryState.destination == AppDestination.ENTRY -> {
                        AppEntryScreen(
                            onContinueAsPatient = {
                                patientSignInViewModel.reset()
                                appEntryViewModel.continueAsPatient()
                            },
                            onTherapistSignIn = {
                                therapistSignInViewModel.reset()
                                appEntryViewModel.openTherapistSignIn()
                            },
                        )
                    }

                    appEntryState.destination ==
                        AppDestination.PATIENT_SIGN_IN -> {
                        val patientSignInState by
                            patientSignInViewModel
                                .uiState
                                .collectAsState()

                        PatientSignInScreen(
                            state = patientSignInState,
                            onEmailChanged =
                                patientSignInViewModel::updateEmail,
                            onPasswordChanged =
                                patientSignInViewModel::updatePassword,
                            onSignIn =
                                patientSignInViewModel::signIn,
                            onBack =
                                appEntryViewModel::openEntry,
                        )

                        if (
                            patientSignInState.signedInUserId != null
                        ) {
                            appEntryViewModel.patientSignedIn(
                                mustChangePassword =
                                    patientSignInState
                                        .mustChangePasswordRequired,
                            )
                        }
                    }

                    appEntryState.destination ==
                        AppDestination.PATIENT_CHANGE_PASSWORD -> {
                        val passwordState by
                            patientChangePasswordViewModel
                                .uiState
                                .collectAsState()

                        PatientChangePasswordScreen(
                            state = passwordState,
                            onNewPasswordChanged =
                                patientChangePasswordViewModel::updateNewPassword,
                            onConfirmPasswordChanged =
                                patientChangePasswordViewModel::updateConfirmPassword,
                            onChangePassword =
                                patientChangePasswordViewModel::changePassword,
                            onSignOut = {
                                patientChangePasswordViewModel.reset()
                                patientSignInViewModel.reset()
                                appEntryViewModel.signOut()
                            },
                        )

                        if (passwordState.completed) {
                            appEntryViewModel.patientPasswordChanged()
                        }
                    }

                    appEntryState.destination ==
                        AppDestination.THERAPIST_SIGN_IN -> {
                        val signInState by
                            therapistSignInViewModel
                                .uiState
                                .collectAsState()

                        TherapistSignInScreen(
                            state = signInState,
                            onEmailChanged =
                                therapistSignInViewModel::updateEmail,
                            onPasswordChanged =
                                therapistSignInViewModel::updatePassword,
                            onSignIn = {
                                therapistSignInViewModel.signIn()
                            },
                            onBack =
                                appEntryViewModel::openEntry,
                        )

                        if (
                            signInState.signedInUserId != null
                        ) {
                            appEntryViewModel.therapistSignedIn()
                        }
                    }

                    appEntryState.destination ==
                        AppDestination.THERAPIST -> {
                        LaunchedEffect(Unit) {
                            therapistHomeViewModel.clearSelectedPatient()
                        }

                        val therapistState by
                            therapistHomeViewModel
                                .uiState
                                .collectAsState()

                        TherapistHomeScreen(
                            state = therapistState,
                            onPatientSelected =
                                therapistHomeViewModel::selectPatient,
                            onSaveAssignment =
                                therapistHomeViewModel::saveAssignment,
                            onDismissSaveMessage =
                                therapistHomeViewModel::clearSaveMessage,
                            onNewPatientEmailChanged =
                                therapistHomeViewModel::updateNewPatientEmail,
                            onNewPatientDisplayNameChanged =
                                therapistHomeViewModel::updateNewPatientDisplayName,
                            onAddPatient =
                                therapistHomeViewModel::addPatient,
                            onCreateKneeFlexionAssignment =
                                therapistHomeViewModel::createKneeFlexionAssignment,
                            onSignOut = {
                                therapistSignInViewModel.reset()
                                appEntryViewModel.signOut()
                            },
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                            patientHomeViewModel.load()
                        }

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
                            onSignOut = {
                                patientHomeViewModel.reset()
                                patientSignInViewModel.reset()
                                appEntryViewModel.signOut()
                            },
                        )
                    }
                }
            }
        }
    }
}
