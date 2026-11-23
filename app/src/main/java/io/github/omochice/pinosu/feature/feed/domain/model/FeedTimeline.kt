package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * Items of every subscribed feed merged into one list
 *
 * @property items Items newest first, followed by undated items in subscription and document order
 * @property failedFeeds Subscribed feeds that could not be fetched, in subscription order
 * @property categories Every category at least one subscribed feed is in, each once, in the order
 *   the subscription list first names them
 */
data class FeedTimeline(
    val items: List<FeedItem>,
    val failedFeeds: List<FeedFetchFailure>,
    val categories: List<String> = emptyList(),
)
