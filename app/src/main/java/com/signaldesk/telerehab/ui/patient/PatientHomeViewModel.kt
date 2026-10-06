package com.signaldesk.telerehab.ui.patient


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.usecase.GetActiveExerciseAssignments
import com.signaldesk.telerehab.domain.assignment.usecase.RefreshExerciseAssignments
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.usecase.CompleteExerciseSession
import com.signaldesk.telerehab.domain.session.usecase.GetRecentCompletedSessions
import com.signaldesk.telerehab.domain.session.usecase.StartExerciseSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class PatientHomeUiState(
    val patientId: String? = null,
    val assignments: List<ExerciseAssignment> = emptyList(),
    val recentSessions: List<ExerciseSession> = emptyList(),
    val recentCompletedSessionCount: Int = 0,
    val recentTotalRepetitions: Int = 0,
    val latestCompletedRepetitions: Int? = null,
    val repetitionChangeFromPrevious: Int? = null,
    val latestKneeRangeWidthDegrees: Double? = null,
    val kneeRangeChangeFromPreviousDegrees: Double? = null,
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
    private val completeExerciseSession: CompleteExerciseSession,
    private val getRecentCompletedSessions: GetRecentCompletedSessions,
) : ViewModel() {

    private val _uiState =
        kotlinx.coroutines.flow.MutableStateFlow(
            PatientHomeUiState(),
        )

    val uiState:
        kotlinx.coroutines.flow.StateFlow<PatientHomeUiState> =
        _uiState

    fun load() {
        _uiState.value =
            PatientHomeUiState()

        viewModelScope.launch {
            try {
                val patientId =
                    ensureSignedIn()

                val cachedAssignments =
                    getActiveExerciseAssignments(
                        patientId = patientId,
                    )

                val recentSessions =
                    getRecentCompletedSessions(
                        patientId = patientId,
                        limit = 10,
                    )

                val progress =
                    recentSessions.toProgressSummary()

                _uiState.value =
                    _uiState.value.copy(
                        patientId = patientId,
                        assignments = cachedAssignments,
                        recentSessions = recentSessions,
                        recentCompletedSessionCount =
                            progress.completedSessionCount,
                        recentTotalRepetitions =
                            progress.totalRepetitions,
                        latestCompletedRepetitions =
                            progress.latestRepetitions,
                        repetitionChangeFromPrevious =
                            progress.repetitionChange,
                        latestKneeRangeWidthDegrees =
                            progress.latestRangeWidth,
                        kneeRangeChangeFromPreviousDegrees =
                            progress.rangeWidthChange,
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

    fun reset() {
        _uiState.value =
            PatientHomeUiState()
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
    fun finishActiveSession(
        metrics: ExerciseSessionMetrics,
    ) {
        val sessionId =
            _uiState.value.startedSessionId
                ?: return

        viewModelScope.launch {
            try {
                completeExerciseSession(
                    sessionId = sessionId,
                    metrics = metrics,
                )

                val patientId =
                    _uiState.value.patientId

                val recentSessions =
                    patientId?.let {
                        getRecentCompletedSessions(
                            patientId = it,
                            limit = 10,
                        )
                    } ?: emptyList()

                val progress =
                    recentSessions.toProgressSummary()

                _uiState.value =
                    _uiState.value.copy(
                        selectedAssignment = null,
                        startedSessionId = null,
                        recentSessions = recentSessions,
                        recentCompletedSessionCount =
                            progress.completedSessionCount,
                        recentTotalRepetitions =
                            progress.totalRepetitions,
                        latestCompletedRepetitions =
                            progress.latestRepetitions,
                        repetitionChangeFromPrevious =
                            progress.repetitionChange,
                        latestKneeRangeWidthDegrees =
                            progress.latestRangeWidth,
                        kneeRangeChangeFromPreviousDegrees =
                            progress.rangeWidthChange,
                        errorMessage = null,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "Unable to finish the exercise session.",
                    )
            }
        }
    }



    private data class ProgressSummary(
        val completedSessionCount: Int,
        val totalRepetitions: Int,
        val latestRepetitions: Int?,
        val repetitionChange: Int?,
        val latestRangeWidth: Double?,
        val rangeWidthChange: Double?,
    )

    private fun List<ExerciseSession>.toProgressSummary(): ProgressSummary {
        val latestMetrics =
            firstOrNull()?.metrics

        val previousMetrics =
            getOrNull(1)?.metrics

        val latestRepetitions =
            latestMetrics?.completedRepetitions

        val previousRepetitions =
            previousMetrics?.completedRepetitions

        val validRangeWidths =
            mapNotNull { session ->
                val metrics =
                    session.metrics
                        ?: return@mapNotNull null

                val minimum =
                    metrics.minimumKneeAngleDegrees

                val maximum =
                    metrics.maximumKneeAngleDegrees

                if (
                    minimum != null &&
                    maximum != null
                ) {
                    maximum - minimum
                } else {
                    null
                }
            }

        val latestRangeWidth =
            validRangeWidths.getOrNull(0)

        val previousRangeWidth =
            validRangeWidths.getOrNull(1)

        return ProgressSummary(
            completedSessionCount = size,
            totalRepetitions =
                sumOf {
                    it.metrics?.completedRepetitions ?: 0
                },
            latestRepetitions = latestRepetitions,
            repetitionChange =
                if (
                    latestRepetitions != null &&
                    previousRepetitions != null
                ) {
                    latestRepetitions - previousRepetitions
                } else {
                    null
                },
            latestRangeWidth = latestRangeWidth,
            rangeWidthChange =
                if (
                    latestRangeWidth != null &&
                    previousRangeWidth != null
                ) {
                    latestRangeWidth - previousRangeWidth
                } else {
                    null
                },
        )
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

