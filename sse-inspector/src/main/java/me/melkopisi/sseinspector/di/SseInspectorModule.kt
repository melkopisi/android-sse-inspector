package me.melkopisi.sseinspector.di

import me.melkopisi.sseinspector.SseInspector
import me.melkopisi.sseinspector.SseInspectorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SseInspectorModule {

    @Binds
    @Singleton
    abstract fun bindSseInspector(impl: SseInspectorImpl): SseInspector
}
