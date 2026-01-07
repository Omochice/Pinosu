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

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds abstract fun bindAuthRepository(impl: AmberAuthRepository): AuthRepository

  @Binds abstract fun bindBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository

  @Binds abstract fun bindUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher
}
