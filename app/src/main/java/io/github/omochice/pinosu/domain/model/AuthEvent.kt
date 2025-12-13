package io.github.omochice.pinosu.domain.model

/**
 * 認証に関するドメインイベント（将来拡張用）
 *
 * ログイン・ログアウトなどの認証関連イベントを表現するsealed class。 将来的なイベントソーシングやイベント駆動アーキテクチャへの拡張を想定。
 *
 * Task 2.1: ドメインモデルの実装
 */
sealed class AuthEvent {
  /**
   * ユーザーがログインしたイベント
   *
   * @property user ログインしたユーザー
   */
  data class UserLoggedIn(val user: User) : AuthEvent()

  /**
   * ユーザーがログアウトしたイベント
   *
   * data objectとして定義し、シングルトンとして扱う
   */
  data object UserLoggedOut : AuthEvent()
}
