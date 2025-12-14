package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.data.amber.AmberError
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.model.error.StorageError

/**
 * AuthRepositoryの実装
 *
 * AmberSignerClientとLocalAuthDataSourceを統合し、 認証フローとローカル状態管理を提供する。
 *
 * Task 5.1: AuthRepositoryの実装 Requirements: 1.3, 1.4, 2.1, 2.2, 2.4, 2.5
 *
 * @property amberSignerClient Amber通信クライアント
 * @property localAuthDataSource ローカルストレージデータソース
 */
class AuthRepositoryImpl(
    private val amberSignerClient: AmberSignerClient,
    private val localAuthDataSource: LocalAuthDataSource
) : AuthRepository {

  /**
   * ログイン状態を取得する
   *
   * LocalAuthDataSourceから保存されたユーザー情報を取得する。
   *
   * Task 5.1: getLoginState()実装 Requirement 2.2: ログイン状態確認
   *
   * @return ログイン済みの場合はUser、未ログインの場合はnull
   */
  override suspend fun getLoginState(): User? {
    return localAuthDataSource.getUser()
  }

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
  override suspend fun saveLoginState(user: User): Result<Unit> {
    return try {
      localAuthDataSource.saveUser(user)
      Result.success(Unit)
    } catch (e: StorageError) {
      Result.failure(e)
    }
  }

  /**
   * ログアウトする
   *
   * LocalAuthDataSourceのログイン状態をクリアする。
   *
   * Task 5.1: logout()実装 Requirement 2.4: ログアウト機能
   *
   * @return 成功時はSuccess、失敗時はFailure(LogoutError)
   */
  override suspend fun logout(): Result<Unit> {
    return try {
      localAuthDataSource.clearLoginState()
      Result.success(Unit)
    } catch (e: StorageError) {
      // StorageErrorをLogoutError.StorageErrorに変換
      Result.failure(LogoutError.StorageError(e.message ?: "Failed to clear login state"))
    }
  }

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
  override suspend fun processAmberResponse(resultCode: Int, data: Intent?): Result<User> {
    // AmberSignerClientでレスポンスを処理
    val amberResult = amberSignerClient.handleAmberResponse(resultCode, data)

    return if (amberResult.isSuccess) {
      // Amber成功時: pubkeyを取得してUserを作成
      val amberResponse = amberResult.getOrNull()!!
      val user = User(amberResponse.pubkey)

      // LocalAuthDataSourceに保存
      try {
        localAuthDataSource.saveUser(user)
        Result.success(user)
      } catch (e: StorageError) {
        // ローカル保存失敗時はUnknownErrorとして返す
        Result.failure(LoginError.UnknownError(e))
      }
    } else {
      // Amber失敗時: AmberErrorをLoginErrorに変換
      val amberError = amberResult.exceptionOrNull() as? AmberError
      val loginError =
          when (amberError) {
            is AmberError.NotInstalled -> LoginError.AmberNotInstalled
            is AmberError.UserRejected -> LoginError.UserRejected
            is AmberError.Timeout -> LoginError.Timeout
            is AmberError.InvalidResponse,
            is AmberError.IntentResolutionError ->
                LoginError.NetworkError(amberError?.toString() ?: "Unknown Amber error")
            null -> LoginError.UnknownError(Exception("Unknown Amber error"))
          }
      Result.failure(loginError)
    }
  }

  /**
   * Amberアプリがインストールされているか確認する
   *
   * AmberSignerClientに委譲してAmberのインストール状態を確認する。
   *
   * Task 5.1: checkAmberInstalled()実装 Requirement 1.2: Amber未インストール検出
   *
   * @return Amberがインストールされている場合true
   */
  override fun checkAmberInstalled(): Boolean {
    return amberSignerClient.checkAmberInstalled()
  }
}
