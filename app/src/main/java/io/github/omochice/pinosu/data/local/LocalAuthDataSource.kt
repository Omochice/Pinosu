package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError
import io.github.omochice.pinosu.domain.model.isValidNostrPubkey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local authentication data data source
 *
 * Uses EncryptedSharedPreferences to securely store and retrieve user's public keys.
 */
@Singleton
class LocalAuthDataSource @Inject constructor(@ApplicationContext context: Context) {

  private val sharedPreferences: SharedPreferences

  init {
    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    sharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            "pinosu_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
  }

  /**
   * Save user information
   *
   * @param user User to save
   * @throws StorageError.WriteError when save fails
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
   * Retrieve saved user information
   *
   * @return Saved user, null if not exists or invalid
   */
  suspend fun getUser(): User? {
    return try {
      val pubkey = sharedPreferences.getString(KEY_USER_PUBKEY, null) ?: return null

      if (!pubkey.isValidNostrPubkey()) {
        return null
      }

      sharedPreferences.edit().putLong(KEY_LAST_ACCESSED, System.currentTimeMillis()).apply()

      User(pubkey)
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Clear login state
   *
   * @throws StorageError.WriteError when clear fails
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
