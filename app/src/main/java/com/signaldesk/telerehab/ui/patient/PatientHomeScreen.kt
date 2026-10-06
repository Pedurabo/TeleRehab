package com.signaldesk.telerehab.ui.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.signaldesk.telerehab.domain.analysis.PoseObservation
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.ui.session.GuidedExerciseSessionScreen
import com.signaldesk.telerehab.ui.session.GuidedSessionAnalysisState

@Composable
fun PatientHomeScreen(
    state: PatientHomeUiState,
    onRefresh: () -> Unit,
    onAssignmentSelected: (ExerciseAssignment) -> Unit,
    onCloseAssignment: () -> Unit,
    onStartSession: () -> Unit,
    onFinishSession: () -> Unit = {},
    guidedAnalysisState: GuidedSessionAnalysisState,
    onPoseObservation: (PoseObservation) -> Unit,
    onPoseError: (Throwable) -> Unit,
    onConfigureTracking: (Int, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .semantics {
                    contentDescription =
                        "TeleRehab patient experience"
                },
    ) { contentPadding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }

                state.startedSessionId != null &&
                    state.selectedAssignment != null -> {
                    androidx.compose.runtime.LaunchedEffect(
                        state.selectedAssignment.id,
                    ) {
                        onConfigureTracking(
                            state.selectedAssignment.targetRepetitions,
                            state.selectedAssignment.flexedAtOrBelowDegrees,
                            state.selectedAssignment.extendedAtOrAboveDegrees,
                        )
                    }
                    GuidedExerciseSessionScreen(
                        exerciseId =
                            state.selectedAssignment.exerciseId,
                        exerciseTitle =
                            state.selectedAssignment.title,
                        targetRepetitions =
                            state.selectedAssignment.targetRepetitions,
                        sessionId =
                            state.startedSessionId,
                        analysisState =
                            guidedAnalysisState,
                        onPoseObservation =
                            onPoseObservation,
                        onPoseError =
                            onPoseError,
                        onFinishSession = onFinishSession,
                    )
                }

                state.selectedAssignment != null -> {
                    AssignmentDetailContent(
                        assignment = state.selectedAssignment,
                        sessionId = state.startedSessionId,
                        isStartingSession = state.isStartingSession,
                        errorMessage = state.errorMessage,
                        onBack = onCloseAssignment,
                        onStartSession = onStartSession,
                    )
                }

                else -> {
                    AssignmentListContent(
                        state = state,
                        onRefresh = onRefresh,
                        onAssignmentSelected =
                            onAssignmentSelected,
                        onSignOut = onSignOut,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center,
    ) {
        CircularProgressIndicator()

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        Text(
            text = "Loading your rehabilitation plan...",
            style =
                MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AssignmentListContent(
    state: PatientHomeUiState,
    onRefresh: () -> Unit,
    onAssignmentSelected: (ExerciseAssignment) -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Text(
            text = "TeleRehab",
            style =
                MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Your rehabilitation plan",
            style =
                MaterialTheme.typography.titleLarge,
        )

        OutlinedButton(
            onClick = onSignOut,
        ) {
            Text("Sign out")
        }

        Spacer(
            modifier = Modifier.height(8.dp),
        )

        Text(
            text =
                "Your saved exercises remain available even when you are offline.",
            style =
                MaterialTheme.typography.bodyMedium,
        )

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Text(
                text = "Dashboard",
                style =
                    MaterialTheme.typography.titleMedium,
            )

            OutlinedButton(
                onClick = onRefresh,
                enabled = !state.isRefreshing,
            ) {
                Text(
                    text =
                        if (state.isRefreshing) {
                            "Refreshing..."
                        } else {
                            "Refresh"
                        },
                )
            }
        }

        state.refreshMessage?.let { message ->
            Spacer(
                modifier = Modifier.height(8.dp),
            )

            Text(
                text = message,
                style =
                    MaterialTheme.typography.bodySmall,
            )
        }

        state.errorMessage?.let { message ->
            Spacer(
                modifier = Modifier.height(8.dp),
            )

            Text(
                text = message,
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.error,
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        Text(
            text = "Assigned exercises",
            style =
                MaterialTheme.typography.titleLarge,
        )

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        if (state.assignments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No active exercises yet",
                        style =
                            MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text =
                            "When your rehabilitation program is assigned, it will appear here.",
                        style =
                            MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                state.assignments.forEach { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        onOpen = {
                            onAssignmentSelected(
                                assignment,
                            )
                        },
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(32.dp),
        )

        if (state.recentCompletedSessionCount > 0) {
            Text(
                text = "Progress",
                style =
                    MaterialTheme.typography.titleLarge,
            )

            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text =
                            "Completed sessions: ${state.recentCompletedSessionCount}",
                        style =
                            MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        text =
                            "Recent repetitions: ${state.recentTotalRepetitions}",
                    )

                    state.latestCompletedRepetitions?.let {
                        Text(
                            text =
                                "Latest repetitions: $it",
                        )
                    }

                    state.repetitionChangeFromPrevious
                        ?.let { change ->
                            Text(
                                text =
                                    "Repetitions vs previous: ${
                                        if (change >= 0) {
                                            "+$change"
                                        } else {
                                            change
                                        }
                                    }",
                            )
                        }

                    state.latestKneeRangeWidthDegrees
                        ?.let { width ->
                            Text(
                                text =
                                    "Latest knee range: %.1f degrees"
                                        .format(width),
                            )
                        }

                    state.kneeRangeChangeFromPreviousDegrees
                        ?.let { change ->
                            Text(
                                text =
                                    if (
                                        kotlin.math.abs(change) < 0.05
                                    ) {
                                        "Range vs previous: no measurable change"
                                    } else {
                                        "Range vs previous: ${
                                            if (change > 0) "+" else ""
                                        }%.1f degrees".format(change)
                                    },
                            )
                        }
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp),
            )
        }

        if (state.recentSessions.isNotEmpty()) {
            Text(
                text = "Recent sessions",
                style =
                    MaterialTheme.typography.titleLarge,
            )

            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                state.recentSessions.forEach { session ->
                    RecentSessionCard(
                        session = session,
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp),
            )
        }

    }
}

@Composable
private fun RecentSessionCard(
    session: ExerciseSession,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text =
                    "${session.metrics?.completedRepetitions ?: 0} repetitions",
                style =
                    MaterialTheme.typography.titleMedium,
            )

            session.metrics?.let { metrics ->
                val minimum =
                    metrics.minimumKneeAngleDegrees

                val maximum =
                    metrics.maximumKneeAngleDegrees

                if (minimum != null && maximum != null) {
                    Text(
                        text =
                            "Knee range: %.1f - %.1f degrees"
                                .format(minimum, maximum),
                        style =
                            MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        text = "No knee range recorded",
                        style =
                            MaterialTheme.typography.bodySmall,
                    )
                }
            } ?: Text(
                text = "No session metrics recorded",
                style =
                    MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: ExerciseAssignment,
    onOpen: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        onClick = onOpen,
    ) {
        Column(
            modifier =
                Modifier.padding(20.dp),
        ) {
            Text(
                text = assignment.title,
                style =
                    MaterialTheme.typography.titleMedium,
            )

            Spacer(
                modifier = Modifier.height(6.dp),
            )

            Text(
                text =
                    "${assignment.targetRepetitions} repetitions",
                style =
                    MaterialTheme.typography.bodyMedium,
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )

            Text(
                text = assignment.instructions,
                style =
                    MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AssignmentDetailContent(
    assignment: ExerciseAssignment,
    sessionId: String?,
    isStartingSession: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    onFinishSession: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
    ) {
        Spacer(
            modifier = Modifier.height(12.dp),
        )

        OutlinedButton(
            onClick = onBack,
        ) {
            Text(
                text = "Back",
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Text(
            text = assignment.title,
            style =
                MaterialTheme.typography.headlineSmall,
        )

        Spacer(
            modifier = Modifier.height(12.dp),
        )

        Text(
            text = "Instructions",
            style =
                MaterialTheme.typography.titleMedium,
        )

        Spacer(
            modifier = Modifier.height(6.dp),
        )

        Text(
            text = assignment.instructions,
            style =
                MaterialTheme.typography.bodyLarge,
        )

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Session target",
                    style =
                        MaterialTheme.typography.titleMedium,
                )

                Text(
                    text =
                        "${assignment.targetRepetitions} repetitions",
                )

                Text(
                    text =
                        "${assignment.targetSessionsPerWeek} sessions per week",
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        HorizontalDivider()

        Spacer(
            modifier = Modifier.height(20.dp),
        )

        Text(
            text =
                "Target: ${assignment.targetRepetitions} repetitions",
            style =
                MaterialTheme.typography.titleMedium,
        )

        Spacer(
            modifier = Modifier.height(24.dp),
        )

        Button(
            modifier =
                Modifier.fillMaxWidth(),
            onClick = onStartSession,
            enabled =
                !isStartingSession &&
                    sessionId == null,
        ) {
            Text(
                text =
                    when {
                        sessionId != null ->
                            "Session started"

                        isStartingSession ->
                            "Starting..."

                        else ->
                            "Start session"
                    },
            )
        }

        sessionId?.let {
            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Text(
                text =
                    "Your exercise session is ready for the guided camera step.",
                style =
                    MaterialTheme.typography.bodyMedium,
            )
        }

        errorMessage?.let { message ->
            Spacer(
                modifier = Modifier.height(12.dp),
            )

            Text(
                text = message,
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
