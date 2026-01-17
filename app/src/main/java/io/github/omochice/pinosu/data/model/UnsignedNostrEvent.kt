package io.github.omochice.pinosu.data.model

import com.vitorpamplona.quartz.nip01Core.crypto.EventHasher
import org.json.JSONArray
import org.json.JSONObject

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
    val json = JSONObject()
    json.put("pubkey", pubkey)
    json.put("created_at", createdAt)
    json.put("kind", kind)
    json.put("content", content)

    val tagsArray = JSONArray()
    for (tag in tags) {
      val tagArray = JSONArray()
      for (element in tag) {
        tagArray.put(element)
      }
      tagsArray.put(tagArray)
    }
    json.put("tags", tagsArray)

    return json.toString()
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

    val json = JSONObject()
    json.put("id", eventId)
    json.put("pubkey", pubkey)
    json.put("created_at", createdAt)
    json.put("kind", kind)
    json.put("content", content)

    val tagsArray = JSONArray()
    for (tag in tags) {
      val tagArray = JSONArray()
      for (element in tag) {
        tagArray.put(element)
      }
      tagsArray.put(tagArray)
    }
    json.put("tags", tagsArray)
    json.put("sig", signature)

    return json.toString()
  }
}
