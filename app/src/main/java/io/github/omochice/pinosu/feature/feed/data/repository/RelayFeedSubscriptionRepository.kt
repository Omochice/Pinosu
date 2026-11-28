package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.core.relay.RelayQueryResult
import io.github.omochice.pinosu.core.relay.RelayTermination
import io.github.omochice.pinosu.feature.feed.data.codec.FeedSubscriptionListCodec
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Relay-based implementation of [FeedSubscriptionRepository] that keeps the list on the user's own
 * relays
 *
 * @param relayPool Pool for querying and publishing to Nostr relays
 * @param relayListProvider Provider of the user's own relays
 * @param clientTagRepository Repository for NIP-89 client tag settings
 * @param codec Converter between the list and its event
 * @param clock Source of the current time for dating new list events
 */
@Singleton
class RelayFeedSubscriptionRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val relayListProvider: RelayListProvider,
    private val clientTagRepository: ClientTagRepository,
    private val codec: FeedSubscriptionListCodec,
    private val clock: Clock,
) : FeedSubscriptionRepository {

  override suspend fun loadSubscriptionList(hexPubkey: String): FeedSubscriptionLoadResult {
    val userRelays = relayListProvider.getUserRelays()
    if (userRelays.isEmpty()) {
      return FeedSubscriptionLoadResult.Unavailable(
          FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS)
    }
    val relays = userRelays.filter { it.write }.ifEmpty { userRelays }
    val results =
        relayPool.queryEachRelay(
            relays, listOf(subscriptionListFilter(hexPubkey)), RelayPool.PER_RELAY_TIMEOUT_MS)
    return classify(results, hexPubkey)
  }

  override fun createSubscriptionListEvent(
      hexPubkey: String,
      feedUrls: List<String>,
      previous: FeedSubscriptionList?,
  ): UnsignedNostrEvent =
      codec.encode(
          hexPubkey = hexPubkey,
          feedUrls = feedUrls,
          now = clock.instant().epochSecond,
          previousCreatedAt = previous?.createdAt,
          includeClientTag = clientTagRepository.clientTagEnabledFlow.value)

  override suspend fun publishSubscriptionList(signedEventJson: String): Result<PublishResult> {
    val userRelays = relayListProvider.getUserRelays()
    if (userRelays.isEmpty()) {
      return Result.failure(IllegalStateException("No user relays to publish to"))
    }
    return relayPool.publishEvent(userRelays, signedEventJson, RelayPool.PER_RELAY_TIMEOUT_MS)
  }

  /**
   * Relays that refused, failed, or timed out do not prevent [FeedSubscriptionLoadResult.Absent],
   * because otherwise a single relay requiring AUTH would block a new list forever.
   */
  private fun classify(
      results: List<RelayQueryResult>,
      hexPubkey: String,
  ): FeedSubscriptionLoadResult {
    val newest =
        results
            .flatMap { it.events }
            .mapNotNull { codec.decode(it, hexPubkey) }
            .maxByOrNull { it.createdAt }
    val terminations = results.map { it.termination }
    return when {
      newest != null -> FeedSubscriptionLoadResult.Found(newest)
      terminations.any { it is RelayTermination.Eose } -> FeedSubscriptionLoadResult.Absent
      terminations.all { it is RelayTermination.Closed && it.isAccessDenied } ->
          FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED)
      else -> FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.UNREACHABLE)
    }
  }

  private fun subscriptionListFilter(hexPubkey: String): String =
      """{"kinds":[${Nip78.KIND_APP_DATA}],"authors":["$hexPubkey"],""" +
          """"#${Nip78.Tag.IDENTIFIER}":["${FeedSubscriptionListCodec.IDENTIFIER}"]}"""
}
