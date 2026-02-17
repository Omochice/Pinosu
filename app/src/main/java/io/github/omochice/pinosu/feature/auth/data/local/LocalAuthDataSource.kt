package io.github.omochice.pinosu.feature.auth.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Local authentication data data source
 *
 * Uses DataStore with Tink AEAD encryption to securely store and retrieve user's public keys. Data
 * is encrypted using AES256-GCM before being written to disk.
 */
@Singleton
class LocalAuthDataSource @Inject constructor(private val dataStore: DataStore<AuthData>) {

  private var testDataStore: DataStore<AuthData>? = null

  private val activeDataStore: DataStore<AuthData>
    get() = testDataStore ?: dataStore

  /** For testing only - sets DataStore before first access */
  internal fun setTestDataStore(dataStore: DataStore<AuthData>) {
    testDataStore = dataStore
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
      activeDataStore.updateData { current ->
        current.copy(
            userPubkey = user.pubkey.npub,
            createdAt = currentTime,
            lastAccessed = currentTime,
            relayList = current.relayList)
      }
    } catch (e: IOException) {
      throw StorageError.WriteError("Failed to save user: ${e.message}", e)
    } catch (e: IllegalStateException) {
      throw StorageError.WriteError("Failed to save user: ${e.message}", e)
    }
  }

  /**
   * Retrieve saved user information
   *
   * @return Saved user, null if not exists or invalid
   */
  suspend fun getUser(): User? {
    return try {
      val data = activeDataStore.data.first()
      val pubkeyStr = data.userPubkey ?: return null

      val pubkey = Pubkey.parse(pubkeyStr) ?: return null

      activeDataStore.updateData { current ->
        current.copy(lastAccessed = System.currentTimeMillis())
      }

      User(pubkey)
    } catch (e: IOException) {
      Log.w(TAG, "Failed to read user data: ${e.message}")
      null
    } catch (e: IllegalStateException) {
      Log.w(TAG, "Failed to read user data: ${e.message}")
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
      activeDataStore.updateData { current -> current.copy(relayList = relays) }
    } catch (e: IOException) {
      throw StorageError.WriteError("Failed to save relay list: ${e.message}", e)
    } catch (e: IllegalStateException) {
      throw StorageError.WriteError("Failed to save relay list: ${e.message}", e)
    }
  }

  /**
   * Retrieve saved relay list
   *
   * @return Saved relay list, null if not exists
   */
  suspend fun getRelayList(): List<RelayConfig>? {
    return try {
      activeDataStore.data.first().relayList
    } catch (e: IOException) {
      Log.w(TAG, "Failed to read relay list: ${e.message}")
      null
    } catch (e: IllegalStateException) {
      Log.w(TAG, "Failed to read relay list: ${e.message}")
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
      activeDataStore.updateData { AuthData.DEFAULT }
    } catch (e: IOException) {
      throw StorageError.WriteError("Failed to clear login state: ${e.message}", e)
    } catch (e: IllegalStateException) {
      throw StorageError.WriteError("Failed to clear login state: ${e.message}", e)
    }
  }

  companion object {
    private const val TAG = "LocalAuthDataSource"
  }
}
