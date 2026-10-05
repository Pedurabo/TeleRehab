package com.signaldesk.telerehab.data.assignment.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
    tableName = "exercise_assignments",
)
data class ExerciseAssignmentEntity(
    @PrimaryKey
    val id: String,
    val patientId: String,
    val exerciseId: String,
    val title: String,
    val instructions: String,
    val targetRepetitions: Int,
    val flexedAtOrBelowDegrees: Double?,
    val extendedAtOrAboveDegrees: Double?,
    val status: String,
)
