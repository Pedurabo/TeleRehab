package com.signaldesk.telerehab.di

import com.signaldesk.telerehab.data.assignment.RoomExerciseAssignmentRepository
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AssignmentModule {

    @Binds
    @Singleton
    abstract fun bindExerciseAssignmentRepository(
        implementation: RoomExerciseAssignmentRepository,
    ): ExerciseAssignmentRepository
}
