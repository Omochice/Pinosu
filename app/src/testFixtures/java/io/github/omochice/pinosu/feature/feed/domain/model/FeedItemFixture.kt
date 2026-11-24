package io.github.omochice.pinosu.feature.feed.domain.model

import java.time.Instant

/** A [FeedItem] of a feed at `https://a.example/feed`, for tests to override what they check */
fun feedItem(
    id: String = "1",
    feedUrl: String = "https://a.example/feed",
    feedTitle: String = "A",
    title: String = "First",
    link: String? = "https://a.example/1",
    publishedAt: Instant? = null,
    summary: String = "Summary",
    contentHtml: String? = null,
    feedCategory: String? = null,
    imageUrl: String? = null,
): FeedItem =
    FeedItem(
        id = id,
        feedUrl = feedUrl,
        feedTitle = feedTitle,
        title = title,
        link = link,
        publishedAt = publishedAt,
        summary = summary,
        contentHtml = contentHtml,
        feedCategory = feedCategory,
        imageUrl = imageUrl,
    )
