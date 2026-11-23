package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Items of the timeline loaded last, kept in memory so the reader can open one of them by its
 * identity
 *
 * Nothing is persisted, so after process death no item is found until the timeline loads again.
 */
@Singleton
class LoadedFeedItems @Inject constructor() {

  @Volatile private var itemsByKey: Map<Pair<String, String>, FeedItem> = emptyMap()

  /**
   * Replace the items with those of a newly loaded timeline
   *
   * @param items Items the timeline shows, unique by feed URL and id
   */
  fun replace(items: List<FeedItem>) {
    itemsByKey = items.associateBy { it.feedUrl to it.id }
  }

  /**
   * Find an item of the timeline loaded last
   *
   * @param feedUrl URL of the feed the item belongs to
   * @param itemId Identifier of the item within its feed
   * @return The item, or null when the timeline loaded last does not hold it
   */
  fun find(feedUrl: String, itemId: String): FeedItem? = itemsByKey[feedUrl to itemId]
}
