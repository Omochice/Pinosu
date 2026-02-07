package io.github.omochice.pinosu.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Nostr event data model
 *
 * Represents a Nostr protocol event as defined in NIP-01.
 *
 * @property id Event ID (32-byte hex-encoded SHA256 hash)
 * @property pubkey Author's public key (32-byte hex-encoded)
 * @property createdAt Unix timestamp in seconds
 * @property kind Event kind number (e.g., 39701 for bookmark lists)
 * @property tags List of tag arrays (e.g., [["r", "url"], ["title", "..."]])
 * @property content Event content (may be encrypted for private events)
 * @property sig Event signature (64-byte hex-encoded Schnorr signature)
 */
@Serializable
data class NostrEvent(
    val id: String,
    val pubkey: String,
    @SerialName("created_at") val createdAt: Long,
    val kind: Int,
    val tags: List<List<String>>,
    val content: String,
    val sig: String? = null,
)
