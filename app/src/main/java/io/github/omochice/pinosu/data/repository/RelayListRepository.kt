package io.github.omochice.pinosu.data.repository

import android.util.Log
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.relay.Nip65RelayListParser
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.relay.RelayPool
import io.github.omochice.pinosu.data.util.Bech32
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Repository for fetching and caching user relay lists via NIP-65
 *
 * Fetches kind:10002 relay list events from default relays, parses them, and caches the result
 * locally for use when fetching other data.
 *
 * @property relayPool Pool for querying relays
 * @property localAuthDataSource Local storage for caching relay lists
 */
interface RelayListRepository {

  /**
   * Fetch user's relay list from default relays and cache locally
   *
   * @param pubkey User's public key (Bech32 npub format)
   * @return Success with relay list, or Failure on error
   */
  suspend fun fetchAndCacheUserRelays(pubkey: String): Result<List<RelayConfig>>
}

/**
 * Implementation of RelayListRepository using NIP-65 relay list events
 *
 * @property relayPool Pool for querying relays
 * @property localAuthDataSource Local storage for caching relay lists
 */
@Singleton
class RelayListRepositoryImpl
@Inject
constructor(
    private val relayPool: RelayPool,
    private val localAuthDataSource: LocalAuthDataSource
) : RelayListRepository {

  companion object {
    private const val TAG = "RelayListRepository"
    private const val DEFAULT_RELAY_URL = "wss://yabu.me"
    private const val TIMEOUT_MS = 10000L
    private const val CONNECTIVITY_TIMEOUT_MS = 3000L
    private const val MAX_CACHED_RELAYS = 5
    private const val MAX_RELAYS_TO_CHECK = 10
  }

  override suspend fun fetchAndCacheUserRelays(pubkey: String): Result<List<RelayConfig>> {
    return try {
      val hexPubkey =
          Bech32.npubToHex(pubkey)
              ?: return Result.failure(IllegalArgumentException("Invalid npub format: $pubkey"))

      Log.d(TAG, "Fetching relay list for pubkey: ${hexPubkey.take(8)}...")

      val defaultRelays = listOf(RelayConfig(url = DEFAULT_RELAY_URL))
      val filter =
          """{"kinds":[${Nip65RelayListParser.KIND_RELAY_LIST}],"authors":["$hexPubkey"],"limit":1}"""

      val events = relayPool.subscribeWithTimeout(defaultRelays, filter, TIMEOUT_MS)

      if (events.isEmpty()) {
        Log.d(TAG, "No relay list found for user")
        return Result.success(emptyList())
      }

      val mostRecentEvent = events.maxByOrNull { it.createdAt }
      if (mostRecentEvent == null) {
        Log.d(TAG, "No valid event found")
        return Result.success(emptyList())
      }

      val relays = Nip65RelayListParser.parseRelayListFromEvent(mostRecentEvent)
      Log.d(TAG, "Parsed ${relays.size} relays from NIP-65 event")

      if (relays.isEmpty()) {
        return Result.success(emptyList())
      }

      val connectableRelays = filterConnectableRelays(relays)
      Log.d(TAG, "Filtered to ${connectableRelays.size} connectable relays")

      if (connectableRelays.isNotEmpty()) {
        localAuthDataSource.saveRelayList(connectableRelays)
        Log.d(TAG, "Cached ${connectableRelays.size} relays locally")
      }

      Result.success(connectableRelays)
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching relay list", e)
      Result.failure(e)
    }
  }

  /**
   * Filter relays by connectivity, prioritizing read+write relays
   *
   * @param relays List of relay configurations to filter
   * @return List of connectable relays, max [MAX_CACHED_RELAYS]
   */
  private suspend fun filterConnectableRelays(relays: List<RelayConfig>): List<RelayConfig> {
    val sortedRelays =
        relays.sortedByDescending { relay -> if (relay.read && relay.write) 1 else 0 }

    val relaysToCheck = sortedRelays.take(MAX_RELAYS_TO_CHECK)

    val connectableRelays = coroutineScope {
      val deferreds =
          relaysToCheck.map { relay ->
            async {
              val isConnectable =
                  relayPool.checkRelayConnectivity(relay.url, CONNECTIVITY_TIMEOUT_MS)
              if (isConnectable) relay else null
            }
          }
      deferreds.awaitAll().filterNotNull()
    }

    return connectableRelays.take(MAX_CACHED_RELAYS)
  }
}
