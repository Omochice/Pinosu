package io.github.omochice.pinosu.data.relay

import android.util.Log
import io.github.omochice.pinosu.data.model.NostrEvent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * Interface for managing multiple relay connections
 *
 * Queries multiple relays in parallel and merges/deduplicates results.
 */
interface RelayPool {

  /**
   * Subscribe to events from multiple relays with timeout
   *
   * Connects to all specified relays in parallel, collects events, deduplicates by event ID, and
   * returns the merged result.
   *
   * @param relays List of relay configurations to query
   * @param filter Nostr filter as JSON string
   * @param timeoutMs Timeout in milliseconds for each relay
   * @return List of deduplicated events from all relays
   */
  suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filter: String,
      timeoutMs: Long
  ): List<NostrEvent>
}

/**
 * Implementation of RelayPool that manages parallel connections to multiple Nostr relays
 *
 * @property okHttpClient OkHttpClient for WebSocket connections
 */
@Singleton
class RelayPoolImpl @Inject constructor(private val okHttpClient: OkHttpClient) : RelayPool {

  companion object {
    private const val TAG = "RelayPool"
  }

  override suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filter: String,
      timeoutMs: Long
  ): List<NostrEvent> {
    if (relays.isEmpty()) {
      return emptyList()
    }

    return coroutineScope {
      val deferredResults =
          relays.map { relay ->
            async {
              try {
                withTimeoutOrNull(timeoutMs) { subscribeToRelay(relay.url, filter).toList() }
                    ?: emptyList()
              } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch from relay ${relay.url}: ${e.message}")
                emptyList()
              }
            }
          }

      val allEvents = deferredResults.awaitAll().flatten()
      allEvents.distinctBy { it.id }
    }
  }

  /**
   * Subscribe to events from a single relay
   *
   * @param relayUrl WebSocket URL of the relay
   * @param filter Nostr filter as JSON string
   * @return Flow of events from the relay
   */
  private fun subscribeToRelay(relayUrl: String, filter: String): Flow<NostrEvent> = callbackFlow {
    val subscriptionId = UUID.randomUUID().toString()
    val request = Request.Builder().url(relayUrl).build()

    val listener =
        object : WebSocketListener() {
          override fun onOpen(webSocket: WebSocket, response: Response) {
            val reqMessage = """["REQ","$subscriptionId",$filter]"""
            webSocket.send(reqMessage)
          }

          override fun onMessage(webSocket: WebSocket, text: String) {
            try {
              val jsonArray = JSONArray(text)
              val messageType = jsonArray.getString(0)
              when (messageType) {
                "EVENT" -> {
                  val eventJson = jsonArray.getJSONObject(2)
                  val event = parseEvent(eventJson)
                  if (event != null) {
                    trySend(event)
                  }
                }
                "EOSE" -> {
                  webSocket.close(1000, "EOSE received")
                }
                "CLOSED" -> {
                  close()
                }
              }
            } catch (e: Exception) {
              Log.e(TAG, "Error parsing message from $relayUrl: $text", e)
              close(e)
            }
          }

          override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.w(TAG, "WebSocket failure for $relayUrl: ${t.message}")
            close(t)
          }

          override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            close()
          }
        }

    val webSocket = okHttpClient.newWebSocket(request, listener)

    awaitClose { webSocket.close(1000, "Flow cancelled") }
  }

  /**
   * Parse JSON to NostrEvent
   *
   * @param json JSONObject of the event
   * @return Parsed NostrEvent or null
   */
  private fun parseEvent(json: JSONObject): NostrEvent? {
    return try {
      val tagsArray = json.getJSONArray("tags")
      val tags = mutableListOf<List<String>>()
      for (i in 0 until tagsArray.length()) {
        val tagArray = tagsArray.getJSONArray(i)
        val tag = mutableListOf<String>()
        for (j in 0 until tagArray.length()) {
          tag.add(tagArray.getString(j))
        }
        tags.add(tag)
      }

      NostrEvent(
          id = json.getString("id"),
          pubkey = json.getString("pubkey"),
          createdAt = json.getLong("created_at"),
          kind = json.getInt("kind"),
          tags = tags,
          content = json.getString("content"))
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing event: ${e.message}")
      null
    }
  }
}
