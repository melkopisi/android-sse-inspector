package me.melkopisi.sseinspector.di

import me.melkopisi.sseinspector.SseInspector
import me.melkopisi.sseinspector.SseInspectorNoOp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SseInspectorNoOpModule {

    @Binds
    @Singleton
    abstract fun bindSseInspector(impl: SseInspectorNoOp): SseInspector
}
