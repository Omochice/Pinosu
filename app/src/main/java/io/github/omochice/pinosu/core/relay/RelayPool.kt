package io.github.omochice.pinosu.core.relay

import android.util.Log
import io.github.omochice.pinosu.core.model.NostrEvent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

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

  /**
   * Publish an event to multiple relays
   *
   * Sends the signed event to all write-enabled relays in parallel and waits for OK responses.
   *
   * @param relays List of relay configurations (only write-enabled relays will be used)
   * @param signedEventJson Signed event as JSON string
   * @param timeoutMs Timeout in milliseconds for each relay
   * @return Result containing PublishResult on success or error on failure
   */
  suspend fun publishEvent(
      relays: List<RelayConfig>,
      signedEventJson: String,
      timeoutMs: Long
  ): Result<PublishResult>
}

/**
 * Implementation of [RelayPool] that manages parallel connections to multiple Nostr relays
 *
 * @param okHttpClient HTTP client for WebSocket connections
 */
@Singleton
class RelayPoolImpl @Inject constructor(private val okHttpClient: OkHttpClient) : RelayPool {
  companion object {
    private const val TAG = "RelayPool"
    private val json = Json { ignoreUnknownKeys = true }
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

  override suspend fun publishEvent(
      relays: List<RelayConfig>,
      signedEventJson: String,
      timeoutMs: Long
  ): Result<PublishResult> {
    val writeRelays = relays.filter { it.write }
    if (writeRelays.isEmpty()) {
      return Result.failure(Exception("No write-enabled relays available"))
    }

    val eventId =
        try {
          Json.parseToJsonElement(signedEventJson).jsonObject["id"]?.jsonPrimitive?.content ?: ""
        } catch (e: Exception) {
          Log.e(TAG, "Failed to parse signedEventJson", e)
          return Result.failure(Exception("Invalid JSON: ${e.message}"))
        }
    if (eventId.isBlank()) {
      return Result.failure(Exception("Missing event id"))
    }
    Log.d(TAG, "publishEvent called for eventId=$eventId")

    return coroutineScope {
      val results =
          writeRelays.map { relay ->
            async {
              try {
                withTimeoutOrNull(timeoutMs) { publishToRelay(relay.url, signedEventJson) }
                    ?: Pair(relay.url, "Timeout")
              } catch (e: Exception) {
                Log.w(TAG, "Failed to publish to relay ${relay.url}: ${e.message}")
                Pair(relay.url, e.message ?: "Unknown error")
              }
            }
          }

      val allResults = results.awaitAll()
      val successful = allResults.filter { it.second == "OK" }.map { it.first }
      val failed = allResults.filter { it.second != "OK" }.map { Pair(it.first, it.second) }

      if (successful.isEmpty()) {
        Result.failure(
            Exception(
                "Failed to publish to any relay: ${failed.joinToString { "${it.first}: ${it.second}" }}"))
      } else {
        Result.success(PublishResult(eventId, successful, failed))
      }
    }
  }

  /**
   * Publish event to a single relay and wait for OK response
   *
   * @param relayUrl WebSocket URL of the relay
   * @param signedEventJson Signed event as JSON string
   * @return Pair of (relay URL, result message) where "OK" means success
   */
  private suspend fun publishToRelay(
      relayUrl: String,
      signedEventJson: String
  ): Pair<String, String> {
    return suspendCancellableCoroutine { continuation ->
      val request = Request.Builder().url(relayUrl).build()

      val listener =
          object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
              val eventMessage = """["EVENT",$signedEventJson]"""
              webSocket.send(eventMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
              try {
                val message = json.decodeFromString<NostrRelayMessage>(text)
                if (message !is NostrRelayMessage.Ok) return
                webSocket.close(1000, "OK received")
                if (!continuation.isActive) return
                val result = if (message.accepted) "OK" else message.message.ifEmpty { "Rejected" }
                continuation.resume(Pair(relayUrl, result))
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing OK response from $relayUrl: $text", e)
                if (continuation.isActive) {
                  continuation.resume(Pair(relayUrl, "Parse error: ${e.message}"))
                }
              }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
              Log.w(TAG, "WebSocket failure for $relayUrl: ${t.message}")
              if (continuation.isActive) {
                continuation.resume(Pair(relayUrl, t.message ?: "Connection failed"))
              }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
              if (continuation.isActive) {
                continuation.resume(Pair(relayUrl, "Connection closed: $reason"))
              }
            }
          }

      val webSocket = okHttpClient.newWebSocket(request, listener)

      continuation.invokeOnCancellation { webSocket.close(1000, "Cancelled") }
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
              when (val message = json.decodeFromString<NostrRelayMessage>(text)) {
                is NostrRelayMessage.Event -> trySend(message.event)
                is NostrRelayMessage.Eose -> webSocket.close(1000, "EOSE received")
                is NostrRelayMessage.Closed -> close()
                else -> {}
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
}
