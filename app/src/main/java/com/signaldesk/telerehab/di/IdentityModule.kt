package com.signaldesk.telerehab.di

import com.signaldesk.telerehab.data.identity.FirestoreUserProfileRepository
import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IdentityModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        implementation: FirestoreUserProfileRepository,
    ): UserProfileRepository
}
