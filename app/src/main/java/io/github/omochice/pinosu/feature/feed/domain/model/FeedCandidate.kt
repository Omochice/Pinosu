package io.github.omochice.pinosu.feature.feed.domain.model

/**
 * A feed that a web page advertises
 *
 * @property url Absolute URL of the feed
 * @property title Title the page gives the feed, or null when it gives none
 */
data class FeedCandidate(val url: String, val title: String?)
