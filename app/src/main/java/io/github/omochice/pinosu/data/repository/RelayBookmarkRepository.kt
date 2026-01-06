package io.github.omochice.pinosu.data.repository

import android.util.Log
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.relay.RelayClient
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkList
import io.github.omochice.pinosu.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.domain.util.Bech32
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Implementation of BookmarkRepository using RelayClient
 *
 * @property relayClient Relay client for WebSocket communication
 * @property urlMetadataFetcher Fetches URL metadata (og:title) from URLs
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
  }

  override suspend fun getBookmarkList(pubkey: String): Result<BookmarkList?> {
    Log.d(TAG, "Getting bookmark list for pubkey: $pubkey")
    return try {
      // Convert npub to hex format for relay filter
      val hexPubkey =
          Bech32.npubToHex(pubkey)
              ?: run {
                Log.d(TAG, "Failed to convert npub to hex: $pubkey")
                return Result.failure(IllegalArgumentException("Invalid npub format"))
              }
      Log.d(TAG, "Converted npub to hex: $hexPubkey")

      val filter = """{"kinds":[$KIND_BOOKMARK_LIST],"limit":100}"""
      Log.d(TAG, "Subscribing with filter: $filter")

      val events = withTimeoutOrNull(TIMEOUT_MS) { relayClient.subscribe(filter).toList() }
      Log.d(TAG, "Received ${events?.size ?: 0} events from relay")

      if (events.isNullOrEmpty()) {
        Log.d(TAG, "No events received, returning null")
        return Result.success(null)
      }

      // Log all received events with their full details
      events.forEachIndexed { index, event ->
        Log.d(TAG, "Event #$index:")
        Log.d(TAG, "  id: ${event.id}")
        Log.d(TAG, "  pubkey: ${event.pubkey}")
        Log.d(TAG, "  created_at: ${event.createdAt}")
        Log.d(TAG, "  kind: ${event.kind}")
        Log.d(TAG, "  content: ${event.content}")
        Log.d(TAG, "  tags (${event.tags.size}):")
        event.tags.forEachIndexed { tagIndex, tag ->
          Log.d(TAG, "    [$tagIndex]: ${tag.joinToString(", ")}")
        }
      }

      Log.d(TAG, "Processing ${events.size} events (event-based, r tags only)")
      val allItems = mutableListOf<BookmarkItem>()

      // Track the most recent event for metadata
      val mostRecentEvent = events.maxByOrNull { it.createdAt }

      // Process events with r tags only (event-based approach)
      events.forEach { event ->
        Log.d(TAG, "Processing event: id=${event.id}, kind=${event.kind}, tags=${event.tags.size}")

        // Extract r tags
        val rTags =
            event.tags.filter { it.isNotEmpty() && it[0] == "r" }.mapNotNull { it.getOrNull(1) }

        if (rTags.isEmpty()) {
          Log.d(TAG, "Skipping event ${event.id}: no r tags")
          Log.d(TAG, "  Event details:")
          Log.d(TAG, "    id: ${event.id}")
          Log.d(TAG, "    pubkey: ${event.pubkey}")
          Log.d(TAG, "    created_at: ${event.createdAt}")
          Log.d(TAG, "    kind: ${event.kind}")
          Log.d(TAG, "    content: ${event.content}")
          Log.d(TAG, "    tags (${event.tags.size}):")
          event.tags.forEachIndexed { tagIndex, tag ->
            Log.d(TAG, "      [$tagIndex]: ${tag.joinToString(", ")}")
          }
          return@forEach
        }

        Log.d(TAG, "Event ${event.id} has ${rTags.size} r tags")

        // Extract title tag
        val titleTag = event.tags.firstOrNull { it.size >= 2 && it[0] == "title" }?.get(1)

        // Determine title (title tag or og:title)
        val (title, titleSource) =
            if (titleTag != null) {
              Log.d(TAG, "Using title tag: $titleTag")
              titleTag to "tag"
            } else {
              Log.d(TAG, "No title tag, fetching og:title from ${rTags.first()}")
              val fetchedTitle = urlMetadataFetcher.fetchTitle(rTags.first()).getOrNull()
              if (fetchedTitle != null) {
                Log.d(TAG, "Fetched og:title: $fetchedTitle")
              } else {
                Log.d(TAG, "Failed to fetch og:title, will use null")
              }
              fetchedTitle to "metadata"
            }

        // Create BookmarkItem (event-based)
        val item =
            BookmarkItem(
                type = "event",
                eventId = event.id,
                url = rTags.first(), // For backward compatibility
                urls = rTags,
                title = title,
                titleSource = titleSource,
                event =
                    BookmarkedEvent(
                        kind = event.kind,
                        content = event.content, // Used as description
                        author = event.pubkey,
                        createdAt = event.createdAt,
                        tags = event.tags))

        allItems.add(item)
        Log.d(
            TAG,
            "Created event-based bookmark: eventId=${event.id}, title=$title, urls=${rTags.size}")
      }

      Log.d(TAG, "Created ${allItems.size} event-based bookmark items from ${events.size} events")

      val itemsWithEvents = allItems.sortedByDescending { it.event?.createdAt ?: 0L }

      val event = mostRecentEvent!!

      val encryptedContent = if (event.content.isNotEmpty()) event.content else null

      val contentDisplay =
          if (encryptedContent != null) {
            "(ENCRYPTED - NIP-04)"
          } else {
            event.content
          }

      val eventJson = buildString {
        appendLine("{")
        appendLine("  \"id\": \"${event.id}\",")
        appendLine("  \"pubkey\": \"${event.pubkey}\",")
        appendLine("  \"created_at\": ${event.createdAt},")
        appendLine("  \"kind\": ${event.kind},")
        appendLine("  \"content\": \"$contentDisplay\",")
        if (encryptedContent != null) {
          appendLine("  \"encrypted_content\": \"${encryptedContent.take(50)}...\",")
        }
        appendLine("  \"tags\": [")
        event.tags.forEachIndexed { index, tag ->
          append("    [")
          append(tag.joinToString(", ") { "\"$it\"" })
          append("]")
          if (index < event.tags.size - 1) appendLine(",") else appendLine()
        }
        appendLine("  ]")
        append("}")
      }

      val result =
          BookmarkList(
              pubkey = event.pubkey,
              items = itemsWithEvents,
              createdAt = event.createdAt,
              rawEventJson = eventJson,
              encryptedContent = encryptedContent)
      Log.d(TAG, "Returning bookmark list with ${result.items.size} items")
      result.items.forEachIndexed { index, item ->
        Log.d(
            TAG,
            "Item #$index: type=${item.type}, eventId=${item.eventId}, url=${item.url}, hashtag=${item.hashtag}, hasEvent=${item.event != null}")
      }
      if (encryptedContent != null) {
        Log.d(TAG, "Content is encrypted, needs decryption")
      }
      Result.success(result)
    } catch (e: Exception) {
      Log.d(TAG, "Error getting bookmark list: ${e.message}", e)
      Result.failure(e)
    }
  }
}
