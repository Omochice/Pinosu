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
 * @property contentHtml Fullest body the item carries, as HTML that is safe to render: only text
 *   formatting, tables, and images remain and every link and image source is an absolute HTTP(S)
 *   URL; null when the item has neither body text nor a body image
 * @property feedCategory Category the user filed the feed under, or null when the feed is in none
 * @property imageUrl Absolute HTTP(S) URL of the image the feed gives the item, or null when it
 *   gives none
 */
data class FeedItem(
    val id: String,
    val feedUrl: String,
    val feedTitle: String,
    val title: String,
    val link: String?,
    val publishedAt: Instant?,
    val summary: String,
    val contentHtml: String? = null,
    val feedCategory: String? = null,
    val imageUrl: String? = null,
)
