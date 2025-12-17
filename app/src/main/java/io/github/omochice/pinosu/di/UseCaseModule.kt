package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCaseImpl

/**
 * Use case dependency injection module
 *
 * Task 7.3: Dependency injection configuration
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
   * Registers LoginUseCaseImpl as LoginUseCase in the DI container
   */
  @Binds abstract fun bindLoginUseCase(impl: LoginUseCaseImpl): LoginUseCase

  /**
   * LogoutUseCase binding
   *
   * Registers LogoutUseCaseImpl as LogoutUseCase in the DI container
   */
  @Binds abstract fun bindLogoutUseCase(impl: LogoutUseCaseImpl): LogoutUseCase

  /**
   * GetLoginStateUseCase binding
   *
   * Registers GetLoginStateUseCaseImpl as GetLoginStateUseCase in the DI container
   */
  @Binds abstract fun bindGetLoginStateUseCase(impl: GetLoginStateUseCaseImpl): GetLoginStateUseCase
}
