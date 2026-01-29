package io.github.omochice.pinosu.core.model

import com.vitorpamplona.quartz.nip01Core.crypto.EventHasher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Unsigned Nostr event for NIP-55 signing
 *
 * @property pubkey Author's public key (hex-encoded)
 * @property createdAt Unix timestamp in seconds
 * @property kind Event kind (39701 for bookmarks)
 * @property tags Event tags as list of string lists
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
   * Serialize to JSON string for NIP-55 signing request
   *
   * @return JSON string representation of the unsigned event
   */
  fun toJson(): String {
    val json = buildJsonObject {
      put("pubkey", pubkey)
      put("created_at", createdAt)
      put("kind", kind)
      putJsonArray("tags") { tags.forEach { tag -> add(JsonArray(tag.map { JsonPrimitive(it) })) } }
      put("content", content)
    }
    return Json.encodeToString(json)
  }

  /**
   * Calculate the event ID according to NIP-01
   *
   * Uses quartz library's EventHasher for correct NIP-01 compliant serialization and hashing.
   *
   * @return Event ID as hex string
   */
  fun calculateId(): String {
    val tagsArray = tags.map { it.toTypedArray() }.toTypedArray()
    return EventHasher.hashId(pubkey, createdAt, kind, tagsArray, content)
  }

  /**
   * Create a signed event JSON by adding id and sig to the unsigned event
   *
   * @param signature The signature (hex string) returned from NIP-55 signer
   * @return Complete signed event as JSON string
   */
  fun toSignedJson(signature: String): String {
    val eventId = calculateId()
    val json = buildJsonObject {
      put("id", eventId)
      put("pubkey", pubkey)
      put("created_at", createdAt)
      put("kind", kind)
      putJsonArray("tags") { tags.forEach { tag -> add(JsonArray(tag.map { JsonPrimitive(it) })) } }
      put("content", content)
      put("sig", signature)
    }
    return Json.encodeToString(json)
  }
}
