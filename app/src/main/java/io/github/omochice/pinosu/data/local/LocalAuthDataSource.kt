package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError
import io.github.omochice.pinosu.domain.model.isValidNostrPubkey
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local authentication data data source
 *
 * Uses EncryptedSharedPreferences to securely store and retrieve user's public keys.
 */
@Singleton
class LocalAuthDataSource @Inject constructor(@ApplicationContext private val context: Context) {

  private val sharedPreferences: SharedPreferences by lazy {
    testSharedPreferences ?: createEncryptedSharedPreferences(context)
  }

  private var testSharedPreferences: SharedPreferences? = null

  /** For testing only - sets SharedPreferences before first access */
  internal fun setTestSharedPreferences(prefs: SharedPreferences) {
    testSharedPreferences = prefs
  }

  companion object {
    internal const val KEY_USER_PUBKEY = "user_pubkey"
    internal const val KEY_CREATED_AT = "login_created_at"
    internal const val KEY_LAST_ACCESSED = "login_last_accessed"
    internal const val KEY_RELAY_LIST = "relay_list"

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
      val masterKey =
          MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
      return EncryptedSharedPreferences.create(
          context,
          "pinosu_auth_prefs",
          masterKey,
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
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
   * Save relay list
   *
   * @param relays List of relay configurations to save
   * @throws StorageError.WriteError when save fails
   */
  suspend fun saveRelayList(relays: List<RelayConfig>) {
    try {
      val jsonArray = JSONArray()
      relays.forEach { relay ->
        val jsonObject =
            JSONObject().apply {
              put("url", relay.url)
              put("read", relay.read)
              put("write", relay.write)
            }
        jsonArray.put(jsonObject)
      }
      sharedPreferences.edit().putString(KEY_RELAY_LIST, jsonArray.toString()).apply()
    } catch (e: Exception) {
      throw StorageError.WriteError("Failed to save relay list: ${e.message}")
    }
  }

  /**
   * Retrieve saved relay list
   *
   * @return Saved relay list, null if not exists
   */
  suspend fun getRelayList(): List<RelayConfig>? {
    return try {
      val jsonString = sharedPreferences.getString(KEY_RELAY_LIST, null) ?: return null
      val jsonArray = JSONArray(jsonString)
      val relays = mutableListOf<RelayConfig>()
      for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        relays.add(
            RelayConfig(
                url = jsonObject.getString("url"),
                read = jsonObject.getBoolean("read"),
                write = jsonObject.getBoolean("write")))
      }
      relays
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
          .remove(KEY_RELAY_LIST)
          .apply()
    } catch (e: Exception) {
      throw StorageError.WriteError("Failed to clear login state: ${e.message}")
    }
  }
}
