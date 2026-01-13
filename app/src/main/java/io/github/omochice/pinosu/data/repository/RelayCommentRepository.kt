package io.github.omochice.pinosu.data.repository

import android.util.Log
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.relay.RelayPool
import io.github.omochice.pinosu.feature.comments.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Relay-based implementation of CommentRepository
 *
 * Fetches kind:1111 comment events from Nostr relays that reference a specific event via `e` tag.
 *
 * @property relayPool Pool for relay queries
 * @property localAuthDataSource Local data source for cached relay list
 */
@Singleton
class RelayCommentRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val localAuthDataSource: LocalAuthDataSource,
) : CommentRepository {

  companion object {
    private const val TAG = "RelayCommentRepository"
    const val KIND_COMMENT = 1111
    const val PER_RELAY_TIMEOUT_MS = 10000L
    const val DEFAULT_RELAY_URL = "wss://yabu.me"
  }

  /**
   * Fetch comments for a specific event
   *
   * @param eventId The event ID to fetch comments for (hex-encoded)
   * @return Success with list of comments sorted by createdAt descending, or Failure on error
   */
  override suspend fun getCommentsForEvent(eventId: String): Result<List<Comment>> {
    return try {
      val relays = getRelaysForQuery()
      val filter = """{"kinds":[$KIND_COMMENT],"#e":["$eventId"]}"""

      Log.d(TAG, "Fetching comments for event: $eventId")
      val events = relayPool.subscribeWithTimeout(relays, filter, PER_RELAY_TIMEOUT_MS)

      val comments =
          events
              .mapNotNull { event ->
                val referencedEventId =
                    event.tags.firstOrNull { it.isNotEmpty() && it[0] == "e" }?.getOrNull(1)
                        ?: return@mapNotNull null

                Comment(
                    id = event.id,
                    content = event.content,
                    author = event.pubkey,
                    createdAt = event.createdAt,
                    referencedEventId = referencedEventId)
              }
              .sortedByDescending { it.createdAt }

      Log.d(TAG, "Found ${comments.size} comments for event: $eventId")
      Result.success(comments)
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching comments for event: $eventId", e)
      Result.failure(e)
    }
  }

  /**
   * Get relay list for querying comments
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
}
