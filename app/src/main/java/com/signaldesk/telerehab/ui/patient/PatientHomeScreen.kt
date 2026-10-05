package com.signaldesk.telerehab.ui.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.signaldesk.telerehab.domain.analysis.PoseFrame
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
    onPoseFrame: (PoseFrame) -> Unit,
    onConfigureTracking: (Int, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
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
                        exerciseTitle =
                            state.selectedAssignment.title,
                        targetRepetitions =
                            state.selectedAssignment.targetRepetitions,
                        sessionId =
                            state.startedSessionId,
                        analysisState =
                            guidedAnalysisState,
                        onPoseFrame =
                            onPoseFrame,
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
            text =
                "Loading your rehabilitation planÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦",
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
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
                text = "Assigned exercises",
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
                            "RefreshingÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦"
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

        if (state.recentSessions.isNotEmpty()) {
            val totalRepetitions =
                state.recentSessions.sumOf {
                    it.metrics?.completedRepetitions ?: 0
                }

            val latestMetrics =
                state.recentSessions.first().metrics

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text("Completed sessions: ${state.recentSessions.size}")
                    Text("Recent repetitions: $totalRepetitions")

                    if (
                        latestMetrics?.minimumKneeAngleDegrees != null &&
                        latestMetrics.maximumKneeAngleDegrees != null
                    ) {
                        Text(
                            "Latest knee range: %.1f - %.1f degrees".format(
                                latestMetrics.minimumKneeAngleDegrees,
                                latestMetrics.maximumKneeAngleDegrees,
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.recentSessions.isNotEmpty()) {
            Text(text = "Recent sessions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            state.recentSessions.take(3).forEach { session ->
                RecentSessionCard(session)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (state.assignments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier.padding(20.dp),
                ) {
                    Text(
                        text =
                            "No active exercises yet",
                        style =
                            MaterialTheme.typography.titleMedium,
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp),
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
            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp,
                    ),
            ) {
                items(
                    items = state.assignments,
                    key = ExerciseAssignment::id,
                ) { assignment ->
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
    }
}

@Composable
private fun RecentSessionCard(session: ExerciseSession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${session.metrics?.completedRepetitions ?: 0} repetitions")
            session.metrics?.let { metrics ->
                if (metrics.minimumKneeAngleDegrees != null && metrics.maximumKneeAngleDegrees != null) {
                    Text("Knee range: %.1f - %.1f degrees".format(metrics.minimumKneeAngleDegrees, metrics.maximumKneeAngleDegrees))
                }
            }
            Text("Sync: ${session.syncStatus}")
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
                            "StartingÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦"

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



