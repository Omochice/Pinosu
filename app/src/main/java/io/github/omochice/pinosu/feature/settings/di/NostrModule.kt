package io.github.omochice.pinosu.feature.settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.feature.settings.data.repository.LocalClientTagRepository
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveBootstrapRelaysUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveBootstrapRelaysUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveClientTagEnabledUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveClientTagEnabledUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetBootstrapRelaysUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetBootstrapRelaysUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetClientTagEnabledUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetClientTagEnabledUseCaseImpl

/**
 * Hilt module for Nostr protocol-related settings.
 *
 * Provides bootstrap relay and NIP-89 client tag dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NostrModule {

  @Binds fun bindSetBootstrapRelays(impl: SetBootstrapRelaysUseCaseImpl): SetBootstrapRelaysUseCase

  @Binds
  fun bindObserveBootstrapRelays(
      impl: ObserveBootstrapRelaysUseCaseImpl
  ): ObserveBootstrapRelaysUseCase

  @Binds fun bindClientTagRepository(impl: LocalClientTagRepository): ClientTagRepository

  @Binds
  fun bindObserveClientTagEnabled(
      impl: ObserveClientTagEnabledUseCaseImpl
  ): ObserveClientTagEnabledUseCase

  @Binds
  fun bindSetClientTagEnabled(impl: SetClientTagEnabledUseCaseImpl): SetClientTagEnabledUseCase
}
