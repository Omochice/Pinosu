package io.github.omochice.pinosu.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.StorageError
import io.github.omochice.pinosu.domain.model.isValidNostrPubkey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Local authentication data data source
 *
 * Uses DataStore with Tink AEAD encryption to securely store and retrieve user's public keys. Data
 * is encrypted using AES256-GCM before being written to disk.
 */
@Singleton
class LocalAuthDataSource
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<AuthData>
) {

  private val migrationMutex = Mutex()
  private var migrationCompleted = false

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
    ensureMigrated()
    try {
      val currentTime = System.currentTimeMillis()
      activeDataStore.updateData { current ->
        current.copy(
            userPubkey = user.pubkey,
            createdAt = currentTime,
            lastAccessed = currentTime,
            relayList = current.relayList)
      }
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
    ensureMigrated()
    return try {
      val data = activeDataStore.data.first()
      val pubkey = data.userPubkey ?: return null

      if (!pubkey.isValidNostrPubkey()) {
        return null
      }

      activeDataStore.updateData { current ->
        current.copy(lastAccessed = System.currentTimeMillis())
      }

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
    ensureMigrated()
    try {
      activeDataStore.updateData { current -> current.copy(relayList = relays) }
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
    ensureMigrated()
    return try {
      activeDataStore.data.first().relayList
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
    ensureMigrated()
    try {
      activeDataStore.updateData { AuthData.DEFAULT }
    } catch (e: Exception) {
      throw StorageError.WriteError("Failed to clear login state: ${e.message}")
    }
  }

  private suspend fun ensureMigrated() {
    if (migrationCompleted || testDataStore != null) return

    migrationMutex.withLock {
      if (migrationCompleted) return

      try {
        val migration = EncryptedSharedPrefsMigration(context)
        if (migration.needsMigration()) {
          val legacyData = migration.readLegacyData()
          if (legacyData != null) {
            activeDataStore.updateData { legacyData }
          }
          migration.clearLegacyData()
        }
      } catch (e: Exception) {
        // Migration failed, continue with empty data
      }

      migrationCompleted = true
    }
  }
}
