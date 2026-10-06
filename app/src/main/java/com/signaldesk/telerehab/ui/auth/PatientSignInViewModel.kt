package com.signaldesk.telerehab.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.SendPatientPasswordReset
import com.signaldesk.telerehab.domain.auth.SignInPatient
import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PatientSignInUiState(
    val email: String = "",
    val password: String = "",
    val isWorking: Boolean = false,
    val signedInUserId: String? = null,
    val mustChangePasswordRequired: Boolean = false,
    val resetMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class PatientSignInViewModel @Inject constructor(
    private val signInPatient: SignInPatient,
    private val sendPatientPasswordReset:
        SendPatientPasswordReset,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            PatientSignInUiState(),
        )

    val uiState: StateFlow<PatientSignInUiState> =
        _uiState

    fun reset() {
        _uiState.value =
            PatientSignInUiState()
    }

    fun updateEmail(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                email = value,
                resetMessage = null,
                errorMessage = null,
            )
    }

    fun updatePassword(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                password = value,
                errorMessage = null,
            )
    }

    fun sendPasswordReset() {
        val state =
            _uiState.value

        if (state.isWorking) {
            return
        }

        if (state.email.isBlank()) {
            _uiState.value =
                state.copy(
                    errorMessage =
                        "Enter your email address first.",
                    resetMessage = null,
                )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    state.copy(
                        isWorking = true,
                        errorMessage = null,
                        resetMessage = null,
                    )

                sendPatientPasswordReset(
                    email = state.email,
                )

                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        resetMessage =
                            "Password reset email sent.",
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        resetMessage = null,
                        errorMessage =
                            error.message
                                ?: "Unable to send password reset email.",
                    )
            }
        }
    }

    fun signIn() {
        runAuthAction { state ->
            signInPatient(
                email = state.email,
                password = state.password,
            )
        }
    }

    private fun runAuthAction(
        action: suspend (PatientSignInUiState) -> String,
    ) {
        val state =
            _uiState.value

        if (state.isWorking) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    state.copy(
                        isWorking = true,
                        errorMessage = null,
                    )

                val userId =
                    action(state)

                val profile =
                    userProfileRepository.findById(
                        userId = userId,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        signedInUserId = userId,
                        mustChangePasswordRequired =
                            profile?.mustChangePassword == true,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        errorMessage =
                            error.message
                                ?: "Unable to continue as patient.",
                    )
            }
        }
    }
}
