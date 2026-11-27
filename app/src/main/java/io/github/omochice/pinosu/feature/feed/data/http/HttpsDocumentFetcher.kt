package io.github.omochice.pinosu.feature.feed.data.http

import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import okhttp3.CacheControl

/**
 * Downloads documents over HTTPS only
 *
 * The app does not allow cleartext traffic, so an `http://` URL is fetched from its `https://`
 * counterpart, and a URL without a scheme is taken as `https://`.
 *
 * @param fetcher Fetcher the HTTPS URL is downloaded with
 */
class HttpsDocumentFetcher(private val fetcher: DocumentFetcher) {

  /**
   * Download the document at [url] over HTTPS
   *
   * @param url Absolute URL, or a URL without a scheme; surrounding whitespace is ignored
   * @param cacheControl Cache directives sent with the request, or null to send none
   * @return The document, whose [FetchedDocument.requestedUrl] is the HTTPS URL actually requested,
   *   or a failure with [HttpFeedNotSupportedException] when [url] is `http://` and its HTTPS
   *   counterpart cannot be fetched, or the failure of [DocumentFetcher.fetch] otherwise
   */
  suspend fun fetch(url: String, cacheControl: CacheControl? = null): Result<FetchedDocument> {
    val trimmed = url.trim()
    return if (HTTP_SCHEME.containsMatchIn(trimmed)) {
      fetchUpgraded(trimmed, cacheControl)
    } else {
      fetcher.fetch(withDefaultScheme(trimmed), cacheControl)
    }
  }

  private suspend fun fetchUpgraded(
      httpUrl: String,
      cacheControl: CacheControl?,
  ): Result<FetchedDocument> {
    val result = fetcher.fetch(httpUrl.replaceFirst(HTTP_SCHEME, HTTPS_PREFIX), cacheControl)
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
