package io.github.omochice.pinosu.data.repository

import android.util.Log
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.relay.RelayClient
import io.github.omochice.pinosu.data.util.Bech32
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkList
import io.github.omochice.pinosu.domain.model.BookmarkedEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Relay-based implementation of BookmarkRepository
 *
 * Fetches kind:39701 bookmark events from Nostr relays and enriches them with URL metadata
 * (og:title) when title tags are not present.
 *
 * @property relayClient Client for Nostr relay WebSocket communication
 * @property urlMetadataFetcher Fetcher for URL Open Graph metadata
 */
@Singleton
class RelayBookmarkRepository
@Inject
constructor(
    private val relayClient: RelayClient,
    private val urlMetadataFetcher: UrlMetadataFetcher
) : BookmarkRepository {

  companion object {
    private const val TAG = "RelayBookmarkRepository"
    const val KIND_BOOKMARK_LIST = 39701
    const val TIMEOUT_MS = 10000L

    private fun isValidUrl(url: String): Boolean {
      return try {
        val uri = java.net.URI(url)
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
          Bech32.npubToHex(pubkey)
              ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

      val filter = """{"kinds":[$KIND_BOOKMARK_LIST],"limit":10}"""
      val events = withTimeoutOrNull(TIMEOUT_MS) { relayClient.subscribe(filter).toList() }

      if (events.isNullOrEmpty()) {
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

        val (title, titleSource) =
            if (titleTag != null) {
              titleTag to "tag"
            } else {
              val fetchedTitle = urlMetadataFetcher.fetchTitle(urls.first()).getOrNull()
              fetchedTitle to "metadata"
            }

        val item =
            BookmarkItem(
                type = "event",
                eventId = event.id,
                url = urls.first(),
                urls = urls,
                title = title,
                titleSource = titleSource,
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
}
