package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55LoginUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55LogoutUseCase

/**
 * Hilt module for UseCase dependency injection
 *
 * Binds UseCase interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

  @Binds abstract fun bindLoginUseCase(impl: Nip55LoginUseCase): LoginUseCase

  @Binds abstract fun bindLogoutUseCase(impl: Nip55LogoutUseCase): LogoutUseCase

  @Binds
  abstract fun bindGetLoginStateUseCase(impl: Nip55GetLoginStateUseCase): GetLoginStateUseCase

  @Binds
  abstract fun bindGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase
}
