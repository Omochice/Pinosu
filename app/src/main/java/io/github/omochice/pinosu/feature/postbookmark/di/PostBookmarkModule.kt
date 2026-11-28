package io.github.omochice.pinosu.feature.postbookmark.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.postbookmark.domain.usecase.PostBookmarkUseCase
import io.github.omochice.pinosu.feature.postbookmark.domain.usecase.PostBookmarkUseCaseImpl

/**
 * Hilt module for PostBookmark feature dependency injection
 *
 * Provides UseCase dependencies for the post bookmark feature.
 */
@Module
@InstallIn(SingletonComponent::class)
interface PostBookmarkModule {

  @Binds fun bindPostBookmarkUseCase(impl: PostBookmarkUseCaseImpl): PostBookmarkUseCase
}
