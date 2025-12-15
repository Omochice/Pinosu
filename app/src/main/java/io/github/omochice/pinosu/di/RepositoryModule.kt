package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.AuthRepositoryImpl

/**
 * リポジトリDIモジュール
 *
 * Task 7.3: 依存性注入の設定 - Repository層のDI設定
 *
 * AuthRepositoryインターフェースの実装を提供する。 @Bindsアノテーションにより、Hiltはコンパイル時にバインディングコードを生成する。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  /**
   * AuthRepositoryの実装をバインド
   *
   * AuthRepositoryImplは@Injectコンストラクタを持つため、 @Bindsで自動的にインターフェースにバインドできる。
   */
  @Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
