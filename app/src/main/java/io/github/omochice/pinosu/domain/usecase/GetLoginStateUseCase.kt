package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.User

/**
 * ログイン状態取得のUseCaseインターフェース
 *
 * Task 6.3: GetLoginStateUseCaseの実装
 * - AuthRepositoryへの委譲
 * - 読み取り専用操作の保証
 *
 * Requirements: 2.2, 2.3
 */
interface GetLoginStateUseCase {

  /**
   * ログイン状態を取得する
   *
   * AuthRepositoryに委譲してローカルストレージからユーザー情報を取得する。 読み取り専用操作であり、ログイン状態を変更しない。
   *
   * Task 6.3: invoke()実装 Requirement 2.2, 2.3: ログイン状態確認とメイン画面表示
   *
   * @return ログイン済みの場合はUser、未ログインの場合はnull
   */
  suspend operator fun invoke(): User?
}
