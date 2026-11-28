package io.github.omochice.pinosu.feature.comment.di

import dagger.Binds
import dagger.Module
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
interface CommentModule {

  @Binds fun bindCommentRepository(impl: RelayCommentRepository): CommentRepository

  @Binds
  fun bindGetCommentsUseCase(impl: GetCommentsForBookmarkUseCaseImpl): GetCommentsForBookmarkUseCase

  @Binds fun bindPostCommentUseCase(impl: PostCommentUseCaseImpl): PostCommentUseCase
}
