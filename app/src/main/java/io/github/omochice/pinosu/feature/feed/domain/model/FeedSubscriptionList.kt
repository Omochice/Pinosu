package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * The RSS and Atom feeds a user subscribes to, as stored in one published list event
 *
 * @property feedUrls Subscribed feed URLs in their stored order, without duplicates
 * @property createdAt Unix timestamp in seconds of the list event
 */
data class FeedSubscriptionList(val feedUrls: List<String>, val createdAt: Long)
