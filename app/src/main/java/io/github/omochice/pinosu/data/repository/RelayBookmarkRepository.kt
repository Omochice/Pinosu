package io.github.omochice.pinosu.data.repository

import android.util.Log
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
 */
@Singleton
class RelayBookmarkRepository @Inject constructor(private val relayClient: RelayClient) :
    BookmarkRepository {

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

      // Test: Check if any kind 10003 events exist on relay (without author filter)
      val filter = """{"kinds":[$KIND_BOOKMARK_LIST],"limit":10}"""
      Log.d(TAG, "Subscribing with filter: $filter (testing if kind 10003 exists on relay)")

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

      // Get the most recent event
      val event =
          events.maxByOrNull { it.createdAt }
              ?: run {
                Log.d(TAG, "No event found after filtering, returning null")
                return Result.success(null)
              }
      Log.d(TAG, "Selected event: id=${event.id}, kind=${event.kind}, tags=${event.tags.size}")

      val items =
          event.tags.mapNotNull { tag ->
            if (tag.isEmpty()) return@mapNotNull null
            when (tag[0]) {
              "e" -> {
                // Event reference tag
                val eventId = tag.getOrNull(1) ?: return@mapNotNull null
                BookmarkItem(type = "e", eventId = eventId, relayUrl = tag.getOrNull(2))
              }
              "a" -> {
                // Article/parameterized replaceable event reference
                val coordinate = tag.getOrNull(1) ?: return@mapNotNull null
                BookmarkItem(
                    type = "a", articleCoordinate = coordinate, relayUrl = tag.getOrNull(2))
              }
              "r" -> {
                // URL reference
                val url = tag.getOrNull(1) ?: return@mapNotNull null
                BookmarkItem(type = "r", url = url)
              }
              "t" -> {
                // Hashtag
                val hashtag = tag.getOrNull(1) ?: return@mapNotNull null
                BookmarkItem(type = "t", hashtag = hashtag)
              }
              else -> null
            }
          }
      Log.d(TAG, "Parsed ${items.size} bookmark items (e/a/r/t tags)")

      // Fetch full event data for "e" tag bookmarks only
      val eventIds = items.mapNotNull { if (it.type == "e") it.eventId else null }
      Log.d(TAG, "Fetching ${eventIds.size} bookmarked events (e tags)...")
      val bookmarkedEvents =
          if (eventIds.isNotEmpty()) {
            withTimeoutOrNull(TIMEOUT_MS) { relayClient.fetchEventsByIds(eventIds).toList() }
                ?: emptyList()
          } else {
            emptyList()
          }
      Log.d(TAG, "Fetched ${bookmarkedEvents.size} bookmarked events")

      // Map events to "e" tag items
      val eventMap = bookmarkedEvents.associateBy { it.id }
      val itemsWithEvents =
          items
              .map { item ->
                if (item.type == "e" && item.eventId != null) {
                  val fetchedEvent = eventMap[item.eventId]
                  if (fetchedEvent != null) {
                    item.copy(
                        event =
                            BookmarkedEvent(
                                kind = fetchedEvent.kind,
                                content = fetchedEvent.content,
                                author = fetchedEvent.pubkey,
                                createdAt = fetchedEvent.createdAt))
                  } else {
                    item
                  }
                } else {
                  item
                }
              }
              .sortedByDescending { it.event?.createdAt ?: 0L }

      // Check if content is encrypted (NIP-04 format: base64?iv=base64)
      val encryptedContent = if (event.content.isNotEmpty()) event.content else null

      // Generate JSON representation of the event
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
