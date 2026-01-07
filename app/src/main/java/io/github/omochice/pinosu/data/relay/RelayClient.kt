package io.github.omochice.pinosu.data.relay

import io.github.omochice.pinosu.data.model.NostrEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class RelayClient @Inject constructor() {

  private val client = OkHttpClient()
  private val activeWebSockets = ConcurrentHashMap<String, WebSocket>()

  companion object {
    const val RELAY_URL = "wss://yabu.me"
  }

  /**
   * Subscribe to events from relay
   *
   * @param filter Nostr filter as JSON string
   * @return Flow of events
   */
  fun subscribe(filter: String): Flow<NostrEvent> = callbackFlow {
    val subscriptionId = UUID.randomUUID().toString()
    val request = Request.Builder().url(RELAY_URL).build()

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
                  // End of stored events - close connection for this PoC
                  webSocket.close(1000, "EOSE received")
                }
                "CLOSED" -> {
                  close()
                }
              }
            } catch (_: Exception) {
              // Ignore parse errors for PoC
            }
          }

          override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            close(t)
          }

          override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            activeWebSockets.remove(subscriptionId)
            close()
          }
        }

    val webSocket = client.newWebSocket(request, listener)
    activeWebSockets[subscriptionId] = webSocket

    awaitClose {
      webSocket.close(1000, "Flow cancelled")
      activeWebSockets.remove(subscriptionId)
    }
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
    } catch (_: Exception) {
      null
    }
  }

  /** Close all active connections */
  fun closeAll() {
    activeWebSockets.forEach { (_, ws) -> ws.close(1000, "Client closing") }
    activeWebSockets.clear()
  }
}
