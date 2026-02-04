package io.github.omochice.pinosu.feature.bookmark.data.metadata

import android.util.Log
import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

/** Fetches URL metadata (Open Graph tags) */
interface UrlMetadataFetcher {
  suspend fun fetchTitle(url: String): Result<String?>
}

/**
 * Implementation of [UrlMetadataFetcher] using OkHttp and Jsoup
 *
 * @param okHttpClient HTTP client for fetching URL content
 */
@Singleton
class OkHttpUrlMetadataFetcher @Inject constructor(private val okHttpClient: OkHttpClient) :
    UrlMetadataFetcher {

  companion object {
    private const val TAG = "UrlMetadataFetcher"
    private const val MAX_CACHE_SIZE = 100
  }

  // Simple in-memory cache: URL -> Title
  private val cache = LruCache<String, String>(MAX_CACHE_SIZE)

  override suspend fun fetchTitle(url: String): Result<String?> {
    // Check cache first
    cache.get(url)?.let {
      return Result.success(it)
    }

    return withContext(Dispatchers.IO) {
      try {

        val request =
            Request.Builder().url(url).header("User-Agent", "Pinosu/1.0 (Android)").build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
          Log.w(TAG, "HTTP request failed with code: ${response.code}")
          return@withContext Result.failure(Exception("HTTP ${response.code}"))
        }

        val html = response.body.string()

        val title = parseOgTitle(html)

        title?.let { cache.put(url, it) }

        Result.success(title)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to fetch metadata for $url: ${e.message}")
        Result.failure(e)
      }
    }
  }

  /**
   * Parse Open Graph title or HTML title from HTML content
   *
   * Priority:
   * 1. og:title meta tag
   * 2. <title> tag
   *
   * @param html HTML content
   * @return Title string or null if not found
   */
  private fun parseOgTitle(html: String): String? {
    try {
      val doc = Jsoup.parse(html)

      // Priority 1: og:title
      doc.selectFirst("meta[property=og:title]")
          ?.attr("content")
          ?.takeIf { it.isNotBlank() }
          ?.let {
            return it
          }

      // Priority 2: <title> tag
      doc.selectFirst("title")
          ?.text()
          ?.takeIf { it.isNotBlank() }
          ?.let {
            return it
          }

      return null
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse HTML: ${e.message}")
      return null
    }
  }
}
