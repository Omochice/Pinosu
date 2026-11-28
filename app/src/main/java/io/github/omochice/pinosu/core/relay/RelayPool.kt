package io.github.omochice.pinosu.core.relay

import android.util.Log
import io.github.omochice.pinosu.core.model.NostrEvent
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
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
   * Merges the events of [queryEachRelay], deduplicated by event ID. Events a relay sent before it
   * failed or timed out are included.
   *
   * @param relays List of relay configurations to query
   * @param filters Nostr filters as JSON strings; a relay ORs multiple filters within one REQ
   * @param timeoutMs Timeout in milliseconds for each relay
   * @return List of deduplicated events from all relays; empty when [relays] or [filters] is empty
   */
  suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filters: List<String>,
      timeoutMs: Long
  ): List<NostrEvent>

  /**
   * Query each relay in parallel and report its outcome separately
   *
   * Results are not merged across relays, so a relay that answered with no events can be told apart
   * from one that refused, failed, or timed out.
   *
   * @param relays List of relay configurations to query
   * @param filters Nostr filters as JSON strings; a relay ORs multiple filters within one REQ
   * @param timeoutMs Timeout in milliseconds for each relay
   * @return One result per relay, in the order of [relays]
   * @throws IllegalArgumentException if [filters] is empty
   */
  suspend fun queryEachRelay(
      relays: List<RelayConfig>,
      filters: List<String>,
      timeoutMs: Long
  ): List<RelayQueryResult>

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

  companion object {
    const val PER_RELAY_TIMEOUT_MS = 10_000L
  }
}

/**
 * Implementation of [RelayPool] that manages parallel connections to multiple Nostr relays
 *
 * @param okHttpClient HTTP client for WebSocket connections
 */
@Singleton
class RelayPoolImpl @Inject constructor(private val okHttpClient: OkHttpClient) : RelayPool {

  override suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filters: List<String>,
      timeoutMs: Long
  ): List<NostrEvent> {
    if (relays.isEmpty() || filters.isEmpty()) {
      return emptyList()
    }
    return queryEachRelay(relays, filters, timeoutMs).flatMap { it.events }.distinctBy { it.id }
  }

  override suspend fun queryEachRelay(
      relays: List<RelayConfig>,
      filters: List<String>,
      timeoutMs: Long
  ): List<RelayQueryResult> {
    require(filters.isNotEmpty()) { "At least one filter is required" }
    return coroutineScope {
      relays.map { relay -> async { queryRelay(relay.url, filters, timeoutMs) } }.awaitAll()
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
        extractEventId(signedEventJson).getOrElse {
          return Result.failure(it)
        }
    Log.d(TAG, "publishEvent called for eventId=$eventId")

    return coroutineScope {
      val results =
          writeRelays.map { relay ->
            async {
              try {
                withTimeoutOrNull(timeoutMs) { publishToRelay(relay.url, signedEventJson) }
                    ?: Pair(relay.url, "Timeout")
              } catch (e: IOException) {
                Log.w(TAG, "Failed to publish to relay ${relay.url}: ${e.message}")
                Pair(relay.url, e.message ?: "Unknown error")
              } catch (e: IllegalArgumentException) {
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
   * Extract event ID from a signed event JSON string
   *
   * @param signedEventJson Signed event as JSON string
   * @return Success(eventId) or Failure with parse/validation error
   */
  private fun extractEventId(signedEventJson: String): Result<String> {
    val id =
        try {
          Json.parseToJsonElement(signedEventJson).jsonObject["id"]?.jsonPrimitive?.content ?: ""
        } catch (e: IllegalArgumentException) {
          Log.e(TAG, "Failed to parse signedEventJson", e)
          return Result.failure(IllegalArgumentException("Invalid JSON: ${e.message}"))
        }
    if (id.isBlank()) {
      return Result.failure(Exception("Missing event id"))
    }
    return Result.success(id)
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
              } catch (e: SerializationException) {
                Log.e(TAG, "Error parsing OK response from $relayUrl: $text", e)
                if (continuation.isActive) {
                  continuation.resume(Pair(relayUrl, "Parse error: ${e.message}"))
                }
              } catch (e: IllegalArgumentException) {
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
   * Query a single relay until it ends the subscription, the connection fails, or time runs out
   *
   * @param relayUrl WebSocket URL of the relay
   * @param filters Nostr filters as JSON strings, sent as separate elements of the REQ message
   * @param timeoutMs Timeout in milliseconds
   * @return Events received before the query ended, together with how it ended
   */
  private suspend fun queryRelay(
      relayUrl: String,
      filters: List<String>,
      timeoutMs: Long
  ): RelayQueryResult {
    val events = ConcurrentLinkedQueue<NostrEvent>()
    val termination = CompletableDeferred<RelayTermination>()
    val webSocket =
        try {
          okHttpClient.newWebSocket(
              Request.Builder().url(relayUrl).build(),
              QueryListener(relayUrl, filters, events, termination))
        } catch (e: IllegalArgumentException) {
          Log.w(TAG, "Failed to query relay $relayUrl: ${e.message}")
          return RelayQueryResult(
              relayUrl, emptyList(), RelayTermination.Failed(e.message ?: "Invalid relay URL"))
        }
    val outcome =
        try {
          withTimeoutOrNull(timeoutMs) { termination.await() } ?: RelayTermination.TimedOut
        } finally {
          webSocket.close(1000, "Query finished")
        }
    return RelayQueryResult(relayUrl, events.distinctBy { it.id }, outcome)
  }

  /**
   * Listener that records events of one REQ and completes [termination] with the first ending
   *
   * Only the first ending counts because closing the socket after EOSE or CLOSED triggers
   * [onClosed], which must not overwrite the real outcome.
   */
  private class QueryListener(
      private val relayUrl: String,
      private val filters: List<String>,
      private val events: MutableCollection<NostrEvent>,
      private val termination: CompletableDeferred<RelayTermination>,
  ) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
      val subscriptionId = UUID.randomUUID().toString()
      webSocket.send("""["REQ","$subscriptionId",${filters.joinToString(",")}]""")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
      val message =
          try {
            json.decodeFromString<NostrRelayMessage>(text)
          } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unparsable query response from $relayUrl: $text", e)
            termination.complete(RelayTermination.Failed("Unparsable relay message: ${e.message}"))
            return
          }
      when (message) {
        is NostrRelayMessage.Event -> if (!termination.isCompleted) events.add(message.event)
        is NostrRelayMessage.Eose -> termination.complete(RelayTermination.Eose)
        is NostrRelayMessage.Closed ->
            termination.complete(RelayTermination.Closed(message.message))
        is NostrRelayMessage.Ok,
        is NostrRelayMessage.Unknown -> Unit
      }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
      Log.w(TAG, "Query connection to $relayUrl failed: ${t.message}")
      termination.complete(RelayTermination.Failed(t.message ?: "Connection failed"))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
      termination.complete(RelayTermination.Failed("Connection closed by relay: $reason"))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
      termination.complete(RelayTermination.Failed("Connection closed: $reason"))
    }
  }

  companion object {
    private const val TAG = "RelayPool"
    private val json = Json { ignoreUnknownKeys = true }
  }
}
