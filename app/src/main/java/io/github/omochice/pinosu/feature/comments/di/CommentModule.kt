package io.github.omochice.pinosu.feature.comments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.comments.repository.CommentRepository
import io.github.omochice.pinosu.feature.comments.repository.RelayCommentRepository
import io.github.omochice.pinosu.feature.comments.usecase.GetCommentsForBookmarkUseCase
import io.github.omochice.pinosu.feature.comments.usecase.GetCommentsForBookmarkUseCaseImpl

/**
 * Hilt module for Comment feature dependency injection
 *
 * Binds Comment-related interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommentModule {

  @Binds abstract fun bindCommentRepository(impl: RelayCommentRepository): CommentRepository

  @Binds
  abstract fun bindGetCommentsForBookmarkUseCase(
      impl: GetCommentsForBookmarkUseCaseImpl
  ): GetCommentsForBookmarkUseCase
}
