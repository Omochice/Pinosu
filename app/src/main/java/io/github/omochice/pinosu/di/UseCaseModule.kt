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
 * UseCasesの依存性注入モジュール
 *
 * Task 7.3: 依存性注入の設定
 * - UseCasesのインターフェースと実装のバインディング
 *
 * @Binds を使用してインターフェースを実装にバインドする
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

  /**
   * LoginUseCaseのバインディング
   *
   * LoginUseCaseImplをLoginUseCaseとしてDIコンテナに登録
   */
  @Binds abstract fun bindLoginUseCase(impl: LoginUseCaseImpl): LoginUseCase

  /**
   * LogoutUseCaseのバインディング
   *
   * LogoutUseCaseImplをLogoutUseCaseとしてDIコンテナに登録
   */
  @Binds abstract fun bindLogoutUseCase(impl: LogoutUseCaseImpl): LogoutUseCase

  /**
   * GetLoginStateUseCaseのバインディング
   *
   * GetLoginStateUseCaseImplをGetLoginStateUseCaseとしてDIコンテナに登録
   */
  @Binds abstract fun bindGetLoginStateUseCase(impl: GetLoginStateUseCaseImpl): GetLoginStateUseCase
}
