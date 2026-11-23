package io.github.omochice.pinosu.feature.feed.domain.model

import io.github.omochice.pinosu.core.model.UnsignedNostrEvent

/** What preparing a change to the user's feed subscription list led to */
sealed interface FeedSubscriptionChange {

  /**
   * The new list is built and has to be signed before it is published
   *
   * @property event Unsigned list event for NIP-55 signing
   * @property feedUrls Feed URLs the new list holds, in order
   * @property isFirstList Whether the user has published no list before, in which case they must be
   *   told that the list is public before it is published
   */
  data class ReadyToSign(
      val event: UnsignedNostrEvent,
      val feedUrls: List<String>,
      val isFirstList: Boolean,
  ) : FeedSubscriptionChange

  /**
   * The entered URL is a web page that advertises several feeds, so the user has to pick one and
   * add it again
   *
   * @property candidates Advertised feeds in page order
   */
  data class ChooseCandidate(val candidates: List<FeedCandidate>) : FeedSubscriptionChange

  /**
   * The feed to add is already in the list, so nothing changes
   *
   * @property feedUrl URL of the feed
   */
  data class AlreadySubscribed(val feedUrl: String) : FeedSubscriptionChange

  /**
   * The feed to remove is not in the list, so nothing changes
   *
   * @property feedUrl URL of the feed
   */
  data class NotSubscribed(val feedUrl: String) : FeedSubscriptionChange
}
