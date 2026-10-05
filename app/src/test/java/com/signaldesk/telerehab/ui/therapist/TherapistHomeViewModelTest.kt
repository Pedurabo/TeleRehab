package com.signaldesk.telerehab.ui.therapist

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import com.signaldesk.telerehab.domain.auth.AuthSession
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import com.signaldesk.telerehab.domain.therapist.CurrentWeekStartProvider
import com.signaldesk.telerehab.domain.therapist.GetAssignedPatients
import com.signaldesk.telerehab.domain.therapist.GetPatientAssignmentsForTherapist
import com.signaldesk.telerehab.domain.therapist.GetPatientCompletedSessionsSinceForTherapist
import com.signaldesk.telerehab.domain.therapist.GetPatientRecentSessionsForTherapist
import com.signaldesk.telerehab.domain.therapist.SavePatientAssignmentForTherapist
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseAssignmentRemoteSource
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseSessionRemoteSource
import com.signaldesk.telerehab.domain.therapist.TherapistPatient
import com.signaldesk.telerehab.domain.therapist.TherapistPatientRepository
import com.signaldesk.telerehab.domain.therapist.WeeklyAdherenceCalculator
import com.signaldesk.telerehab.domain.therapist.WeeklyAdherenceStatus
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TherapistHomeViewModelTest {

    private val dispatcher =
        UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectingPatientPopulatesWeeklyAdherenceSummary() =
        runTest {
            val therapistId = "therapist-1"
            val patientId = "patient-1"

            val assignment =
                ExerciseAssignment(
                    id = "assignment-1",
                    patientId = patientId,
                    exerciseId = "knee-flexion",
                    title = "Knee Flexion",
                    instructions = "Perform slowly.",
                    targetRepetitions = 5,
                    targetSessionsPerWeek = 3,
                    flexedAtOrBelowDegrees = 80.0,
                    extendedAtOrAboveDegrees = 130.0,
                    status = ExerciseAssignmentStatus.ACTIVE,
                )

            val weeklySessions =
                listOf(
                    completedSession(
                        id = "session-1",
                        patientId = patientId,
                    ),
                    completedSession(
                        id = "session-2",
                        patientId = patientId,
                    ),
                )

            val assignmentSource =
                FakeAssignmentSource(
                    assignments = listOf(assignment),
                )

            val sessionSource =
                FakeSessionSource(
                    recentSessions = weeklySessions,
                    weeklySessions = weeklySessions,
                )

            val viewModel =
                TherapistHomeViewModel(
                    ensureSignedIn =
                        EnsureSignedIn(
                            FakeAuthSession(therapistId),
                        ),
                    getAssignedPatients =
                        GetAssignedPatients(
                            FakePatientRepository(
                                therapistId = therapistId,
                                patientId = patientId,
                            ),
                        ),
                    getPatientAssignments =
                        GetPatientAssignmentsForTherapist(
                            assignmentSource,
                        ),
                    getPatientRecentSessions =
                        GetPatientRecentSessionsForTherapist(
                            sessionSource,
                        ),
                    getPatientCompletedSessionsSince =
                        GetPatientCompletedSessionsSinceForTherapist(
                            sessionSource,
                        ),
                    savePatientAssignment =
                        SavePatientAssignmentForTherapist(
                            assignmentSource,
                        ),
                    weeklyAdherenceCalculator =
                        WeeklyAdherenceCalculator(),
                    currentWeekStartProvider =
                        CurrentWeekStartProvider(),
                )

            advanceUntilIdle()

            viewModel.selectPatient(patientId)

            advanceUntilIdle()

            val state =
                viewModel.uiState.value

            assertEquals(patientId, state.selectedPatientId)

            val adherence =
                state.assignmentWeeklyAdherence.single()

            assertEquals("assignment-1", adherence.assignmentId)
            assertEquals("Knee Flexion", adherence.assignmentTitle)
            assertEquals(2, adherence.completedSessions)
            assertEquals(3, adherence.targetSessions)
            assertEquals(66, adherence.percent)
            assertEquals(
                WeeklyAdherenceStatus.IN_PROGRESS,
                adherence.status,
            )
        }

    @Test
    fun selectingPatientGroupsWeeklyAdherenceByAssignment() =
        runTest {
            val therapistId = "therapist-1"
            val patientId = "patient-1"

            val assignments =
                listOf(
                    ExerciseAssignment(
                        id = "assignment-1",
                        patientId = patientId,
                        exerciseId = "knee-flexion",
                        title = "Knee Flexion",
                        instructions = "Perform slowly.",
                        targetRepetitions = 5,
                        targetSessionsPerWeek = 3,
                        flexedAtOrBelowDegrees = 80.0,
                        extendedAtOrAboveDegrees = 130.0,
                        status = ExerciseAssignmentStatus.ACTIVE,
                    ),
                    ExerciseAssignment(
                        id = "assignment-2",
                        patientId = patientId,
                        exerciseId = "knee-extension",
                        title = "Knee Extension",
                        instructions = "Extend under control.",
                        targetRepetitions = 6,
                        targetSessionsPerWeek = 2,
                        flexedAtOrBelowDegrees = 85.0,
                        extendedAtOrAboveDegrees = 135.0,
                        status = ExerciseAssignmentStatus.ACTIVE,
                    ),
                )

            val weeklySessions =
                listOf(
                    completedSession(
                        id = "session-1",
                        patientId = patientId,
                        assignmentId = "assignment-1",
                    ),
                    completedSession(
                        id = "session-2",
                        patientId = patientId,
                        assignmentId = "assignment-1",
                    ),
                    completedSession(
                        id = "session-3",
                        patientId = patientId,
                        assignmentId = "assignment-2",
                    ),
                )

            val assignmentSource =
                FakeAssignmentSource(assignments)

            val sessionSource =
                FakeSessionSource(
                    recentSessions = weeklySessions,
                    weeklySessions = weeklySessions,
                )

            val viewModel =
                TherapistHomeViewModel(
                    ensureSignedIn =
                        EnsureSignedIn(
                            FakeAuthSession(therapistId),
                        ),
                    getAssignedPatients =
                        GetAssignedPatients(
                            FakePatientRepository(
                                therapistId = therapistId,
                                patientId = patientId,
                            ),
                        ),
                    getPatientAssignments =
                        GetPatientAssignmentsForTherapist(
                            assignmentSource,
                        ),
                    getPatientRecentSessions =
                        GetPatientRecentSessionsForTherapist(
                            sessionSource,
                        ),
                    getPatientCompletedSessionsSince =
                        GetPatientCompletedSessionsSinceForTherapist(
                            sessionSource,
                        ),
                    savePatientAssignment =
                        SavePatientAssignmentForTherapist(
                            assignmentSource,
                        ),
                    weeklyAdherenceCalculator =
                        WeeklyAdherenceCalculator(),
                    currentWeekStartProvider =
                        CurrentWeekStartProvider(),
                )

            advanceUntilIdle()
            viewModel.selectPatient(patientId)
            advanceUntilIdle()

            val adherence =
                viewModel.uiState.value.assignmentWeeklyAdherence

            assertEquals(2, adherence.size)

            assertEquals("assignment-1", adherence[0].assignmentId)
            assertEquals(2, adherence[0].completedSessions)
            assertEquals(3, adherence[0].targetSessions)
            assertEquals(66, adherence[0].percent)

            assertEquals("assignment-2", adherence[1].assignmentId)
            assertEquals(1, adherence[1].completedSessions)
            assertEquals(2, adherence[1].targetSessions)
            assertEquals(50, adherence[1].percent)
        }


    private fun completedSession(
        id: String,
        patientId: String,
        assignmentId: String = "assignment-1",
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            assignmentId = assignmentId,
            patientId = patientId,
            startedAt = Instant.parse("2026-10-05T08:00:00Z"),
            completedAt = Instant.parse("2026-10-05T08:15:00Z"),
            status = ExerciseSessionStatus.COMPLETED,
            syncStatus = SyncStatus.SYNCED,
            metrics =
                ExerciseSessionMetrics(
                    completedRepetitions = 5,
                ),
        )

    private class FakeAuthSession(
        private val userId: String,
    ) : AuthSession {

        override fun currentUserId(): String =
            userId

        override suspend fun signInAnonymously(): String =
            userId

        override suspend fun signInWithEmailAndPassword(
            email: String,
            password: String,
        ): String =
            userId

        override fun signOut() = Unit
    }

    private class FakePatientRepository(
        therapistId: String,
        patientId: String,
    ) : TherapistPatientRepository {

        private val patient =
            TherapistPatient(
                therapistId = therapistId,
                patientId = patientId,
                displayName = "Test Patient",
            )

        override suspend fun findPatientsForTherapist(
            therapistId: String,
        ): List<TherapistPatient> =
            listOf(patient)

        override suspend fun isAssigned(
            therapistId: String,
            patientId: String,
        ): Boolean =
            true
    }

    private class FakeAssignmentSource(
        private val assignments: List<ExerciseAssignment>,
    ) : TherapistExerciseAssignmentRemoteSource {

        override suspend fun fetchForPatient(
            therapistId: String,
            patientId: String,
        ): List<ExerciseAssignment> =
            assignments

        override suspend fun saveForPatient(
            therapistId: String,
            assignment: ExerciseAssignment,
        ) = Unit
    }

    private class FakeSessionSource(
        private val recentSessions: List<ExerciseSession>,
        private val weeklySessions: List<ExerciseSession>,
    ) : TherapistExerciseSessionRemoteSource {

        override suspend fun fetchRecentCompletedForPatient(
            therapistId: String,
            patientId: String,
            limit: Int,
        ): List<ExerciseSession> =
            recentSessions.take(limit)

        override suspend fun fetchCompletedForPatientSince(
            therapistId: String,
            patientId: String,
            sinceEpochMillis: Long,
        ): List<ExerciseSession> =
            weeklySessions
    }
}
