package io.github.omochice.pinosu.core.nip.nip65

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.relay.BootstrapRelayProvider
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
 * @param bootstrapRelayProvider Provider for bootstrap relay configurations
 */
@Singleton
class Nip65RelayListFetcherImpl
@Inject
constructor(
    private val relayPool: RelayPool,
    private val parser: Nip65EventParser,
    private val bootstrapRelayProvider: BootstrapRelayProvider,
) : Nip65RelayListFetcher {

  override suspend fun fetchRelayList(hexPubkey: String): Result<List<RelayConfig>> {
    if (!isValidHexPubkey(hexPubkey)) {
      return Result.failure(
          IllegalArgumentException("Invalid hex pubkey: must be 64 hex characters"))
    }

    return try {
      val bootstrapRelays = bootstrapRelayProvider.getBootstrapRelays()
      val filter =
          """{"kinds":[${Nip65EventParserImpl.KIND_RELAY_LIST_METADATA}],"authors":["$hexPubkey"],"limit":1}"""

      val relays = fetchFirstNonEmpty(bootstrapRelays, filter)
      Result.success(relays)
    } catch (e: IOException) {
      Result.failure(e)
    } catch (e: IllegalArgumentException) {
      Result.failure(e)
    }
  }

  /**
   * Query all relays in parallel and return the first non-empty result.
   *
   * Each relay is queried individually. The first relay to return a non-empty event list wins, and
   * remaining queries are cancelled. If all relays return empty or fail, returns an empty list.
   */
  private suspend fun fetchFirstNonEmpty(
      relays: List<RelayConfig>,
      filter: String,
  ): List<RelayConfig> {
    if (relays.isEmpty()) return emptyList()

    return coroutineScope {
      val resultChannel = Channel<List<RelayConfig>>(capacity = relays.size)

      val jobs =
          relays.map { relay ->
            async { queryRelay(relay, filter)?.let { resultChannel.send(it) } }
          }

      launch {
        jobs.forEach { it.join() }
        resultChannel.close()
      }

      val result = resultChannel.receiveCatching().getOrNull() ?: emptyList()
      jobs.forEach { it.cancel() }
      result
    }
  }

  /**
   * Query a single relay and parse the response.
   *
   * @return Parsed relay list if the relay returns a non-empty result, null otherwise
   */
  private suspend fun queryRelay(relay: RelayConfig, filter: String): List<RelayConfig>? {
    val events =
        try {
          relayPool.subscribeWithTimeout(listOf(relay), filter, RELAY_TIMEOUT_MS)
        } catch (_: IOException) {
          return null
        }
    return parseEvents(events)
  }

  private fun parseEvents(events: List<NostrEvent>): List<RelayConfig>? {
    if (events.isEmpty()) return null
    val mostRecentEvent = events.maxByOrNull { it.createdAt } ?: return null
    val parsed = parser.parseRelayListEvent(mostRecentEvent)
    return parsed.ifEmpty { null }
  }

  /** Validate that the pubkey is a valid 64-character hex string */
  private fun isValidHexPubkey(pubkey: String): Boolean =
      pubkey.length == HEX_PUBKEY_LENGTH && pubkey.all { it in HEX_CHARS }

  companion object {
    /** Bootstrap relay URL used to fetch NIP-65 events */
    const val BOOTSTRAP_RELAY_URL = "wss://directory.yabu.me/"

    /** Timeout for relay queries in milliseconds */
    const val RELAY_TIMEOUT_MS = 10_000L

    /** Expected length of hex-encoded public key */
    private const val HEX_PUBKEY_LENGTH = 64

    /** Valid hex characters */
    private val HEX_CHARS = "0123456789abcdefABCDEF".toSet()
  }
}
