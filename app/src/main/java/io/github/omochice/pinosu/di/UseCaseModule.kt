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
 * Use case dependency injection module
 * - Binding use case interfaces to their implementations
 *
 * Uses @Binds to bind interfaces to implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

  /**
   * LoginUseCase binding
   *
   * Registers AmberLoginUseCase as LoginUseCase in the DI container
   */
  @Binds abstract fun bindLoginUseCase(impl: AmberLoginUseCase): LoginUseCase

  /**
   * LogoutUseCase binding
   *
   * Registers AmberLogoutUseCase as LogoutUseCase in the DI container
   */
  @Binds abstract fun bindLogoutUseCase(impl: AmberLogoutUseCase): LogoutUseCase

  /**
   * GetLoginStateUseCase binding
   *
   * Registers AmberGetLoginStateUseCase as GetLoginStateUseCase in the DI container
   */
  @Binds
  abstract fun bindGetLoginStateUseCase(impl: AmberGetLoginStateUseCase): GetLoginStateUseCase

  /**
   * GetBookmarkListUseCase binding
   *
   * Registers GetBookmarkListUseCaseImpl as GetBookmarkListUseCase in the DI container
   */
  @Binds
  abstract fun bindGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase
}
