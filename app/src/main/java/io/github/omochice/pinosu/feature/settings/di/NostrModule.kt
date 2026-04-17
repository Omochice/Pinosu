package io.github.omochice.pinosu.feature.settings.di

import dagger.Module
import dagger.Provides
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
object NostrModule {

  @Provides
  fun provideSetBootstrapRelays(impl: SetBootstrapRelaysUseCaseImpl): SetBootstrapRelaysUseCase =
      impl

  @Provides
  fun provideObserveBootstrapRelays(
      impl: ObserveBootstrapRelaysUseCaseImpl
  ): ObserveBootstrapRelaysUseCase = impl

  @Provides
  fun provideClientTagRepository(impl: LocalClientTagRepository): ClientTagRepository = impl

  @Provides
  fun provideObserveClientTagEnabled(
      impl: ObserveClientTagEnabledUseCaseImpl
  ): ObserveClientTagEnabledUseCase = impl

  @Provides
  fun provideSetClientTagEnabled(impl: SetClientTagEnabledUseCaseImpl): SetClientTagEnabledUseCase =
      impl
}
