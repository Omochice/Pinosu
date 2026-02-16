package io.github.omochice.pinosu.feature.comment.data.repository

import android.util.Log
import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CommentFilter(
    val kinds: List<Int>,
    @SerialName("#A") val aTag: List<String>,
    @SerialName("#E") val eTag: List<String>,
)

@Serializable
private data class EventIdFilter(
    val ids: List<String>,
)

/**
 * Relay-based implementation of CommentRepository
 *
 * Fetches kind 1111 (NIP-22) comment events from Nostr relays and publishes new comments. Uses
 * [RelayPool] for parallel relay queries and [LocalAuthDataSource] for cached relay list.
 */
@Singleton
class RelayCommentRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val localAuthDataSource: LocalAuthDataSource,
) : CommentRepository {

  override suspend fun getCommentsForBookmark(
      rootPubkey: String,
      dTag: String,
      rootEventId: String
  ): Result<List<Comment>> {
    return try {
      val relays = getRelaysForQuery()
      val aTagValue = "$KIND_BOOKMARK_LIST:$rootPubkey:$dTag"
      val filter =
          Json.encodeToString(
              CommentFilter(
                  kinds = listOf(KIND_COMMENT),
                  aTag = listOf(aTagValue),
                  eTag = listOf(rootEventId),
              ))

      val events = relayPool.subscribeWithTimeout(relays, filter, PER_RELAY_TIMEOUT_MS)

      val comments =
          events.map { event ->
            Comment(
                id = event.id,
                content = event.content,
                authorPubkey = event.pubkey,
                createdAt = event.createdAt,
                isAuthorComment = false,
                kind = event.kind)
          }

      Result.success(comments)
    } catch (e: IOException) {
      Log.e(TAG, "Error getting comments", e)
      Result.failure(e)
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Error getting comments", e)
      Result.failure(e)
    }
  }

  override fun createCommentEvent(
      hexPubkey: String,
      content: String,
      rootPubkey: String,
      dTag: String,
      rootEventId: String
  ): UnsignedNostrEvent {
    val aTagValue = "$KIND_BOOKMARK_LIST:$rootPubkey:$dTag"

    val tags =
        listOf(
            listOf("A", aTagValue),
            listOf("E", rootEventId),
            listOf("K", KIND_BOOKMARK_LIST.toString()),
            listOf("P", rootPubkey),
            listOf("a", aTagValue),
            listOf("e", rootEventId),
            listOf("k", KIND_BOOKMARK_LIST.toString()),
            listOf("p", rootPubkey),
        )

    return UnsignedNostrEvent(
        pubkey = hexPubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = KIND_COMMENT,
        tags = tags,
        content = content)
  }

  override suspend fun getEventsByIds(ids: List<String>): Result<List<NostrEvent>> {
    if (ids.isEmpty()) return Result.success(emptyList())
    return try {
      val relays = getRelaysForQuery()
      val filter = Json.encodeToString(EventIdFilter(ids = ids))

      val events = relayPool.subscribeWithTimeout(relays, filter, PER_RELAY_TIMEOUT_MS)
      Result.success(events)
    } catch (e: IOException) {
      Log.e(TAG, "Error fetching events by IDs", e)
      Result.failure(e)
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Error fetching events by IDs", e)
      Result.failure(e)
    }
  }

  override suspend fun publishComment(signedEventJson: String): Result<PublishResult> {
    return try {
      val relays = getRelaysForQuery()
      relayPool.publishEvent(relays, signedEventJson, PER_RELAY_TIMEOUT_MS)
    } catch (e: IOException) {
      Log.e(TAG, "Error publishing comment", e)
      Result.failure(e)
    }
  }

  private suspend fun getRelaysForQuery(): List<RelayConfig> {
    val cachedRelays = localAuthDataSource.getRelayList()
    return if (cachedRelays.isNullOrEmpty()) {
      listOf(RelayConfig(url = DEFAULT_RELAY_URL))
    } else {
      cachedRelays
    }
  }

  companion object {
    private const val TAG = "RelayCommentRepository"
    const val KIND_COMMENT = 1111
    const val KIND_BOOKMARK_LIST = 39_701
    const val PER_RELAY_TIMEOUT_MS = 10_000L
    const val DEFAULT_RELAY_URL = "wss://yabu.me"
  }
}
