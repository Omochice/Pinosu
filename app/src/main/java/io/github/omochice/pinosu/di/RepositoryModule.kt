package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.repository.AmberAuthRepository
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.data.repository.RelayBookmarkRepository

/**
 * Repository DI module
 *
 * Provides implementation of AuthRepository interface. The @Binds annotation causes Hilt to
 * generate binding code at compile time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  /**
   * Binds AuthRepository implementation
   *
   * AmberAuthRepository has an @Inject constructor, so it can be automatically bound to the
   * interface with @Binds.
   */
  @Binds abstract fun bindAuthRepository(impl: AmberAuthRepository): AuthRepository

  /**
   * Binds BookmarkRepository implementation
   *
   * RelayBookmarkRepository fetches bookmarks from Nostr relay.
   */
  @Binds abstract fun bindBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository

  /**
   * Binds UrlMetadataFetcher implementation
   *
   * OkHttpUrlMetadataFetcher fetches URL metadata (og:title) using OkHttp and Jsoup.
   */
  @Binds abstract fun bindUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher
}
