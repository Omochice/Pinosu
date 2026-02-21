package io.github.omochice.pinosu.feature.auth.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.auth.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCaseImpl
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LogoutUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCaseImpl

/**
 * Hilt module for Auth feature dependency injection.
 *
 * Provides Auth-related repositories and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

  @Provides fun provideAuthRepository(impl: Nip55AuthRepository): AuthRepository = impl

  @Provides fun provideLoginUseCase(impl: Nip55LoginUseCase): LoginUseCase = impl

  @Provides fun provideLogoutUseCase(impl: Nip55LogoutUseCase): LogoutUseCase = impl

  @Provides
  fun provideGetLoginStateUseCase(impl: Nip55GetLoginStateUseCase): GetLoginStateUseCase = impl

  @Provides
  fun provideFetchRelayListUseCase(impl: FetchRelayListUseCaseImpl): FetchRelayListUseCase = impl

  @Provides
  fun provideReadOnlyLoginUseCase(impl: ReadOnlyLoginUseCaseImpl): ReadOnlyLoginUseCase = impl
}
