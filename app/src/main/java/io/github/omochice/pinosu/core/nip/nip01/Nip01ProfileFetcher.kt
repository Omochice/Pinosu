package io.github.omochice.pinosu.core.nip.nip01

import io.github.omochice.pinosu.core.model.UserProfile
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Fetcher for NIP-01 user profile metadata from Nostr relays
 *
 * Queries relays for kind 0 events and caches results in memory. Supports batch fetching for
 * multiple pubkeys to minimize relay queries.
 */
interface Nip01ProfileFetcher {

  /**
   * Fetch profiles for a list of public keys
   *
   * Results are cached in memory. Cached profiles are returned immediately; only uncached pubkeys
   * trigger relay queries.
   *
   * @param hexPubkeys List of hex-encoded public keys
   * @return Map of pubkey to UserProfile for successfully fetched profiles
   */
  suspend fun fetchProfiles(hexPubkeys: List<String>): Map<String, UserProfile>
}

/**
 * Implementation of [Nip01ProfileFetcher]
 *
 * @param relayPool Pool for querying Nostr relays
 * @param parser Parser for kind 0 events
 * @param localAuthDataSource Data source for cached relay list
 */
@Singleton
class Nip01ProfileFetcherImpl
@Inject
constructor(
    private val relayPool: RelayPool,
    private val parser: Nip01ProfileParser,
    private val localAuthDataSource: LocalAuthDataSource,
) : Nip01ProfileFetcher {

  private val cache = ConcurrentHashMap<String, UserProfile>()

  override suspend fun fetchProfiles(hexPubkeys: List<String>): Map<String, UserProfile> {
    if (hexPubkeys.isEmpty()) return emptyMap()

    val result = mutableMapOf<String, UserProfile>()
    val uncached = mutableListOf<String>()

    for (pubkey in hexPubkeys.distinct()) {
      val cached = cache[pubkey]
      if (cached != null) {
        result[pubkey] = cached
      } else {
        uncached.add(pubkey)
      }
    }

    if (uncached.isEmpty()) return result

    val relays = getRelaysForQuery()
    val filter =
        Json.encodeToString(
            ProfileFilter(
                kinds = listOf(Nip01ProfileParserImpl.KIND_USER_METADATA), authors = uncached))
    val events = relayPool.subscribeWithTimeout(relays, filter, RELAY_TIMEOUT_MS)

    val profilesByPubkey =
        events
            .groupBy { it.pubkey }
            .mapValues { (_, events) -> events.maxByOrNull { it.createdAt } }
            .mapNotNull { (pubkey, event) ->
              event?.let { parser.parseProfileEvent(it) }?.let { pubkey to it }
            }
            .toMap()

    for ((pubkey, profile) in profilesByPubkey) {
      cache[pubkey] = profile
      result[pubkey] = profile
    }

    return result
  }

  private suspend fun getRelaysForQuery(): List<RelayConfig> {
    val cachedRelays = localAuthDataSource.getRelayList()
    return if (cachedRelays.isNullOrEmpty()) {
      listOf(RelayConfig(url = DEFAULT_RELAY_URL))
    } else {
      cachedRelays
    }
  }

  @Serializable
  private data class ProfileFilter(
      val kinds: List<Int>,
      val authors: List<String>,
  )

  companion object {
    private const val DEFAULT_RELAY_URL = "wss://yabu.me"
    private const val RELAY_TIMEOUT_MS = 10_000L
  }
}
