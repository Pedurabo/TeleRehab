package com.signaldesk.telerehab.ui.therapist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.therapist.GetAssignedPatients
import com.signaldesk.telerehab.domain.therapist.GetPatientAssignmentsForTherapist
import com.signaldesk.telerehab.domain.therapist.SavePatientAssignmentForTherapist
import com.signaldesk.telerehab.domain.therapist.TherapistPatient
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
    private val savePatientAssignment: SavePatientAssignmentForTherapist,
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

                _uiState.value =
                    _uiState.value.copy(
                        selectedPatientId = patientId,
                        assignments = assignments,
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
    fun clearSaveMessage() {
        _uiState.value =
            _uiState.value.copy(
                saveMessage = null,
            )
    }}
