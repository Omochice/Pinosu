package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * An RSS or Atom feed as published at one point in time
 *
 * @property url URL the feed document was fetched from
 * @property title Feed title, or an empty string when the feed has none
 * @property siteUrl URL of the website the feed belongs to, or null when the feed does not name one
 * @property items Items in document order
 */
data class Feed(
    val url: String,
    val title: String,
    val siteUrl: String?,
    val items: List<FeedItem>,
)
