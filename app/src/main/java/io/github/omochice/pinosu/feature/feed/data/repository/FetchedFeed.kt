package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.feature.feed.data.http.FetchedDocument
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.domain.model.Feed

/**
 * Parse a downloaded document as the feed known by [feedUrl]
 *
 * Links are resolved against the URL the document was served from, while [Feed.url] and every
 * item's feed URL stay [feedUrl], so that a redirect does not change which subscription the feed
 * belongs to.
 */
internal fun FeedParser.parseFetched(document: FetchedDocument, feedUrl: String): Result<Feed> =
    document
        .openBody()
        .use { parse(it, document.finalUrl) }
        .map { feed ->
          feed.copy(url = feedUrl, items = feed.items.map { it.copy(feedUrl = feedUrl) })
        }
