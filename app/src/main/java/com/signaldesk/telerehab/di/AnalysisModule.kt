package com.signaldesk.telerehab.di

import com.signaldesk.telerehab.data.analysis.MlKitPoseAnalysisEngine
import com.signaldesk.telerehab.domain.analysis.PoseAnalysisEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisModule {

    @Binds
    @Singleton
    abstract fun bindPoseAnalysisEngine(
        implementation: MlKitPoseAnalysisEngine,
    ): PoseAnalysisEngine
}
