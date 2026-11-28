package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * Items of every subscribed feed merged into one list
 *
 * @property items Items newest first, followed by undated items in subscription and document order
 * @property failedFeeds Subscribed feeds that could not be fetched, in subscription order
 */
data class FeedTimeline(val items: List<FeedItem>, val failedFeeds: List<FeedFetchFailure>)
