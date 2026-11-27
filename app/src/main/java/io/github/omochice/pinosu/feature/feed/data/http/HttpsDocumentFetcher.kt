package io.github.omochice.pinosu.feature.feed.data.http

import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException

/**
 * Downloads documents over HTTPS only
 *
 * The app does not allow cleartext traffic, so an `http://` URL is fetched from its `https://`
 * counterpart, and a URL without a scheme is taken as `https://`.
 */
class HttpsDocumentFetcher(private val fetcher: HttpDocumentFetcher) {

  /**
   * Download the document at [url] over HTTPS
   *
   * @param url Absolute URL, or a URL without a scheme; surrounding whitespace is ignored
   * @return The document, whose [FetchedDocument.requestedUrl] is the HTTPS URL actually requested,
   *   or a failure with [HttpFeedNotSupportedException] when [url] is `http://` and its HTTPS
   *   counterpart cannot be fetched, or the failure of [HttpDocumentFetcher.fetch] otherwise
   */
  suspend fun fetch(url: String): Result<FetchedDocument> {
    val trimmed = url.trim()
    return if (HTTP_SCHEME.containsMatchIn(trimmed)) {
      fetchUpgraded(trimmed)
    } else {
      fetcher.fetch(withDefaultScheme(trimmed))
    }
  }

  private suspend fun fetchUpgraded(httpUrl: String): Result<FetchedDocument> {
    val result = fetcher.fetch(httpUrl.replaceFirst(HTTP_SCHEME, HTTPS_PREFIX))
    return result.exceptionOrNull()?.let {
      Result.failure(HttpFeedNotSupportedException(httpUrl, it))
    } ?: result
  }

  private fun withDefaultScheme(url: String): String =
      if (ANY_SCHEME.containsMatchIn(url)) url else HTTPS_PREFIX + url

  companion object {
    private const val HTTPS_PREFIX = "https://"
    private val HTTP_SCHEME = Regex("^http://", RegexOption.IGNORE_CASE)
    private val ANY_SCHEME = Regex("^[a-z][a-z0-9+.-]*://", RegexOption.IGNORE_CASE)
  }
}
