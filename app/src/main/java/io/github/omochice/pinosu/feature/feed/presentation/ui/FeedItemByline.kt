package io.github.omochice.pinosu.feature.feed.presentation.ui

import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Line naming where and when [item] was published
 *
 * @return The feed title, or the feed URL when the feed has no title, followed by the date when the
 *   item has one
 */
internal fun feedItemByline(item: FeedItem): String =
    listOfNotNull(
            item.feedTitle.ifEmpty { item.feedUrl },
            item.publishedAt?.let { formatTimestamp(it.epochSecond) })
        .joinToString(separator = " · ")
