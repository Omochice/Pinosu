package io.github.omochice.pinosu.data.model

import java.security.MessageDigest

/**
 * Unsigned Nostr event data model for NIP-55 signing
 *
 * Represents an unsigned Nostr protocol event that needs to be signed by an external signer
 * (NIP-55). The event ID is calculated according to NIP-01 specification.
 *
 * @property pubkey Author's public key (32-byte hex-encoded)
 * @property createdAt Unix timestamp in seconds
 * @property kind Event kind number (e.g., 39701 for bookmark lists)
 * @property tags List of tag arrays (e.g., [["d", "url"], ["r", "https://..."]])
 * @property content Event content
 */
data class UnsignedNostrEvent(
    val pubkey: String,
    val createdAt: Long,
    val kind: Int,
    val tags: List<List<String>>,
    val content: String
) {

  /**
   * Calculate NIP-01 compliant event ID
   *
   * The ID is the SHA256 hash of the serialized event in the format:
   * [0, pubkey, created_at, kind, tags, content]
   *
   * @return 64-character lowercase hex string
   */
  fun calculateId(): String {
    val json = toJsonForSigning()
    return MessageDigest.getInstance("SHA-256")
        .digest(json.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
  }

  /**
   * Serialize event to NIP-01 format for ID calculation
   *
   * Format: [0, pubkey, created_at, kind, tags, content]
   *
   * @return JSON array string for hashing
   */
  fun toJsonForSigning(): String {
    val tagsJson = serializeTags()
    val escapedContent = escapeJsonString(content)
    return """[0,"$pubkey",$createdAt,$kind,$tagsJson,"$escapedContent"]"""
  }

  /**
   * Create signed event JSON with the provided signature
   *
   * @param signature Hex-encoded signature from NIP-55 signer
   * @return Complete signed event JSON
   */
  fun toSignedEventJson(signature: String): String {
    val id = calculateId()
    val tagsJson = serializeTags()
    val escapedContent = escapeJsonString(content)
    return """{"id":"$id","pubkey":"$pubkey","created_at":$createdAt,"kind":$kind,"tags":$tagsJson,"content":"$escapedContent","sig":"$signature"}"""
  }

  /**
   * Serialize tags list to JSON array format
   *
   * @return JSON array string (e.g., [["d","url"],["r","https://..."]])
   */
  private fun serializeTags(): String {
    if (tags.isEmpty()) return "[]"
    return tags.joinToString(",", "[", "]") { tag ->
      tag.joinToString(",", "[", "]") { "\"${escapeJsonString(it)}\"" }
    }
  }

  /**
   * Escape special characters for JSON string
   *
   * @param value String to escape
   * @return Escaped string safe for JSON
   */
  private fun escapeJsonString(value: String): String {
    return value.replace("\\", "\\\\").replace("\"", "\\\"")
  }
}
