package io.github.omochice.pinosu.feature.feed.data.codec

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.core.nip.nip89.Nip89
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import javax.inject.Inject

/** Converts between [FeedSubscriptionList] and the NIP-78 event that stores it */
class FeedSubscriptionListCodec @Inject constructor() {

  /**
   * Build the list event for the given feeds
   *
   * Feeds with a blank URL are dropped, and of feeds sharing a URL only the first is kept. A blank
   * category is stored as none.
   *
   * @param hexPubkey Author's public key (hex-encoded)
   * @param feeds Feeds in the order to store them
   * @param now Current Unix timestamp in seconds
   * @param previousCreatedAt Unix timestamp in seconds of the list this one replaces, or null when
   *   there is none; the new event is dated after it so that relays keep the new one
   * @param includeClientTag Whether to add the NIP-89 client tag
   * @return Unsigned event ready for NIP-55 signing
   */
  fun encode(
      hexPubkey: String,
      feeds: List<FeedSubscription>,
      now: Long,
      previousCreatedAt: Long?,
      includeClientTag: Boolean,
  ): UnsignedNostrEvent {
    val tags = buildList {
      add(listOf(Nip78.Tag.IDENTIFIER, IDENTIFIER))
      normalize(feeds).forEach { add(urlTag(it)) }
      if (includeClientTag) add(Nip89.clientTag())
    }
    val createdAt = previousCreatedAt?.let { maxOf(now, it + 1) } ?: now
    return UnsignedNostrEvent(
        pubkey = hexPubkey,
        createdAt = createdAt,
        kind = Nip78.KIND_APP_DATA,
        tags = tags,
        content = "")
  }

  /**
   * Read the list from an event
   *
   * A feed stored without a category, as every feed was before categories existed, or with a blank
   * one is read as having none.
   *
   * @param event Event received from a relay
   * @param hexPubkey Public key (hex-encoded) of the user whose list is expected
   * @return The list, or null when the event is not that user's subscription list
   */
  fun decode(event: NostrEvent, hexPubkey: String): FeedSubscriptionList? {
    val identifier =
        event.tags.firstOrNull { it.firstOrNull() == Nip78.Tag.IDENTIFIER }?.getOrNull(1)
    if (event.pubkey != hexPubkey ||
        event.kind != Nip78.KIND_APP_DATA ||
        identifier != IDENTIFIER) {
      return null
    }
    val feeds =
        event.tags
            .filter { it.firstOrNull() == URL_TAG }
            .mapNotNull { tag -> tag.getOrNull(1)?.let { FeedSubscription(it, tag.getOrNull(2)) } }
    return FeedSubscriptionList(feeds = normalize(feeds), createdAt = event.createdAt)
  }

  private fun normalize(feeds: List<FeedSubscription>): List<FeedSubscription> =
      feeds
          .filter { it.url.isNotBlank() }
          .distinctBy { it.url }
          .map { it.copy(category = FeedSubscription.normalizeCategory(it.category)) }

  private fun urlTag(feed: FeedSubscription): List<String> =
      listOfNotNull(URL_TAG, feed.url, feed.category)

  companion object {
    /** `d` tag value that names Pinosu's subscription list among NIP-78 events */
    const val IDENTIFIER = "pinosu/rss-subscriptions"

    /**
     * Tag key holding one subscribed feed URL, followed by the name of its category when it has one
     */
    const val URL_TAG = "url"
  }
}
