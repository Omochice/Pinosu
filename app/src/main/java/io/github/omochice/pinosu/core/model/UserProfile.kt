package io.github.omochice.pinosu.core.model

/**
 * Nostr user profile metadata from kind 0 events (NIP-01)
 *
 * @property pubkey Hex-encoded public key of the profile owner
 * @property name Display name
 * @property picture Profile image URL
 * @property about Bio/description
 */
data class UserProfile(
    val pubkey: String,
    val name: String? = null,
    val picture: String? = null,
    val about: String? = null,
)
