package io.github.omochice.pinosu.feature.feed.data.http

import java.io.IOException
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Answers a request from the client's cache, however stale the cached response is, when the network
 * cannot be reached
 *
 * A request with nothing usable cached still fails with the network error, so the caller sees why
 * the download failed rather than the cache's own refusal.
 */
class StaleCacheOnNetworkFailure : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response =
      try {
        chain.proceed(chain.request())
      } catch (networkError: IOException) {
        fromCache(chain, networkError)
      }

  private fun fromCache(chain: Interceptor.Chain, networkError: IOException): Response {
    val cacheOnly = chain.request().newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
    val cached =
        try {
          chain.proceed(cacheOnly)
        } catch (cacheError: IOException) {
          networkError.addSuppressed(cacheError)
          throw networkError
        }
    if (cached.cacheResponse != null) return cached
    cached.close()
    throw networkError
  }
}
