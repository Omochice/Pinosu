package io.github.omochice.pinosu.domain.usecase

/**
 * ログアウト処理のUseCaseインターフェース
 *
 * Task 6.2: LogoutUseCaseの実装
 * - AuthRepositoryへの委譲
 * - 冪等性の保証
 *
 * Requirements: 2.4, 2.5
 */
interface LogoutUseCase {

  /**
   * ログアウト処理を実行する
   *
   * AuthRepositoryに委譲してローカルストレージのログイン状態をクリアする。 冪等性を保証し、すでにログアウト済みでも正常に処理される。
   *
   * Task 6.2: invoke()実装 Requirement 2.4, 2.5: ログアウト機能と冪等性
   *
   * @return 成功時はSuccess、失敗時はFailure(LogoutError)
   */
  suspend operator fun invoke(): Result<Unit>
}
