package io.github.omochice.pinosu.feature.bookmark.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.bookmark.data.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.feature.bookmark.data.metadata.UrlMetadataFetcher
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
object BookmarkModule {

  @Provides fun provideBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository = impl

  @Provides fun provideUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher = impl

  @Provides
  fun provideGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase = impl
}
