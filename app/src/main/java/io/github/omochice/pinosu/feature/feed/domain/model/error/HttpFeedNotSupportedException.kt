package io.github.omochice.pinosu.feature.feed.domain.model.error

/**
 * The feed is served over plain HTTP only, which the app does not allow
 *
 * @property url The `http://` URL as given
 * @property cause Why the `https://` counterpart of [url] could not be fetched
 */
class HttpFeedNotSupportedException(val url: String, override val cause: Throwable) :
    Exception("HTTP feeds are not supported: $url", cause)
