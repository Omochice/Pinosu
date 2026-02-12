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

/**
 * URL metadata from Open Graph tags
 *
 * @property title og:title or HTML title
 * @property imageUrl og:image URL
 */
data class UrlMetadata(
    val title: String? = null,
    val imageUrl: String? = null,
)

/** Fetches URL metadata (Open Graph tags) */
interface UrlMetadataFetcher {
  suspend fun fetchMetadata(url: String): Result<UrlMetadata>
}

/**
 * Implementation of [UrlMetadataFetcher] using OkHttp and Jsoup
 *
 * @param okHttpClient HTTP client for fetching URL content
 */
@Singleton
class OkHttpUrlMetadataFetcher @Inject constructor(private val okHttpClient: OkHttpClient) :
    UrlMetadataFetcher {

  private val cache = LruCache<String, UrlMetadata>(MAX_CACHE_SIZE)

  override suspend fun fetchMetadata(url: String): Result<UrlMetadata> {
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
        val metadata = parseMetadata(html)

        cache.put(url, metadata)

        Result.success(metadata)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to fetch metadata for $url: ${e.message}")
        Result.failure(e)
      }
    }
  }

  /**
   * Parse Open Graph metadata from HTML content
   *
   * Title priority: og:title → <title> tag Image: og:image meta tag
   *
   * @param html HTML content
   * @return [UrlMetadata] with parsed title and image URL
   */
  private fun parseMetadata(html: String): UrlMetadata {
    try {
      val doc = Jsoup.parse(html)

      val title =
          doc.selectFirst("meta[property=og:title]")?.attr("content")?.takeIf { it.isNotBlank() }
              ?: doc.selectFirst("title")?.text()?.takeIf { it.isNotBlank() }

      val imageUrl =
          doc.selectFirst("meta[property=og:image]")?.attr("content")?.takeIf { it.isNotBlank() }

      return UrlMetadata(title = title, imageUrl = imageUrl)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse HTML: ${e.message}")
      return UrlMetadata()
    }
  }

  companion object {
    private const val TAG = "UrlMetadataFetcher"
    private const val MAX_CACHE_SIZE = 100
  }
}
