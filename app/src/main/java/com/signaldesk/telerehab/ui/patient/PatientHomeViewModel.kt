package com.signaldesk.telerehab.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.usecase.GetActiveExerciseAssignments
import com.signaldesk.telerehab.domain.assignment.usecase.RefreshExerciseAssignments
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.session.usecase.StartExerciseSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class PatientHomeUiState(
    val patientId: String? = null,
    val assignments: List<ExerciseAssignment> = emptyList(),
    val selectedAssignment: ExerciseAssignment? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val errorMessage: String? = null,
    val startedSessionId: String? = null,
    val isStartingSession: Boolean = false,
)

@HiltViewModel
class PatientHomeViewModel @Inject constructor(
    private val ensureSignedIn: EnsureSignedIn,
    private val getActiveExerciseAssignments: GetActiveExerciseAssignments,
    private val refreshExerciseAssignments: RefreshExerciseAssignments,
    private val startExerciseSession: StartExerciseSession,
) : ViewModel() {

    private val _uiState =
        kotlinx.coroutines.flow.MutableStateFlow(
            PatientHomeUiState(),
        )

    val uiState:
        kotlinx.coroutines.flow.StateFlow<PatientHomeUiState> =
        _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                val patientId =
                    ensureSignedIn()

                val cachedAssignments =
                    getActiveExerciseAssignments(
                        patientId = patientId,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        patientId = patientId,
                        assignments = cachedAssignments,
                        isLoading = false,
                        isRefreshing = true,
                        errorMessage = null,
                        refreshMessage = null,
                    )

                refreshFromCloud(
                    patientId = patientId,
                )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage =
                            error.message
                                ?: "Unable to load your rehabilitation plan.",
                    )
            }
        }
    }

    fun refresh() {
        val patientId =
            _uiState.value.patientId
                ?: return

        if (_uiState.value.isRefreshing) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isRefreshing = true,
                    refreshMessage = null,
                )

            refreshFromCloud(
                patientId = patientId,
            )
        }
    }

    fun selectAssignment(
        assignment: ExerciseAssignment,
    ) {
        _uiState.value =
            _uiState.value.copy(
                selectedAssignment = assignment,
                startedSessionId = null,
            )
    }

    fun closeAssignment() {
        _uiState.value =
            _uiState.value.copy(
                selectedAssignment = null,
                startedSessionId = null,
            )
    }

    fun startSelectedAssignment() {
        val state =
            _uiState.value

        val patientId =
            state.patientId
                ?: return

        val assignment =
            state.selectedAssignment
                ?: return

        if (state.isStartingSession) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value =
                    state.copy(
                        isStartingSession = true,
                        errorMessage = null,
                    )

                val session =
                    startExerciseSession(
                        assignmentId = assignment.id,
                        patientId = patientId,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        isStartingSession = false,
                        startedSessionId = session.id,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isStartingSession = false,
                        errorMessage =
                            error.message
                                ?: "Unable to start the exercise session.",
                    )
            }
        }
    }

    private suspend fun refreshFromCloud(
        patientId: String,
    ) {
        try {
            refreshExerciseAssignments(
                patientId = patientId,
            )

            val refreshedAssignments =
                getActiveExerciseAssignments(
                    patientId = patientId,
                )

            val currentSelection =
                _uiState.value.selectedAssignment

            val refreshedSelection =
                currentSelection?.let { selected ->
                    refreshedAssignments.firstOrNull {
                        it.id == selected.id
                    }
                }

            _uiState.value =
                _uiState.value.copy(
                    assignments = refreshedAssignments,
                    selectedAssignment = refreshedSelection,
                    isRefreshing = false,
                    refreshMessage = null,
                )
        } catch (error: Throwable) {
            /*
             * Offline-first behavior:
             * existing cached assignments remain untouched and visible.
             */
            _uiState.value =
                _uiState.value.copy(
                    isRefreshing = false,
                    refreshMessage =
                        "Showing saved assignments. Refresh is unavailable right now.",
                )
        }
    }
}
