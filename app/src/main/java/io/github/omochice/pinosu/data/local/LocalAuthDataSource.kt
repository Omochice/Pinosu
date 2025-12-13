package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError

/**
 * ローカル認証データのデータソース
 *
 * EncryptedSharedPreferencesを使用して、ユーザーの公開鍵を安全に保存・取得する。 Android
 * Keystoreを使用したハードウェア支援の暗号化により、TEE/SEレベルのセキュリティを提供。
 *
 * Task 3.1: EncryptedSharedPreferencesの初期化処理 Task 3.2: ユーザーデータの保存・取得・削除機能 Requirements: 1.4, 2.1,
 * 2.2, 2.5, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3
 */
class LocalAuthDataSource(context: Context) {

  private val sharedPreferences: SharedPreferences

  init {
    // Task 3.1: Android Keystore経由のMasterKey生成（AES256_GCM）
    // Requirement 3.2: Android Keystoreを使用したハードウェア支援の暗号化
    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    // Task 3.1: AES256-SIVキー暗号化、AES256-GCM値暗号化の設定
    // Requirement 3.3: キーと値の両方を暗号化（AES256-SIV/AES256-GCM）
    sharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            "pinosu_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
  }

  /**
   * ユーザー情報を保存する
   *
   * Task 3.2: saveUser実装 Requirement 1.4: ログイン状態の永続化
   *
   * @param user 保存するユーザー
   * @throws StorageError.WriteError 保存に失敗した場合
   */
  suspend fun saveUser(user: User) {
    try {
      val currentTime = System.currentTimeMillis()
      sharedPreferences
          .edit()
          .putString(KEY_USER_PUBKEY, user.pubkey)
          .putLong(KEY_CREATED_AT, currentTime)
          .putLong(KEY_LAST_ACCESSED, currentTime)
          .apply()
    } catch (e: Exception) {
      throw StorageError.WriteError("Failed to save user: ${e.message}")
    }
  }

  /**
   * 保存されたユーザー情報を取得する
   *
   * Task 3.2: getUser実装 Requirement 2.1: ローカルストレージからの状態復元
   *
   * @return 保存されたユーザー、存在しないまたは無効な場合はnull
   */
  suspend fun getUser(): User? {
    return try {
      val pubkey = sharedPreferences.getString(KEY_USER_PUBKEY, null) ?: return null

      // Task 3.2: 検証ロジック - pubkeyのフォーマット検証
      if (!pubkey.matches(Regex("^[0-9a-f]{64}$"))) {
        return null
      }

      // Task 3.2: last_accessedタイムスタンプの更新
      sharedPreferences.edit().putLong(KEY_LAST_ACCESSED, System.currentTimeMillis()).apply()

      User(pubkey)
    } catch (e: Exception) {
      // 不正なデータや復号化エラーの場合はnullを返す
      null
    }
  }

  /**
   * ログイン状態をクリアする
   *
   * Task 3.2: clearLoginState実装 Requirement 2.2: ログアウト機能
   *
   * @throws StorageError.WriteError クリアに失敗した場合
   */
  suspend fun clearLoginState() {
    try {
      sharedPreferences
          .edit()
          .remove(KEY_USER_PUBKEY)
          .remove(KEY_CREATED_AT)
          .remove(KEY_LAST_ACCESSED)
          .apply()
    } catch (e: Exception) {
      throw StorageError.WriteError("Failed to clear login state: ${e.message}")
    }
  }

  companion object {
    private const val KEY_USER_PUBKEY = "user_pubkey"
    private const val KEY_CREATED_AT = "login_created_at"
    private const val KEY_LAST_ACCESSED = "login_last_accessed"
  }
}
