package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCase
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCaseImpl

/**
 * Hilt module for UseCase dependency injection
 *
 * Provides UseCase interfaces with their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

  @Provides
  fun providePostBookmarkUseCase(impl: PostBookmarkUseCaseImpl): PostBookmarkUseCase = impl
}
