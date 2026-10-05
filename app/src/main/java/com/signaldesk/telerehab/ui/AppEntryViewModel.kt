package com.signaldesk.telerehab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.AuthSession
import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import com.signaldesk.telerehab.domain.identity.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppDestination {
    ENTRY,
    PATIENT_SIGN_IN,
    PATIENT,
    THERAPIST_SIGN_IN,
    THERAPIST,
}

data class AppEntryUiState(
    val isLoading: Boolean = true,
    val destination: AppDestination? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    private val authSession: AuthSession,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AppEntryUiState(),
        )

    val uiState: StateFlow<AppEntryUiState> =
        _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val currentUserId =
                    authSession.currentUserId()

                val profile =
                    currentUserId?.let {
                        userProfileRepository.findById(it)
                    }

                val destination =
                    if (profile?.role == UserRole.THERAPIST) {
                        AppDestination.THERAPIST
                    } else {
                        AppDestination.ENTRY
                    }

                _uiState.value =
                    AppEntryUiState(
                        isLoading = false,
                        destination = destination,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    AppEntryUiState(
                        isLoading = false,
                        errorMessage =
                            error.message
                                ?: "Unable to determine the signed-in user.",
                    )
            }
        }
    }

    fun openEntry() {
        _uiState.value =
            _uiState.value.copy(
                destination = AppDestination.ENTRY,
                errorMessage = null,
            )
    }

    fun continueAsPatient() {
        _uiState.value =
            _uiState.value.copy(
                destination = AppDestination.PATIENT_SIGN_IN,
                errorMessage = null,
            )
    }

    fun patientSignedIn() {
        _uiState.value =
            _uiState.value.copy(
                destination = AppDestination.PATIENT,
                errorMessage = null,
            )
    }

    fun openTherapistSignIn() {
        _uiState.value =
            _uiState.value.copy(
                destination = AppDestination.THERAPIST_SIGN_IN,
                errorMessage = null,
            )
    }

    fun signOut() {
        authSession.signOut()

        _uiState.value =
            AppEntryUiState(
                isLoading = false,
                destination = AppDestination.ENTRY,
            )
    }

    fun therapistSignedIn() {
        _uiState.value =
            _uiState.value.copy(
                destination = AppDestination.THERAPIST,
                errorMessage = null,
            )
    }
}
