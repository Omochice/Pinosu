package io.github.omochice.pinosu.feature.settings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.settings.data.repository.LocalSettingsRepository
import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.usecase.GetCurrentLocaleUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.GetCurrentLocaleUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLocaleUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLocaleUseCaseImpl

/**
 * Hilt module for Settings feature dependency injection.
 *
 * Provides Settings-related repositories and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

  @Provides fun provideSettingsRepository(impl: LocalSettingsRepository): SettingsRepository = impl

  @Provides
  fun provideSetDisplayModeUseCase(impl: SetDisplayModeUseCaseImpl): SetDisplayModeUseCase = impl

  @Provides
  fun provideObserveDisplayMode(impl: ObserveDisplayModeUseCaseImpl): ObserveDisplayModeUseCase =
      impl

  @Provides
  fun provideGetCurrentLocaleUseCase(impl: GetCurrentLocaleUseCaseImpl): GetCurrentLocaleUseCase =
      impl

  @Provides fun provideSetLocaleUseCase(impl: SetLocaleUseCaseImpl): SetLocaleUseCase = impl
}
