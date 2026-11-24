package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Merge the items of several feeds newest first
 *
 * Items published at the same instant keep their input order, and undated items follow every dated
 * item in their input order.
 *
 * @param itemsPerFeed Items of each feed, in subscription order
 */
internal fun mergeNewestFirst(itemsPerFeed: List<List<FeedItem>>): List<FeedItem> {
  val (dated, undated) = itemsPerFeed.flatten().partition { it.publishedAt != null }
  return dated.sortedByDescending { it.publishedAt } + undated
}
