package io.github.omochice.pinosu.feature.bookmark.data.repository

import android.util.Log
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
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Relay-based implementation of [BookmarkRepository]
 *
 * Fetches kind:39701 bookmark events from multiple Nostr relays in parallel, deduplicates results,
 * and enriches them with URL metadata (og:title) when title tags are not present.
 *
 * @param relayPool Pool for querying Nostr relays
 * @param localAuthDataSource Local data source for cached authentication and relay data
 * @param urlMetadataFetcher Fetcher for URL metadata (og:title)
 */
@Singleton
class RelayBookmarkRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val localAuthDataSource: LocalAuthDataSource,
    private val urlMetadataFetcher: UrlMetadataFetcher
) : BookmarkRepository {

  companion object {
    private const val TAG = "RelayBookmarkRepository"
    const val KIND_BOOKMARK_LIST = 39701
    const val PER_RELAY_TIMEOUT_MS = 10000L
    const val DEFAULT_RELAY_URL = "wss://yabu.me"

    private fun isValidUrl(url: String): Boolean {
      return try {
        val uri = URI(url)
        uri.scheme in listOf("http", "https") && uri.host != null
      } catch (e: Exception) {
        false
      }
    }
  }

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

      val allItems = mutableListOf<BookmarkItem>()
      val mostRecentEvent = events.maxByOrNull { it.createdAt }

      events.forEach { event ->
        val dTags =
            event.tags
                .filter { it.isNotEmpty() && it[0] == "d" }
                .mapNotNull { it.getOrNull(1) }
                .map { "https://$it" }
                .filter { isValidUrl(it) }

        val rTags =
            event.tags.filter { it.isNotEmpty() && it[0] == "r" }.mapNotNull { it.getOrNull(1) }

        val urls =
            when {
              dTags.isNotEmpty() -> dTags
              rTags.isNotEmpty() -> rTags
              else -> return@forEach
            }

        val titleTag = event.tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)
        val title = titleTag ?: urlMetadataFetcher.fetchTitle(urls.first()).getOrNull()

        val item =
            BookmarkItem(
                type = "event",
                eventId = event.id,
                url = urls.first(),
                urls = urls,
                title = title,
                event =
                    BookmarkedEvent(
                        kind = event.kind,
                        content = event.content,
                        author = event.pubkey,
                        createdAt = event.createdAt,
                        tags = event.tags))

        allItems.add(item)
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
    } catch (e: Exception) {
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
          rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl.substring("https://".length)
          rawUrl.startsWith("http://", ignoreCase = true) -> rawUrl.substring("http://".length)
          else -> rawUrl
        }

    tags.add(listOf("d", normalizedUrl))

    if (title.isNotBlank()) {
      tags.add(listOf("title", title))
    }

    categories
        .filter { it.isNotBlank() }
        .forEach { category -> tags.add(listOf("t", category.trim())) }

    val fullUrl =
        if (rawUrl.startsWith("http://", ignoreCase = true) ||
            rawUrl.startsWith("https://", ignoreCase = true)) {
          rawUrl
        } else {
          "https://$normalizedUrl"
        }
    tags.add(listOf("r", fullUrl))

    return UnsignedNostrEvent(
        pubkey = hexPubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = KIND_BOOKMARK_LIST,
        tags = tags,
        content = comment)
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
    } catch (e: Exception) {
      Log.e(TAG, "Error publishing bookmark", e)
      Result.failure(e)
    }
  }
}
