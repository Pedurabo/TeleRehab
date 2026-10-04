package com.signaldesk.telerehab.data.assignment

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentDao
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomExerciseAssignmentRepository @Inject constructor(
    private val dao: ExerciseAssignmentDao,
    private val mapper: ExerciseAssignmentMapper,
) : ExerciseAssignmentRepository {

    override suspend fun findActiveByPatient(
        patientId: String,
    ): List<ExerciseAssignment> =
        dao
            .findActiveByPatient(
                patientId = patientId,
            )
            .map(mapper::toDomain)
}
