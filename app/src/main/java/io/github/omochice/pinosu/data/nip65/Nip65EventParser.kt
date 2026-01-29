package io.github.omochice.pinosu.data.nip65

import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.data.model.NostrEvent
import javax.inject.Inject

/**
 * Parser for NIP-65 Relay List Metadata events (kind 10002)
 *
 * NIP-65 defines relay list metadata stored as a replaceable event with "r" tags specifying relay
 * URLs and optional read/write markers.
 */
interface Nip65EventParser {

  /**
   * Parse a NIP-65 relay list event into a list of relay configurations
   *
   * @param event The NostrEvent to parse (expected kind 10002)
   * @return List of RelayConfig extracted from "r" tags. Empty list if event is invalid or has no
   *   valid relays.
   */
  fun parseRelayListEvent(event: NostrEvent): List<RelayConfig>
}

/** Implementation of Nip65EventParser */
class Nip65EventParserImpl @Inject constructor() : Nip65EventParser {

  companion object {
    /** NIP-65 Relay List Metadata event kind */
    const val KIND_RELAY_LIST_METADATA = 10002

    private const val TAG_RELAY = "r"
    private const val MARKER_READ = "read"
    private const val MARKER_WRITE = "write"
  }

  override fun parseRelayListEvent(event: NostrEvent): List<RelayConfig> {
    if (event.kind != KIND_RELAY_LIST_METADATA) {
      return emptyList()
    }

    return event.tags
        .filter { tag -> tag.size >= 2 && tag[0] == TAG_RELAY }
        .mapNotNull { tag -> parseRelayTag(tag) }
  }

  /**
   * Parse a single "r" tag into RelayConfig
   *
   * Tag format: ["r", "<url>", "<optional marker>"] where marker is "read" or "write". No marker
   * means both read and write are true.
   */
  private fun parseRelayTag(tag: List<String>): RelayConfig? {
    val url = tag[1]

    if (!isValidRelayUrl(url)) {
      return null
    }

    val marker = tag.getOrNull(2)

    val (read, write) =
        when (marker) {
          MARKER_READ -> Pair(true, false)
          MARKER_WRITE -> Pair(false, true)
          else -> Pair(true, true)
        }

    return RelayConfig(url = url, read = read, write = write)
  }

  /** Validate relay URL has WebSocket scheme (wss:// or ws://) */
  private fun isValidRelayUrl(url: String): Boolean {
    return url.startsWith("wss://") || url.startsWith("ws://")
  }
}
