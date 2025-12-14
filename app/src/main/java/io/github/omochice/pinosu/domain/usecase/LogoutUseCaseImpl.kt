package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.AuthRepository

/**
 * LogoutUseCaseの実装
 *
 * AuthRepositoryに委譲してログアウト処理を実行する。 冪等性を保証し、複数回呼び出されても正常に動作する。
 *
 * Task 6.2: LogoutUseCaseの実装 Requirements: 2.4, 2.5
 *
 * @property authRepository 認証リポジトリ
 */
class LogoutUseCaseImpl(private val authRepository: AuthRepository) : LogoutUseCase {

  /**
   * ログアウト処理を実行する
   *
   * AuthRepositoryに委譲してローカルストレージのログイン状態をクリアする。 AuthRepository.logout()が冪等性を保証しているため、
   * このUseCaseも冪等性を継承する。
   *
   * Task 6.2: invoke()実装 Requirement 2.4, 2.5: ログアウト機能と冪等性
   *
   * @return 成功時はSuccess、失敗時はFailure(LogoutError)
   */
  override suspend fun invoke(): Result<Unit> {
    return authRepository.logout()
  }
}
