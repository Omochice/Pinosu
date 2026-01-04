package io.github.omochice.pinosu.data.repository

import android.util.Log
import io.github.omochice.pinosu.data.relay.RelayClient
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkList
import io.github.omochice.pinosu.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.domain.model.ThreadReply
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
    const val MAX_THREAD_DEPTH = 2
    const val MAX_REPLIES_PER_LEVEL = 20
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

      val filter = """{"kinds":[$KIND_BOOKMARK_LIST],"limit":10}"""
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

      Log.d(TAG, "Processing ${events.size} events to extract bookmark items")
      val allItems = mutableListOf<BookmarkItem>()

      // Track the most recent event for metadata
      val mostRecentEvent = events.maxByOrNull { it.createdAt }

      events.forEach { event ->
        Log.d(TAG, "Processing event: id=${event.id}, kind=${event.kind}, tags=${event.tags.size}")

        val items =
            event.tags.mapNotNull { tag ->
              if (tag.isEmpty()) {
                Log.d(TAG, "Skipping empty tag")
                return@mapNotNull null
              }
              val tagType = tag[0]
              Log.d(TAG, "Processing tag type: $tagType, full tag: ${tag.joinToString(", ")}")
              when (tagType) {
                "e" -> {
                  val eventId = tag.getOrNull(1)
                  if (eventId == null) {
                    Log.d(TAG, "Skipping e tag with null eventId")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created e tag bookmark: eventId=$eventId")
                  BookmarkItem(type = "e", eventId = eventId, relayUrl = tag.getOrNull(2))
                }
                "a" -> {
                  val coordinate = tag.getOrNull(1)
                  if (coordinate == null) {
                    Log.d(TAG, "Skipping a tag with null coordinate")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created a tag bookmark: coordinate=$coordinate")
                  BookmarkItem(
                      type = "a", articleCoordinate = coordinate, relayUrl = tag.getOrNull(2))
                }
                "r" -> {
                  val url = tag.getOrNull(1)
                  if (url == null) {
                    Log.d(TAG, "Skipping r tag with null url")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created r tag bookmark: url=$url")
                  BookmarkItem(type = "r", url = url)
                }
                "t" -> {
                  val hashtag = tag.getOrNull(1)
                  if (hashtag == null) {
                    Log.d(TAG, "Skipping t tag with null hashtag")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created t tag bookmark: hashtag=$hashtag")
                  BookmarkItem(type = "t", hashtag = hashtag)
                }
                "q" -> {
                  val eventId = tag.getOrNull(1)
                  if (eventId == null) {
                    Log.d(TAG, "Skipping q tag with null eventId")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created q tag bookmark: eventId=$eventId")
                  BookmarkItem(type = "q", eventId = eventId, relayUrl = tag.getOrNull(2))
                }
                "p" -> {
                  val pubkey = tag.getOrNull(1)
                  if (pubkey == null) {
                    Log.d(TAG, "Skipping p tag with null pubkey")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created p tag bookmark: pubkey=$pubkey")
                  BookmarkItem(type = "p", pubkey = pubkey, relayUrl = tag.getOrNull(2))
                }
                "d" -> {
                  val identifier = tag.getOrNull(1)
                  if (identifier == null) {
                    Log.d(TAG, "Skipping d tag with null identifier")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created d tag bookmark: identifier=$identifier")
                  BookmarkItem(type = "d", identifier = identifier)
                }
                "title" -> {
                  val title = tag.getOrNull(1)
                  if (title == null) {
                    Log.d(TAG, "Skipping title tag with null title")
                    return@mapNotNull null
                  }
                  Log.d(TAG, "Created title tag bookmark: title=$title")
                  BookmarkItem(type = "title", title = title)
                }
                else -> {
                  Log.d(TAG, "Skipping unknown tag type: $tagType")
                  null
                }
              }
            }
        Log.d(
            TAG,
            "Parsed ${items.size} bookmark items from event ${event.id} (e/a/r/t/q/p/d/title tags)")
        if (items.isEmpty()) {
          Log.d(
              TAG,
              "WARNING: No bookmark items parsed from ${event.tags.size} tags in event ${event.id}")
        }
        allItems.addAll(items)
      }

      Log.d(TAG, "Collected total ${allItems.size} bookmark items from ${events.size} events")

      val uniqueItems =
          allItems.distinctBy { item ->
            when (item.type) {
              "e" -> "e:${item.eventId}"
              "a" -> "a:${item.articleCoordinate}"
              "r" -> "r:${item.url}"
              "t" -> "t:${item.hashtag}"
              "q" -> "q:${item.eventId}"
              "p" -> "p:${item.pubkey}"
              "d" -> "d:${item.identifier}"
              "title" -> "title:${item.title}"
              else -> item.hashCode().toString()
            }
          }
      Log.d(TAG, "After deduplication: ${uniqueItems.size} unique bookmark items")

      val eventIds =
          uniqueItems.mapNotNull { if (it.type == "e" || it.type == "q") it.eventId else null }
      Log.d(TAG, "Fetching ${eventIds.size} bookmarked events (e/q tags)...")
      val bookmarkedEvents =
          if (eventIds.isNotEmpty()) {
            withTimeoutOrNull(TIMEOUT_MS) { relayClient.fetchEventsByIds(eventIds).toList() }
                ?: emptyList()
          } else {
            emptyList()
          }
      Log.d(TAG, "Fetched ${bookmarkedEvents.size} bookmarked events")

      val threadRepliesMap = fetchThreadReplies(eventIds)
      Log.d(TAG, "Fetched thread replies for ${threadRepliesMap.size} events")

      val eventMap = bookmarkedEvents.associateBy { it.id }
      val itemsWithEvents =
          uniqueItems
              .map { item ->
                if ((item.type == "e" || item.type == "q") && item.eventId != null) {
                  val fetchedEvent = eventMap[item.eventId]
                  val replies = threadRepliesMap[item.eventId] ?: emptyList()
                  if (fetchedEvent != null) {
                    item.copy(
                        event =
                            BookmarkedEvent(
                                kind = fetchedEvent.kind,
                                content = fetchedEvent.content,
                                author = fetchedEvent.pubkey,
                                createdAt = fetchedEvent.createdAt,
                                tags = fetchedEvent.tags),
                        threadReplies = replies)
                  } else {
                    item.copy(threadReplies = replies)
                  }
                } else {
                  item
                }
              }
              .sortedByDescending { it.event?.createdAt ?: 0L }

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

  /**
   * Fetch threaded replies for bookmarked events
   *
   * @param eventIds List of root event IDs (bookmarked events)
   * @param maxDepth Maximum depth to fetch (default: MAX_THREAD_DEPTH)
   * @return Map of eventId -> List<ThreadReply>
   */
  private suspend fun fetchThreadReplies(
      eventIds: List<String>,
      maxDepth: Int = MAX_THREAD_DEPTH
  ): Map<String, List<ThreadReply>> {
    if (eventIds.isEmpty()) return emptyMap()

    Log.d(TAG, "Fetching thread replies for ${eventIds.size} events, max depth: $maxDepth")

    try {
      // Fetch all replies level by level (breadth-first)
      val allReplies = mutableMapOf<String, ThreadReply>()
      var currentLevelEventIds = eventIds
      var currentDepth = 1

      while (currentDepth <= maxDepth && currentLevelEventIds.isNotEmpty()) {
        Log.d(TAG, "Fetching level $currentDepth replies for ${currentLevelEventIds.size} events")

        val levelReplies = fetchRepliesForEvents(currentLevelEventIds)
        val nextLevelEventIds = mutableListOf<String>()

        levelReplies.forEach { (parentId, replies) ->
          replies.forEach { reply ->
            val threadReply =
                ThreadReply(
                    eventId = reply.id,
                    event =
                        BookmarkedEvent(
                            kind = reply.kind,
                            content = reply.content,
                            author = reply.pubkey,
                            createdAt = reply.createdAt,
                            tags = reply.tags),
                    depth = currentDepth,
                    replies = emptyList())
            allReplies[reply.id] = threadReply
            nextLevelEventIds.add(reply.id)
          }
        }

        currentLevelEventIds = nextLevelEventIds
        currentDepth++
      }

      Log.d(TAG, "Fetched ${allReplies.size} total replies across all levels")

      val flatReplies = mutableMapOf<String, MutableList<ThreadReply>>()
      allReplies.values.forEach { reply ->
        val parentId = parseParentEventId(reply.event?.tags ?: emptyList())
        if (parentId != null) {
          flatReplies.getOrPut(parentId) { mutableListOf() }.add(reply)
        }
      }

      val result = mutableMapOf<String, List<ThreadReply>>()
      eventIds.forEach { rootId ->
        val nestedReplies = buildNestedThreads(rootId, flatReplies)
        if (nestedReplies.isNotEmpty()) {
          result[rootId] = nestedReplies
        }
      }

      Log.d(TAG, "Built nested threads for ${result.size} root events")
      return result
    } catch (e: Exception) {
      Log.w(TAG, "Error fetching thread replies: ${e.message}", e)
      return emptyMap()
    }
  }

  /**
   * Build nested thread structure from flat reply map
   *
   * @param rootEventId Root event ID
   * @param flatReplies Flat map of eventId -> List<ThreadReply>
   * @param currentDepth Current depth level
   * @return List of ThreadReply with nested structure
   */
  private fun buildNestedThreads(
      rootEventId: String,
      flatReplies: Map<String, List<ThreadReply>>,
      currentDepth: Int = 1
  ): List<ThreadReply> {
    if (currentDepth > MAX_THREAD_DEPTH) {
      Log.d(TAG, "Reached max depth $MAX_THREAD_DEPTH for event $rootEventId")
      return emptyList()
    }

    val directReplies = flatReplies[rootEventId] ?: emptyList()
    Log.d(
        TAG,
        "Building nested threads for $rootEventId at depth $currentDepth: ${directReplies.size} replies")

    return directReplies.map { reply ->
      val nestedReplies = buildNestedThreads(reply.eventId, flatReplies, currentDepth + 1)
      reply.copy(replies = nestedReplies)
    }
  }

  /**
   * Fetch replies for given events using NIP-10
   *
   * @param eventIds Events to find replies for
   * @return Map of parentId -> List<NostrEvent> replies
   */
  private suspend fun fetchRepliesForEvents(
      eventIds: List<String>
  ): Map<String, List<RelayClient.NostrEvent>> {
    if (eventIds.isEmpty()) return emptyMap()

    try {
      val idsFilter = eventIds.joinToString(",") { "\"$it\"" }
      val filter = """{"kinds":[1],"#e":[$idsFilter],"limit":$MAX_REPLIES_PER_LEVEL}"""
      Log.d(TAG, "Fetching replies with filter: $filter")

      val replies =
          withTimeoutOrNull(TIMEOUT_MS) { relayClient.subscribe(filter).toList() } ?: emptyList()

      Log.d(TAG, "Fetched ${replies.size} potential replies")

      val replyMap = mutableMapOf<String, MutableList<RelayClient.NostrEvent>>()
      replies.forEach { reply ->
        val parentId = parseParentEventId(reply.tags)
        if (parentId != null && eventIds.contains(parentId)) {
          replyMap.getOrPut(parentId) { mutableListOf() }.add(reply)
        }
      }

      Log.d(TAG, "Grouped replies: ${replyMap.size} parents with replies")
      return replyMap
    } catch (e: Exception) {
      Log.w(TAG, "Error fetching replies: ${e.message}", e)
      return emptyMap()
    }
  }

  /**
   * Parse NIP-10 tags to find parent event ID
   *
   * Priority:
   * 1. "e" tag with "reply" marker
   * 2. Last "e" tag (positional)
   * 3. "e" tag with "root" marker (if only one)
   *
   * @param tags Event tags
   * @return Parent event ID or null if not a reply
   */
  private fun parseParentEventId(tags: List<List<String>>): String? {
    try {
      val eTags = tags.filter { it.isNotEmpty() && it[0] == "e" }
      if (eTags.isEmpty()) return null

      // Priority 1: Look for "reply" marker
      val replyTag = eTags.firstOrNull { it.size >= 4 && it[3] == "reply" }
      if (replyTag != null && replyTag.size >= 2) {
        return replyTag[1]
      }

      // Priority 2: If multiple e tags, last one is parent (positional)
      if (eTags.size > 1 && eTags.last().size >= 2) {
        return eTags.last()[1]
      }

      // Priority 3: Single e tag (could be root or parent)
      if (eTags.size == 1 && eTags[0].size >= 2) {
        return eTags[0][1]
      }

      return null
    } catch (e: Exception) {
      Log.w(TAG, "Failed to parse parent event ID: ${e.message}")
      return null
    }
  }
}
