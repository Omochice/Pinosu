package io.github.omochice.pinosu.feature.comment.data.repository

import android.util.Log
import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip22.Nip22
import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.core.nip.nip89.Nip89
import io.github.omochice.pinosu.core.nip.nipb0.NipB0
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.feature.comment.domain.repository.CommentRepository
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
)

@Serializable
private data class EventIdFilter(
    val ids: List<String>,
)

/**
 * Relay-based implementation of CommentRepository
 *
 * Fetches kind 1111 (NIP-22) comment events from Nostr relays and publishes new comments. Uses
 * [RelayPool] for parallel relay queries and [RelayListProvider] for relay list resolution.
 */
@Singleton
class RelayCommentRepository
@Inject
constructor(
    private val relayPool: RelayPool,
    private val relayListProvider: RelayListProvider,
    private val clientTagRepository: ClientTagRepository,
) : CommentRepository {

  override suspend fun getCommentsForBookmark(
      rootPubkey: String,
      identifier: String,
  ): Result<List<Comment>> {
    return try {
      val relays = relayListProvider.getRelays()
      val addressTagValue = NipB0.createAddress(rootPubkey, identifier)
      val filter =
          Json.encodeToString(
              CommentFilter(
                  kinds = listOf(Nip22.KIND_COMMENT),
                  aTag = listOf(addressTagValue),
              ))

      val events = relayPool.subscribeWithTimeout(relays, filter, RelayPool.PER_RELAY_TIMEOUT_MS)

      val comments =
          events.map { event ->
            Comment(
                id = event.id,
                content = event.content,
                authorPubkey = event.pubkey,
                createdAt = event.createdAt,
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
      identifier: String,
      rootEventId: String
  ): UnsignedNostrEvent {
    val addressTagValue = NipB0.createAddress(rootPubkey, identifier)

    val tags = buildList {
      add(listOf(Nip22.Tag.ADDRESS_ROOT, addressTagValue))
      add(listOf(Nip22.Tag.EVENT_ROOT, rootEventId))
      add(listOf(Nip22.Tag.KIND_ROOT, NipB0.KIND_BOOKMARK_LIST.toString()))
      add(listOf(Nip22.Tag.PUBKEY_ROOT, rootPubkey))
      add(listOf(Nip22.Tag.ADDRESS, addressTagValue))
      add(listOf(Nip22.Tag.EVENT, rootEventId))
      add(listOf(Nip22.Tag.KIND, NipB0.KIND_BOOKMARK_LIST.toString()))
      add(listOf(Nip22.Tag.PUBKEY, rootPubkey))
      if (clientTagRepository.clientTagEnabledFlow.value) {
        add(Nip89.clientTag())
      }
    }

    return UnsignedNostrEvent(
        pubkey = hexPubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = Nip22.KIND_COMMENT,
        tags = tags,
        content = content)
  }

  override suspend fun getEventsByIds(ids: List<String>): Result<List<NostrEvent>> {
    if (ids.isEmpty()) return Result.success(emptyList())
    return try {
      val relays = relayListProvider.getRelays()
      val filter = Json.encodeToString(EventIdFilter(ids = ids))

      val events = relayPool.subscribeWithTimeout(relays, filter, RelayPool.PER_RELAY_TIMEOUT_MS)
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
      val relays = relayListProvider.getRelays()
      relayPool.publishEvent(relays, signedEventJson, RelayPool.PER_RELAY_TIMEOUT_MS)
    } catch (e: IOException) {
      Log.e(TAG, "Error publishing comment", e)
      Result.failure(e)
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Error publishing comment", e)
      Result.failure(e)
    }
  }

  companion object {
    private const val TAG = "RelayCommentRepository"
  }
}
