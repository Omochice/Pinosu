package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * A subscribed feed that could not be fetched
 *
 * @property feedUrl URL of the feed, as subscribed
 * @property error Why the feed could not be fetched
 * @property feedCategory Category the user filed the feed under, or null when the feed is in none
 */
data class FeedFetchFailure(
    val feedUrl: String,
    val error: Throwable,
    val feedCategory: String? = null,
)
