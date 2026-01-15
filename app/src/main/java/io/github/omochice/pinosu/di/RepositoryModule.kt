package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.relay.RelayPool
import io.github.omochice.pinosu.data.relay.RelayPoolImpl
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.data.repository.RelayBookmarkRepository
import io.github.omochice.pinosu.data.repository.RelayListRepository
import io.github.omochice.pinosu.data.repository.RelayListRepositoryImpl

/**
 * Hilt module for Repository dependency injection
 *
 * Binds Repository interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds abstract fun bindAuthRepository(impl: Nip55AuthRepository): AuthRepository

  @Binds abstract fun bindBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository

  @Binds abstract fun bindUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher

  @Binds abstract fun bindRelayPool(impl: RelayPoolImpl): RelayPool

  @Binds abstract fun bindRelayListRepository(impl: RelayListRepositoryImpl): RelayListRepository
}
