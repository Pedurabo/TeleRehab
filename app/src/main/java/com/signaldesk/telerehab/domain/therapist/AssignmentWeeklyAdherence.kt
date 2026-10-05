package com.signaldesk.telerehab.domain.therapist

data class AssignmentWeeklyAdherence(
    val assignmentId: String,
    val assignmentTitle: String,
    val completedSessions: Int,
    val targetSessions: Int,
    val percent: Int,
    val status: WeeklyAdherenceStatus,
)
