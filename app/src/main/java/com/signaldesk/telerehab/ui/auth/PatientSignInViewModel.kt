package com.signaldesk.telerehab.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.SignInPatient
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
    val errorMessage: String? = null,
)

@HiltViewModel
class PatientSignInViewModel @Inject constructor(
    private val signInPatient: SignInPatient,
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

                _uiState.value =
                    _uiState.value.copy(
                        isWorking = false,
                        signedInUserId = userId,
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
