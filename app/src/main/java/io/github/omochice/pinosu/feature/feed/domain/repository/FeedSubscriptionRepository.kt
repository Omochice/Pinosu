package io.github.omochice.pinosu.feature.feed.domain.repository

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult

/** Repository for the user's RSS and Atom feed subscription list stored on Nostr relays */
interface FeedSubscriptionRepository {

  /**
   * Load the user's subscription list from their own relays
   *
   * @param hexPubkey User's public key (hex-encoded)
   * @return Whether the list was found, confirmed absent, or could not be determined
   */
  suspend fun loadSubscriptionList(hexPubkey: String): FeedSubscriptionLoadResult

  /**
   * Create an unsigned event that replaces the subscription list
   *
   * @param hexPubkey User's public key (hex-encoded)
   * @param feeds Feeds to store, in order
   * @param previous The list being replaced, or null when the user has none yet
   * @return Unsigned event ready for NIP-55 signing
   */
  fun createSubscriptionListEvent(
      hexPubkey: String,
      feeds: List<FeedSubscription>,
      previous: FeedSubscriptionList?,
  ): UnsignedNostrEvent

  /**
   * Publish a signed subscription list event to the user's write relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishSubscriptionList(signedEventJson: String): Result<PublishResult>
}
