package io.github.omochice.pinosu.feature.feed.domain.model.error

/**
 * The URL is a web page that neither is a feed nor advertises one
 *
 * @property pageUrl URL of the page, after redirects
 */
class NoFeedFoundException(val pageUrl: String) : Exception("No feed found at $pageUrl")
