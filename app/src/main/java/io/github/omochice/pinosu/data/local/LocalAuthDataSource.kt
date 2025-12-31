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
 * Uses EncryptedSharedPreferences to securely store and retrieve user's public keys. Provides
 * TEE/SE level security through hardware-assisted encryption using Android Keystore.
 *
 * Task 3.1: EncryptedSharedPreferences initialization Task 3.2: User data save/get/delete
 * functionality Requirements: 1.4, 2.1, 2.2, 2.5, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3
 */
@Singleton
class LocalAuthDataSource @Inject constructor(@ApplicationContext context: Context) {

  private val sharedPreferences: SharedPreferences

  init {
    // Task 3.1: MasterKey generation via Android Keystore (AES256_GCM)
    // Requirement 3.2: Hardware-assisted encryption using Android Keystore
    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    // Task 3.1: Configure AES256-SIV key encryption and AES256-GCM value encryption
    // Requirement 3.3: Encrypt both keys and values (AES256-SIV/AES256-GCM)
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
   * Task 3.2: saveUser implementation Requirement 1.4: Login state persistence
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
   * Task 3.2: getUser implementation Requirement 2.1: State restoration from local storage
   *
   * @return Saved user, null if not exists or invalid
   */
  suspend fun getUser(): User? {
    return try {
      val pubkey = sharedPreferences.getString(KEY_USER_PUBKEY, null) ?: return null

      // Task 3.2: Validation logic - pubkey format validation (Bech32-encoded)
      if (!pubkey.isValidNostrPubkey()) {
        return null
      }

      // Task 3.2: Update last_accessed timestamp
      sharedPreferences.edit().putLong(KEY_LAST_ACCESSED, System.currentTimeMillis()).apply()

      User(pubkey)
    } catch (e: Exception) {
      // Return null for invalid data or decryption errors
      null
    }
  }

  /**
   * Clear login state
   *
   * Task 3.2: clearLoginState implementation Requirement 2.2: Logout functionality
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
