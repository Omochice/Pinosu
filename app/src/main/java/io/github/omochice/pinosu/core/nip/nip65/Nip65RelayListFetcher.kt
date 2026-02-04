package io.github.omochice.pinosu.core.nip.nip65

import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetcher for NIP-65 relay list metadata from Nostr relays
 *
 * Queries bootstrap relays for kind 10002 events containing the user's preferred relay list.
 */
interface Nip65RelayListFetcher {

  /**
   * Fetch relay list for a given public key from Nostr relays
   *
   * @param hexPubkey 64-character hex-encoded public key
   * @return Success(List<RelayConfig>) with the parsed relay list, or empty list if not found.
   *   Failure if pubkey is invalid or network error occurs.
   */
  suspend fun fetchRelayList(hexPubkey: String): Result<List<RelayConfig>>
}

/**
 * Implementation of [Nip65RelayListFetcher]
 *
 * @param relayPool Pool for querying Nostr relays
 * @param parser Parser for NIP-65 events
 */
@Singleton
class Nip65RelayListFetcherImpl
@Inject
constructor(
    private val relayPool: RelayPool,
    private val parser: Nip65EventParser,
) : Nip65RelayListFetcher {

  override suspend fun fetchRelayList(hexPubkey: String): Result<List<RelayConfig>> {
    if (!isValidHexPubkey(hexPubkey)) {
      return Result.failure(
          IllegalArgumentException("Invalid hex pubkey: must be 64 hex characters"))
    }

    return try {
      val bootstrapRelays = listOf(RelayConfig(url = BOOTSTRAP_RELAY_URL))
      val filter =
          """{"kinds":[${Nip65EventParserImpl.KIND_RELAY_LIST_METADATA}],"authors":["$hexPubkey"],"limit":1}"""

      val events = relayPool.subscribeWithTimeout(bootstrapRelays, filter, RELAY_TIMEOUT_MS)

      if (events.isEmpty()) {
        return Result.success(emptyList())
      }

      val mostRecentEvent =
          events.maxByOrNull { it.createdAt } ?: return Result.success(emptyList())
      val relays = parser.parseRelayListEvent(mostRecentEvent)

      Result.success(relays)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /** Validate that the pubkey is a valid 64-character hex string */
  private fun isValidHexPubkey(pubkey: String): Boolean {
    return pubkey.length == HEX_PUBKEY_LENGTH && pubkey.all { it in HEX_CHARS }
  }

  companion object {
    /** Bootstrap relay URL used to fetch NIP-65 events */
    const val BOOTSTRAP_RELAY_URL = "wss://yabu.me"

    /** Timeout for relay queries in milliseconds */
    const val RELAY_TIMEOUT_MS = 10000L

    /** Expected length of hex-encoded public key */
    private const val HEX_PUBKEY_LENGTH = 64

    /** Valid hex characters */
    private val HEX_CHARS = "0123456789abcdefABCDEF".toSet()
  }
}
