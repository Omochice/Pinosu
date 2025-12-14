package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.domain.model.User

/**
 * 認証リポジトリのインターフェース
 *
 * AmberSignerClientとLocalAuthDataSourceを統合し、 認証フローとローカル状態管理を提供する。
 *
 * Task 5.1: AuthRepositoryの実装 Requirements: 1.3, 1.4, 2.1, 2.2, 2.4, 2.5
 */
interface AuthRepository {

  /**
   * ログイン状態を取得する
   *
   * LocalAuthDataSourceから保存されたユーザー情報を取得する。
   *
   * Task 5.1: getLoginState()実装 Requirement 2.2: ログイン状態確認
   *
   * @return ログイン済みの場合はUser、未ログインの場合はnull
   */
  suspend fun getLoginState(): User?

  /**
   * ログイン状態を保存する
   *
   * ユーザー情報をLocalAuthDataSourceに保存する。
   *
   * Task 5.1: saveLoginState()実装 Requirement 1.4: ログイン状態保存
   *
   * @param user 保存するユーザー
   * @return 成功時はSuccess、失敗時はFailure(StorageError)
   */
  suspend fun saveLoginState(user: User): Result<Unit>

  /**
   * ログアウトする
   *
   * LocalAuthDataSourceのログイン状態をクリアする。
   *
   * Task 5.1: logout()実装 Requirement 2.4: ログアウト機能
   *
   * @return 成功時はSuccess、失敗時はFailure(LogoutError)
   */
  suspend fun logout(): Result<Unit>

  /**
   * Amberレスポンスを処理してユーザーをログイン状態にする
   *
   * AmberSignerClientでレスポンスを解析し、成功時にLocalAuthDataSourceに保存する。
   *
   * Task 5.1: processAmberResponse実装 Requirement 1.3, 1.4: Amber認証とローカル保存
   *
   * @param resultCode ActivityResultのresultCode
   * @param data Intentデータ
   * @return 成功時はSuccess(User)、失敗時はFailure(LoginError)
   */
  suspend fun processAmberResponse(resultCode: Int, data: Intent?): Result<User>

  /**
   * Amberアプリがインストールされているか確認する
   *
   * AmberSignerClientに委譲してAmberのインストール状態を確認する。
   *
   * Task 5.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   *
   * @return Amberがインストールされている場合true
   */
  fun checkAmberInstalled(): Boolean
}
