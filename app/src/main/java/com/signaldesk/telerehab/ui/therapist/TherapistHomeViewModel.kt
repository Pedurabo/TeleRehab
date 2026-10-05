package com.signaldesk.telerehab.ui.therapist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.therapist.GetAssignedPatients
import com.signaldesk.telerehab.domain.therapist.CurrentWeekStartProvider
import com.signaldesk.telerehab.domain.therapist.GetPatientAssignmentsForTherapist
import com.signaldesk.telerehab.domain.therapist.GetPatientRecentSessionsForTherapist
import com.signaldesk.telerehab.domain.therapist.GetPatientCompletedSessionsSinceForTherapist
import com.signaldesk.telerehab.domain.therapist.SavePatientAssignmentForTherapist
import com.signaldesk.telerehab.domain.therapist.TherapistPatient
import com.signaldesk.telerehab.domain.therapist.WeeklyAdherenceCalculator
import com.signaldesk.telerehab.domain.therapist.WeeklyAdherenceStatus
import com.signaldesk.telerehab.domain.session.ExerciseSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TherapistHomeUiState(
    val therapistId: String? = null,
    val patients: List<TherapistPatient> = emptyList(),
    val selectedPatientId: String? = null,
    val assignments: List<ExerciseAssignment> = emptyList(),
    val recentSessions: List<ExerciseSession> = emptyList(),
    val completedSessionCount: Int = 0,
    val weeklyCompletedSessionCount: Int = 0,
    val weeklyTargetSessionCount: Int? = null,
    val weeklyAdherencePercent: Int? = null,
    val weeklyAdherenceStatus: WeeklyAdherenceStatus? = null,
    val latestCompletedRepetitions: Int? = null,
    val repetitionTrend: Int? = null,
    val isLoading: Boolean = true,
    val isSavingAssignment: Boolean = false,
    val saveMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class TherapistHomeViewModel @Inject constructor(
    private val ensureSignedIn: EnsureSignedIn,
    private val getAssignedPatients: GetAssignedPatients,
    private val getPatientAssignments: GetPatientAssignmentsForTherapist,
    private val getPatientRecentSessions: GetPatientRecentSessionsForTherapist,
    private val getPatientCompletedSessionsSince:
        GetPatientCompletedSessionsSinceForTherapist,
    private val savePatientAssignment: SavePatientAssignmentForTherapist,
    private val weeklyAdherenceCalculator: WeeklyAdherenceCalculator,
    private val currentWeekStartProvider: CurrentWeekStartProvider,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            TherapistHomeUiState(),
        )

    val uiState: StateFlow<TherapistHomeUiState> =
        _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val therapistId =
                    ensureSignedIn()

                val patients =
                    getAssignedPatients(
                        therapistId = therapistId,
                    )

                _uiState.value =
                    TherapistHomeUiState(
                        therapistId = therapistId,
                        patients = patients,
                        isLoading = false,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage =
                            error.message
                                ?: "Unable to load therapist patients.",
                    )
            }
        }
    }

    fun selectPatient(
        patientId: String,
    ) {
        val therapistId =
            _uiState.value.therapistId
                ?: return

        viewModelScope.launch {
            try {
                val assignments =
                    getPatientAssignments(
                        therapistId = therapistId,
                        patientId = patientId,
                    )

                val recentSessions =
                    getPatientRecentSessions(
                        therapistId = therapistId,
                        patientId = patientId,
                    )

                val weekStart =
                    currentWeekStartProvider.epochMillis()

                val weeklySessions =
                    getPatientCompletedSessionsSince(
                        therapistId = therapistId,
                        patientId = patientId,
                        sinceEpochMillis = weekStart,
                    )

                val weeklyTarget =
                    assignments
                        .firstOrNull()
                        ?.targetSessionsPerWeek

                val weeklyAdherence =
                    if (weeklyTarget != null) {
                        weeklyAdherenceCalculator.calculate(
                            completedSessions = weeklySessions.size,
                            targetSessions = weeklyTarget,
                        )
                    } else {
                        null
                    }

                val latestRepetitions =
                    recentSessions
                        .firstOrNull()
                        ?.metrics
                        ?.completedRepetitions

                val previousRepetitions =
                    recentSessions
                        .getOrNull(1)
                        ?.metrics
                        ?.completedRepetitions

                _uiState.value =
                    _uiState.value.copy(
                        selectedPatientId = patientId,
                        assignments = assignments,
                        recentSessions = recentSessions,
                        completedSessionCount = recentSessions.size,
                        weeklyCompletedSessionCount = weeklySessions.size,
                        weeklyTargetSessionCount = weeklyTarget,
                        weeklyAdherencePercent = weeklyAdherence?.percent,
                        weeklyAdherenceStatus = weeklyAdherence?.status,
                        latestCompletedRepetitions = latestRepetitions,
                        repetitionTrend =
                            if (
                                latestRepetitions != null &&
                                previousRepetitions != null
                            ) {
                                latestRepetitions - previousRepetitions
                            } else {
                                null
                            },
                        errorMessage = null,
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "Unable to load patient assignments.",
                    )
            }
        }
    }

    fun saveAssignment(
        assignment: ExerciseAssignment,
        targetRepetitions: Int,
        targetSessionsPerWeek: Int,
        flexedAtOrBelowDegrees: Double?,
        extendedAtOrAboveDegrees: Double?,
    ) {
        val therapistId =
            _uiState.value.therapistId
                ?: return

        viewModelScope.launch {
            try {
                _uiState.value =
                    _uiState.value.copy(
                        isSavingAssignment = true,
                        errorMessage = null,
                        saveMessage = null,
                    )

                val updated =
                    assignment.copy(
                        targetRepetitions = targetRepetitions,
                        targetSessionsPerWeek = targetSessionsPerWeek,
                        flexedAtOrBelowDegrees = flexedAtOrBelowDegrees,
                        extendedAtOrAboveDegrees = extendedAtOrAboveDegrees,
                    )

                savePatientAssignment(
                    therapistId = therapistId,
                    assignment = updated,
                )

                val assignments =
                    getPatientAssignments(
                        therapistId = therapistId,
                        patientId = updated.patientId,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        assignments = assignments,
                        isSavingAssignment = false,
                        saveMessage = "Assignment updated.",
                    )
            } catch (error: Throwable) {
                _uiState.value =
                    _uiState.value.copy(
                        isSavingAssignment = false,
                        errorMessage =
                            error.message
                                ?: "Unable to update assignment.",
                    )
            }
        }
    }
    fun clearSelectedPatient() {
        _uiState.value =
            _uiState.value.copy(
                selectedPatientId = null,
                assignments = emptyList(),
                recentSessions = emptyList(),
                completedSessionCount = 0,
                weeklyCompletedSessionCount = 0,
                weeklyTargetSessionCount = null,
                weeklyAdherencePercent = null,
                weeklyAdherenceStatus = null,
                latestCompletedRepetitions = null,
                repetitionTrend = null,
                saveMessage = null,
                errorMessage = null,
            )
    }

    fun clearSaveMessage() {
        _uiState.value =
            _uiState.value.copy(
                saveMessage = null,
            )
    }}
