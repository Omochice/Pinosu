package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * The RSS and Atom feeds a user subscribes to, as stored in one published list event
 *
 * @property feeds Subscribed feeds in their stored order, without duplicate URLs
 * @property createdAt Unix timestamp in seconds of the list event
 */
data class FeedSubscriptionList(val feeds: List<FeedSubscription>, val createdAt: Long)
