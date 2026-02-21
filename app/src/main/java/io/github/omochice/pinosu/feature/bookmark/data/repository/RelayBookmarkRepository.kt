package io.github.omochice.pinosu.feature.bookmark.data.repository

import android.util.Log
import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.bookmark.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkList
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.domain.repository.BookmarkRepository
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

/**
 * Relay-based implementation of [BookmarkRepository]
 *
 * Fetches kind:39701 bookmark events from multiple Nostr relays in parallel, deduplicates results,
 * and enriches them with URL metadata (og:title, og:image).
 *
 * @param relayPool Pool for querying Nostr relays
 * @param localAuthDataSource Local data source for cached authentication and relay data
 * @param urlMetadataFetcher Fetcher for URL metadata (og:title, og:image)
 */
@Singleton
class RelayBookmarkRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val localAuthDataSource: LocalAuthDataSource,
    private val urlMetadataFetcher: UrlMetadataFetcher
) : BookmarkRepository {

  /**
   * Retrieve bookmark list for the specified public key
   *
   * @param pubkey Nostr public key (Bech32-encoded format, starts with npub1)
   * @return Success(BookmarkList) if found, Success(null) if no bookmarks, Failure on error
   */
  override suspend fun getBookmarkList(pubkey: String): Result<BookmarkList?> {
    return try {
      val hexPubkey =
          Pubkey.parse(pubkey)?.hex
              ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

      val relays = getRelaysForQuery()
      val filter = """{"kinds":[$KIND_BOOKMARK_LIST],"limit":10}"""
      val events = relayPool.subscribeWithTimeout(relays, filter, PER_RELAY_TIMEOUT_MS)

      if (events.isEmpty()) {
        return Result.success(null)
      }

      val mostRecentEvent = events.maxByOrNull { it.createdAt }

      val allItems = coroutineScope {
        events.map { event -> async { buildBookmarkItem(event) } }.awaitAll().filterNotNull()
      }

      val itemsWithEvents = allItems.sortedByDescending { it.event?.createdAt ?: 0L }
      val event = mostRecentEvent ?: return Result.success(null)
      val encryptedContent = if (event.content.isNotEmpty()) event.content else null

      Result.success(
          BookmarkList(
              pubkey = event.pubkey,
              items = itemsWithEvents,
              createdAt = event.createdAt,
              encryptedContent = encryptedContent))
    } catch (e: IOException) {
      Log.e(TAG, "Error getting bookmark list", e)
      Result.failure(e)
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Error getting bookmark list", e)
      Result.failure(e)
    }
  }

  /**
   * Get relay list for querying bookmarks
   *
   * Returns cached relay list from LocalAuthDataSource, or default relay if not available.
   *
   * @return List of relay configurations for querying
   */
  private suspend fun getRelaysForQuery(): List<RelayConfig> {
    val cachedRelays = localAuthDataSource.getRelayList()
    return if (cachedRelays.isNullOrEmpty()) {
      Log.d(TAG, "No cached relay list, using default relay: $DEFAULT_RELAY_URL")
      listOf(RelayConfig(url = DEFAULT_RELAY_URL))
    } else {
      Log.d(TAG, "Using ${cachedRelays.size} cached relays")
      cachedRelays
    }
  }

  /**
   * Create an unsigned bookmark event
   *
   * @param hexPubkey Author's public key (hex-encoded)
   * @param url URL to bookmark (without scheme)
   * @param title Bookmark title
   * @param categories List of categories (t-tags)
   * @param comment Bookmark comment (content)
   * @return Unsigned event ready for NIP-55 signing
   */
  override fun createBookmarkEvent(
      hexPubkey: String,
      url: String,
      title: String,
      categories: List<String>,
      comment: String
  ): UnsignedNostrEvent {
    val tags = mutableListOf<List<String>>()

    val rawUrl = url.trim()
    val normalizedUrl =
        when {
          rawUrl.startsWith(SCHEME_HTTPS, ignoreCase = true) ->
              rawUrl.substring(SCHEME_HTTPS.length)
          rawUrl.startsWith(SCHEME_HTTP, ignoreCase = true) -> rawUrl.substring(SCHEME_HTTP.length)
          else -> rawUrl
        }

    tags.add(listOf("d", normalizedUrl))

    if (title.isNotBlank()) {
      tags.add(listOf("title", title))
    }

    categories
        .filter { it.isNotBlank() }
        .forEach { category -> tags.add(listOf("t", category.trim())) }

    val fullUrl = if (rawUrl != normalizedUrl) rawUrl else "$SCHEME_HTTPS$normalizedUrl"
    tags.add(listOf("r", fullUrl))

    return UnsignedNostrEvent(
        pubkey = hexPubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = KIND_BOOKMARK_LIST,
        tags = tags,
        content = comment)
  }

  private suspend fun buildBookmarkItem(event: NostrEvent): BookmarkItem? {
    val dTags =
        event.tags
            .filter { it.isNotEmpty() && it[0] == "d" }
            .mapNotNull { it.getOrNull(1) }
            .map { "https://$it" }
            .filter { isValidUrl(it) }

    val rTags =
        event.tags
            .filter { it.isNotEmpty() && it[0] == "r" }
            .mapNotNull { it.getOrNull(1) }
            .filter { isValidUrl(it) }

    val urls =
        when {
          dTags.isNotEmpty() -> dTags
          rTags.isNotEmpty() -> rTags
          else -> return null
        }

    val titleTag = event.tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
    val metadata = urlMetadataFetcher.fetchMetadata(urls.first()).getOrNull()
    val title = titleTag ?: metadata?.title

    return BookmarkItem(
        type = "event",
        eventId = event.id,
        url = urls.first(),
        urls = urls,
        title = title,
        imageUrl = metadata?.imageUrl,
        event =
            BookmarkedEvent(
                kind = event.kind,
                content = event.content,
                author = event.pubkey,
                createdAt = event.createdAt,
                tags = event.tags),
        rawJson = Json.encodeToString(event))
  }

  /**
   * Publish a signed bookmark event to relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Result containing PublishResult on success or error on failure
   */
  override suspend fun publishBookmark(signedEventJson: String): Result<PublishResult> {
    return try {
      val relays = getRelaysForQuery()
      relayPool.publishEvent(relays, signedEventJson, PER_RELAY_TIMEOUT_MS)
    } catch (e: IOException) {
      Log.e(TAG, "Error publishing bookmark", e)
      Result.failure(e)
    }
  }

  companion object {
    private const val TAG = "RelayBookmarkRepository"
    private const val SCHEME_HTTPS = "https://"
    private const val SCHEME_HTTP = "http://"
    const val KIND_BOOKMARK_LIST = 39701
    const val PER_RELAY_TIMEOUT_MS = 10_000L
    const val DEFAULT_RELAY_URL = "wss://yabu.me"

    private fun isValidUrl(url: String): Boolean {
      return try {
        val uri = URI(url)
        uri.scheme in listOf("http", "https") && uri.host != null
      } catch (_: URISyntaxException) {
        false
      }
    }
  }
}
