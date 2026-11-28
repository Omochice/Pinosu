package io.github.omochice.pinosu.feature.feed.domain.model

import java.time.Instant

/**
 * One entry of a [Feed]
 *
 * @property id Identifier that stays the same across fetches of the feed; unique within the feed,
 *   not across feeds
 * @property feedUrl URL of the feed this item belongs to
 * @property feedTitle Title of the feed this item belongs to
 * @property title Item title, or an empty string when the item has none
 * @property link Absolute URL of the item's web page, or null when the item has none
 * @property publishedAt When the item was published, or null when the date is missing or unreadable
 * @property summary Item summary as plain text, or an empty string when the item has none
 */
data class FeedItem(
    val id: String,
    val feedUrl: String,
    val feedTitle: String,
    val title: String,
    val link: String?,
    val publishedAt: Instant?,
    val summary: String,
)
