package io.github.omochice.pinosu.feature.feed.data.codec

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.core.nip.nip89.Nip89
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import javax.inject.Inject

/** Converts between [FeedSubscriptionList] and the NIP-78 event that stores it */
class FeedSubscriptionListCodec @Inject constructor() {

  /**
   * Build the list event for the given feeds
   *
   * Blank URLs are dropped and duplicates keep their first position.
   *
   * @param hexPubkey Author's public key (hex-encoded)
   * @param feedUrls Feed URLs in the order to store them
   * @param now Current Unix timestamp in seconds
   * @param previousCreatedAt Unix timestamp in seconds of the list this one replaces, or null when
   *   there is none; the new event is dated after it so that relays keep the new one
   * @param includeClientTag Whether to add the NIP-89 client tag
   * @return Unsigned event ready for NIP-55 signing
   */
  fun encode(
      hexPubkey: String,
      feedUrls: List<String>,
      now: Long,
      previousCreatedAt: Long?,
      includeClientTag: Boolean,
  ): UnsignedNostrEvent {
    val tags = buildList {
      add(listOf(Nip78.Tag.IDENTIFIER, IDENTIFIER))
      normalize(feedUrls).forEach { add(listOf(URL_TAG, it)) }
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
    val urls = event.tags.filter { it.firstOrNull() == URL_TAG }.mapNotNull { it.getOrNull(1) }
    return FeedSubscriptionList(feedUrls = normalize(urls), createdAt = event.createdAt)
  }

  private fun normalize(urls: List<String>): List<String> =
      urls.filter { it.isNotBlank() }.distinct()

  companion object {
    /** `d` tag value that names Pinosu's subscription list among NIP-78 events */
    const val IDENTIFIER = "pinosu/rss-subscriptions"

    /** Tag key holding one subscribed feed URL */
    const val URL_TAG = "url"
  }
}
