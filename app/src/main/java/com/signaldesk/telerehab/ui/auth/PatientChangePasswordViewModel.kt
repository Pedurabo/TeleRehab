package com.signaldesk.telerehab.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.ChangePatientPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PatientChangePasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isWorking: Boolean = false,
    val completed: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PatientChangePasswordViewModel @Inject constructor(
    private val changePatientPassword: ChangePatientPassword,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            PatientChangePasswordUiState(),
        )

    val uiState: StateFlow<PatientChangePasswordUiState> =
        _uiState

    fun updateNewPassword(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                newPassword = value,
                errorMessage = null,
            )
    }

    fun updateConfirmPassword(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                confirmPassword = value,
                errorMessage = null,
            )
    }

    fun changePassword() {
        val state =
            _uiState.value

        if (state.isWorking) {
            return
        }

        if (state.newPassword.length < 8) {
            _uiState.value =
                state.copy(
                    errorMessage =
                        "Password must contain at least 8 characters.",
                )
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.value =
                state.copy(
                    errorMessage =
                        "Passwords do not match.",
                )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    state.copy(
                        isWorking = true,
                        errorMessage = null,
                    )

                changePatientPassword(
                    newPassword = state.newPassword,
                )

                _uiState.value =
                    PatientChangePasswordUiState(
                        completed = true,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        errorMessage =
                            error.message
                                ?: "Unable to change password.",
                    )
            }
        }
    }

    fun reset() {
        _uiState.value =
            PatientChangePasswordUiState()
    }
}
