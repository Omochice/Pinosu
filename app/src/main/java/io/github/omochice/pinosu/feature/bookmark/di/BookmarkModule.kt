package io.github.omochice.pinosu.feature.bookmark.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.bookmark.data.repository.RelayBookmarkRepository
import io.github.omochice.pinosu.feature.bookmark.domain.repository.BookmarkRepository
import io.github.omochice.pinosu.feature.bookmark.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.feature.bookmark.domain.usecase.GetBookmarkListUseCaseImpl

/**
 * Hilt module for Bookmark feature dependency injection
 *
 * Provides Repository, UseCase, and other dependencies for the bookmark feature.
 */
@Module
@InstallIn(SingletonComponent::class)
interface BookmarkModule {

  @Binds fun bindBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository

  @Binds fun bindGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase
}
