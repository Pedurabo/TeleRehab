package com.signaldesk.telerehab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import com.signaldesk.telerehab.domain.identity.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppDestination {
    PATIENT,
    THERAPIST,
}

data class AppEntryUiState(
    val isLoading: Boolean = true,
    val destination: AppDestination? = null,
    val userId: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    private val ensureSignedIn: EnsureSignedIn,
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
                val userId =
                    ensureSignedIn()

                val profile =
                    userProfileRepository.findById(
                        userId = userId,
                    )

                val destination =
                    when (profile?.role) {
                        UserRole.THERAPIST ->
                            AppDestination.THERAPIST

                        UserRole.PATIENT,
                        null ->
                            AppDestination.PATIENT
                    }

                _uiState.value =
                    AppEntryUiState(
                        isLoading = false,
                        destination = destination,
                        userId = userId,
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
}
