package io.github.omochice.pinosu.feature.feed.data.http

/**
 * The response body is larger than the app is willing to download
 *
 * @param limitBytes Largest accepted body size in bytes
 */
class ResponseTooLargeException(limitBytes: Long) :
    Exception("Response body exceeds $limitBytes bytes")
