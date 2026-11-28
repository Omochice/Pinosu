package io.github.omochice.pinosu.feature.feed.data.http

import java.io.IOException
import okhttp3.CacheControl

/**
 * Answers from the HTTP cache, however stale the cached document is, when a download fails on the
 * network
 *
 * A document with nothing usable cached still fails with the network error, so the caller sees why
 * the download failed rather than the cache's own refusal.
 *
 * @param fetcher Fetcher whose client holds the cache
 */
class StaleCacheFallbackFetcher(private val fetcher: HttpDocumentFetcher) : DocumentFetcher {

  /**
   * This wraps the fetcher rather than being an OkHttp interceptor, because an interceptor returns
   * before the body is read and so misses a connection that drops during the download.
   */
  override suspend fun fetch(url: String, cacheControl: CacheControl?): Result<FetchedDocument> {
    val downloaded = fetcher.fetch(url, cacheControl)
    return if (downloaded.exceptionOrNull() is IOException) {
      fetcher.fetch(url, CacheControl.FORCE_CACHE).takeIf { it.isSuccess } ?: downloaded
    } else {
      downloaded
    }
  }
}
