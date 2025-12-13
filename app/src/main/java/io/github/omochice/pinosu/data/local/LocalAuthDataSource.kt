package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * ローカル認証データのデータソース
 *
 * EncryptedSharedPreferencesを使用して、ユーザーの公開鍵を安全に保存・取得する。 Android
 * Keystoreを使用したハードウェア支援の暗号化により、TEE/SEレベルのセキュリティを提供。
 *
 * Task 3.1: EncryptedSharedPreferencesの初期化処理 Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3
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
}
