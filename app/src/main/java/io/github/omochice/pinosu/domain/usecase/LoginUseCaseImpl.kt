package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository
import javax.inject.Inject

/**
 * LoginUseCaseの実装
 *
 * AuthRepositoryに委譲してログイン処理を実行する。
 *
 * Task 6.1: LoginUseCaseの実装 Requirements: 1.1, 1.3, 1.4, 1.5, 4.5
 *
 * @property authRepository 認証リポジトリ
 */
class LoginUseCaseImpl @Inject constructor(private val authRepository: AuthRepository) :
    LoginUseCase {

  /**
   * Amberアプリがインストールされているか確認する
   *
   * AuthRepositoryに委譲してAmberのインストール状態を確認する。
   *
   * Task 6.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   *
   * @return Amberがインストールされている場合true
   */
  override fun checkAmberInstalled(): Boolean {
    return authRepository.checkAmberInstalled()
  }
}
