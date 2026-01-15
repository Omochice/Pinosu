package io.github.omochice.pinosu.data.relay

import io.github.omochice.pinosu.data.model.NostrEvent

/**
 * Parser for NIP-65 relay list events (kind:10002)
 *
 * Extracts relay configurations from NIP-65 relay list events. NIP-65 defines the format for relay
 * list metadata where users publish their preferred relays.
 *
 * Tag format:
 * - `["r", "wss://relay.com"]` - read and write relay
 * - `["r", "wss://relay.com", "read"]` - read-only relay
 * - `["r", "wss://relay.com", "write"]` - write-only relay
 */
object Nip65RelayListParser {

  const val KIND_RELAY_LIST = 10002

  /**
   * Parse relay list from a NIP-65 event
   *
   * @param event NostrEvent with kind:10002
   * @return List of RelayConfig extracted from the event's r tags
   */
  fun parseRelayListFromEvent(event: NostrEvent): List<RelayConfig> {
    return event.tags
        .filter { it.size >= 2 && it[0] == "r" }
        .mapNotNull { tag ->
          val url = tag[1]
          if (!isValidRelayUrl(url)) return@mapNotNull null

          val marker = tag.getOrNull(2)
          val (read, write) =
              when (marker) {
                "read" -> true to false
                "write" -> false to true
                else -> true to true
              }
          RelayConfig(url = url, read = read, write = write)
        }
  }

  /**
   * Validate relay URL
   *
   * @param url URL to validate
   * @return true if URL is a valid WebSocket URL (wss:// or ws://)
   */
  private fun isValidRelayUrl(url: String): Boolean {
    return url.startsWith("wss://") || url.startsWith("ws://")
  }
}
