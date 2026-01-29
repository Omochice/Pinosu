package io.github.omochice.pinosu.feature.auth.data.local

import io.github.omochice.pinosu.core.relay.RelayConfig
import kotlinx.serialization.Serializable

/**
 * Data model for authentication data stored in DataStore
 *
 * @property userPubkey User's Nostr public key (64 hex characters)
 * @property createdAt Timestamp when login was created
 * @property lastAccessed Timestamp when data was last accessed
 * @property relayList List of relay configurations
 */
@Serializable
data class AuthData(
    val userPubkey: String? = null,
    val createdAt: Long = 0L,
    val lastAccessed: Long = 0L,
    val relayList: List<RelayConfig>? = null
) {
  companion object {
    val DEFAULT = AuthData()
  }
}
