package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * A subscribed feed that could not be fetched
 *
 * @property feedUrl URL of the feed, as subscribed
 * @property error Why the feed could not be fetched
 */
data class FeedFetchFailure(val feedUrl: String, val error: Throwable)
