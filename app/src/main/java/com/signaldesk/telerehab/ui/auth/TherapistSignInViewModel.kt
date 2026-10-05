package com.signaldesk.telerehab.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.SignInTherapist
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TherapistSignInUiState(
    val email: String = "",
    val password: String = "",
    val isSigningIn: Boolean = false,
    val signedInUserId: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class TherapistSignInViewModel @Inject constructor(
    private val signInTherapist: SignInTherapist,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            TherapistSignInUiState(),
        )

    val uiState: StateFlow<TherapistSignInUiState> =
        _uiState

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
        val state =
            _uiState.value

        if (state.isSigningIn) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    state.copy(
                        isSigningIn = true,
                        errorMessage = null,
                    )

                val userId =
                    signInTherapist(
                        email = state.email,
                        password = state.password,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        isSigningIn = false,
                        signedInUserId = userId,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isSigningIn = false,
                        errorMessage =
                            error.message
                                ?: "Unable to sign in as therapist.",
                    )
            }
        }
    }
}
