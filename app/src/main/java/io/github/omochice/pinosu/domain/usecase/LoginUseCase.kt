package io.github.omochice.pinosu.domain.usecase

/**
 * ログイン処理のUseCaseインターフェース
 *
 * Task 6.1: LoginUseCaseの実装
 * - Amberインストール確認
 * - AuthRepositoryへの委譲
 *
 * Requirements: 1.1, 1.3, 1.4, 1.5, 4.5
 */
interface LoginUseCase {

  /**
   * Amberアプリがインストールされているか確認する
   *
   * AuthRepositoryに委譲してAmberのインストール状態を確認する。
   *
   * Task 6.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   *
   * @return Amberがインストールされている場合true
   */
  fun checkAmberInstalled(): Boolean
}
