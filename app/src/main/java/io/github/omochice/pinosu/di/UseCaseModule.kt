package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.domain.usecase.AmberGetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLoginUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLogoutUseCase
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase

/**
 * Hilt module for UseCase dependency injection
 *
 * Binds UseCase interfaces to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

  @Binds abstract fun bindLoginUseCase(impl: AmberLoginUseCase): LoginUseCase

  @Binds abstract fun bindLogoutUseCase(impl: AmberLogoutUseCase): LogoutUseCase

  @Binds
  abstract fun bindGetLoginStateUseCase(impl: AmberGetLoginStateUseCase): GetLoginStateUseCase

  @Binds
  abstract fun bindGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase
}
