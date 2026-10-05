package com.signaldesk.telerehab.di

import com.signaldesk.telerehab.data.therapist.FirestoreTherapistExerciseAssignmentRemoteSource
import com.signaldesk.telerehab.data.therapist.FirestoreTherapistPatientRepository
import com.signaldesk.telerehab.data.therapist.FirestoreTherapistExerciseSessionRemoteSource
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseAssignmentRemoteSource
import com.signaldesk.telerehab.domain.therapist.TherapistPatientRepository
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseSessionRemoteSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TherapistModule {

    @Binds
    @Singleton
    abstract fun bindTherapistPatientRepository(
        implementation: FirestoreTherapistPatientRepository,
    ): TherapistPatientRepository

    @Binds
    @Singleton
    abstract fun bindTherapistExerciseAssignmentRemoteSource(
        implementation: FirestoreTherapistExerciseAssignmentRemoteSource,
    ): TherapistExerciseAssignmentRemoteSource

    @Binds
    @Singleton
    abstract fun bindTherapistExerciseSessionRemoteSource(
        implementation: FirestoreTherapistExerciseSessionRemoteSource,
    ): TherapistExerciseSessionRemoteSource}
