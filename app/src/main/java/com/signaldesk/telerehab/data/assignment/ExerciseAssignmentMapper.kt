package com.signaldesk.telerehab.data.assignment

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentEntity
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import javax.inject.Inject

class ExerciseAssignmentMapper @Inject constructor() {

    fun toDomain(
        entity: ExerciseAssignmentEntity,
    ): ExerciseAssignment =
        ExerciseAssignment(
            id = entity.id,
            patientId = entity.patientId,
            exerciseId = entity.exerciseId,
            title = entity.title,
            instructions = entity.instructions,
            targetRepetitions = entity.targetRepetitions,
            status = ExerciseAssignmentStatus.valueOf(entity.status),
        )

    fun toEntity(
        assignment: ExerciseAssignment,
    ): ExerciseAssignmentEntity =
        ExerciseAssignmentEntity(
            id = assignment.id,
            patientId = assignment.patientId,
            exerciseId = assignment.exerciseId,
            title = assignment.title,
            instructions = assignment.instructions,
            targetRepetitions = assignment.targetRepetitions,
            status = assignment.status.name,
        )
}
