package io.github.omochice.pinosu.feature.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.omochice.pinosu.core.relay.RelayConfig
import kotlinx.serialization.json.Json

/**
 * Handles migration from EncryptedSharedPreferences to DataStore
 *
 * Reads data from the old EncryptedSharedPreferences storage and provides it for migration to
 * DataStore. After successful migration, the old data is deleted.
 */
class EncryptedSharedPrefsMigration(private val context: Context) {

  private val json by lazy { Json { ignoreUnknownKeys = true } }

  private val legacyPrefs: SharedPreferences? by lazy { createLegacyPrefs() }

  /**
   * Check if migration is needed
   *
   * @return true if old EncryptedSharedPreferences contains data
   */
  fun needsMigration(): Boolean {
    return try {
      legacyPrefs?.contains(KEY_USER_PUBKEY) == true
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Read data from old EncryptedSharedPreferences
   *
   * @return AuthData from legacy storage, or null if empty or read fails
   */
  fun readLegacyData(): AuthData? =
      runCatching {
            legacyPrefs?.let { prefs ->
              prefs.getString(KEY_USER_PUBKEY, null)?.let { pubkey ->
                val relayList =
                    prefs.getString(KEY_RELAY_LIST, null)?.let { relayJson ->
                      runCatching { json.decodeFromString<List<RelayConfig>>(relayJson) }
                          .getOrNull()
                    }

                AuthData(
                    userPubkey = pubkey,
                    createdAt = prefs.getLong(KEY_CREATED_AT, 0L),
                    lastAccessed = prefs.getLong(KEY_LAST_ACCESSED, 0L),
                    relayList = relayList)
              }
            }
          }
          .getOrNull()

  /**
   * Delete old EncryptedSharedPreferences data after successful migration
   *
   * Clears all data from the legacy storage.
   */
  fun clearLegacyData() {
    try {
      legacyPrefs?.edit()?.clear()?.apply()
      context.deleteSharedPreferences(PREF_FILE_NAME)
    } catch (_: Exception) {
      // Best effort deletion, ignore errors
    }
  }

  @Suppress("DEPRECATION")
  private fun createLegacyPrefs(): SharedPreferences? {
    return try {
      val masterKey =
          MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
      EncryptedSharedPreferences.create(
          context,
          PREF_FILE_NAME,
          masterKey,
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    } catch (_: Exception) {
      null
    }
  }

  companion object {
    private const val PREF_FILE_NAME = "pinosu_auth_prefs"
    private const val KEY_USER_PUBKEY = "user_pubkey"
    private const val KEY_CREATED_AT = "login_created_at"
    private const val KEY_LAST_ACCESSED = "login_last_accessed"
    private const val KEY_RELAY_LIST = "relay_list"
  }
}
