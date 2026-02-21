package io.github.omochice.pinosu.feature.comment.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.comment.data.repository.RelayCommentRepository
import io.github.omochice.pinosu.feature.comment.domain.repository.CommentRepository
import io.github.omochice.pinosu.feature.comment.domain.usecase.GetCommentsForBookmarkUseCase
import io.github.omochice.pinosu.feature.comment.domain.usecase.GetCommentsForBookmarkUseCaseImpl
import io.github.omochice.pinosu.feature.comment.domain.usecase.PostCommentUseCase
import io.github.omochice.pinosu.feature.comment.domain.usecase.PostCommentUseCaseImpl

/**
 * Hilt module for Comment feature dependency injection
 *
 * Provides Repository and UseCase bindings for the comment feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommentModule {

  @Provides fun provideCommentRepository(impl: RelayCommentRepository): CommentRepository = impl

  @Provides
  fun provideGetCommentsUseCase(
      impl: GetCommentsForBookmarkUseCaseImpl
  ): GetCommentsForBookmarkUseCase = impl

  @Provides fun providePostCommentUseCase(impl: PostCommentUseCaseImpl): PostCommentUseCase = impl
}
