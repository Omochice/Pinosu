package io.github.omochice.pinosu.core.relay

import io.github.omochice.pinosu.core.model.NostrEvent
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [RelayPoolImpl] */
@RunWith(RobolectricTestRunner::class)
class RelayPoolTest {
  private lateinit var okHttpClient: OkHttpClient
  private lateinit var relayPool: RelayPoolImpl
  private val executor = Executors.newSingleThreadScheduledExecutor()

  @Before
  fun setup() {
    okHttpClient = mockk(relaxed = true)
    relayPool = RelayPoolImpl(okHttpClient)
  }

  @org.junit.After
  fun tearDown() {
    executor.shutdown()
  }

  private fun createMockEvent(id: String, pubkey: String = "testpubkey"): NostrEvent {
    return NostrEvent(
        id = id,
        pubkey = pubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = 39701,
        tags = listOf(listOf("r", "https://example.com")),
        content = "",
        sig = "dummy-sig")
  }

  /**
   * Helper class to manage WebSocket mock state and async callback invocation.
   *
   * Uses executor-based scheduling to ensure callbacks happen after callbackFlow is ready. The
   * subscription ID is extracted from the REQ message to ensure EOSE matches correctly.
   */
  private inner class MockWebSocketState(private val events: List<NostrEvent>) {
    val ws: WebSocket = mockk(relaxed = true)
    private val closed = java.util.concurrent.atomic.AtomicBoolean(false)

    @Volatile private var wsListener: WebSocketListener? = null

    init {
      every { ws.send(any<String>()) } answers
          {
            val message = firstArg<String>()
            val currentListener = wsListener

            if (currentListener != null && message.startsWith("[\"REQ\"")) {
              val subId = extractSubscriptionId(message)

              executor.execute {
                try {
                  Thread.sleep(50)
                  if (!closed.get()) {
                    for (event in events) {
                      if (!closed.get()) {
                        val eventJson = buildEventJson(subId, event)
                        currentListener.onMessage(ws, eventJson)
                        Thread.sleep(10)
                      }
                    }
                    Thread.sleep(20)
                    if (!closed.get()) {
                      currentListener.onMessage(ws, """["EOSE","$subId"]""")
                    }
                  }
                } catch (_: InterruptedException) {}
              }
            }
            true
          }

      every { ws.close(any(), any()) } answers
          {
            closed.set(true)
            val code = firstArg<Int>()
            val reason = secondArg<String?>() ?: ""
            val currentListener = wsListener
            if (currentListener != null) {
              executor.execute {
                try {
                  Thread.sleep(30)
                  currentListener.onClosed(ws, code, reason)
                } catch (_: InterruptedException) {}
              }
            }
            true
          }
    }

    private fun extractSubscriptionId(reqMessage: String): String {
      val regex = """\["REQ","([^"]+)".*\]""".toRegex()
      return regex.find(reqMessage)?.groupValues?.get(1) ?: "unknown"
    }

    private fun buildEventJson(subscriptionId: String, event: NostrEvent): String {
      val eventJson = Json.encodeToString(NostrEvent.serializer(), event)
      return """["EVENT","$subscriptionId",$eventJson]"""
    }

    fun setListener(l: WebSocketListener) {
      wsListener = l
    }

    fun triggerOpen() {
      executor.execute {
        try {
          Thread.sleep(30)
          val mockResponse = mockk<Response>(relaxed = true)
          wsListener?.onOpen(ws, mockResponse)
        } catch (_: InterruptedException) {}
      }
    }
  }

  @Test
  fun `subscribeWithTimeout should merge events from multiple relays`() = runBlocking {
    val state1 = MockWebSocketState(listOf(createMockEvent("event1")))
    val state2 = MockWebSocketState(listOf(createMockEvent("event2")))

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("relay1") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          state1.setListener(listener)
          state1.triggerOpen()
          state1.ws
        }

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("relay2") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          state2.setListener(listener)
          state2.triggerOpen()
          state2.ws
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com"),
            RelayConfig(url = "wss://relay2.example.com"))

    val filter = """{"kinds":[39701],"limit":10}"""

    val result = relayPool.subscribeWithTimeout(relays, filter, 5000L)

    assertEquals("Should have 2 events", 2, result.size)
    assertTrue("Should contain event1", result.any { it.id == "event1" })
    assertTrue("Should contain event2", result.any { it.id == "event2" })
  }

  @Test
  fun `subscribeWithTimeout should deduplicate events by ID`() = runBlocking {
    val sameEvent = createMockEvent("same-event-id")

    val state1 = MockWebSocketState(listOf(sameEvent))
    val state2 = MockWebSocketState(listOf(sameEvent))

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("relay1") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          state1.setListener(listener)
          state1.triggerOpen()
          state1.ws
        }

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("relay2") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          state2.setListener(listener)
          state2.triggerOpen()
          state2.ws
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com"),
            RelayConfig(url = "wss://relay2.example.com"))

    val filter = """{"kinds":[39701],"limit":10}"""

    val result = relayPool.subscribeWithTimeout(relays, filter, 5000L)

    assertEquals("Should have only 1 event after deduplication", 1, result.size)
    assertEquals("Event ID should be same-event-id", "same-event-id", result.first().id)
  }

  @Test
  fun `subscribeWithTimeout with single relay should return events`() = runBlocking {
    val state = MockWebSocketState(listOf(createMockEvent("single-event")))

    every { okHttpClient.newWebSocket(any(), any()) } answers
        {
          val listener = secondArg<WebSocketListener>()
          state.setListener(listener)
          state.triggerOpen()
          state.ws
        }

    val relays = listOf(RelayConfig(url = "wss://relay.example.com"))
    val filter = """{"kinds":[39701],"limit":10}"""

    val result = relayPool.subscribeWithTimeout(relays, filter, 5000L)

    assertEquals("Should have 1 event", 1, result.size)
    assertEquals("Event ID should be single-event", "single-event", result.first().id)
  }

  @Test
  fun `subscribeWithTimeout when all relays fail should return empty list`() = runBlocking {
    every { okHttpClient.newWebSocket(any(), any()) } answers
        {
          val listener = secondArg<WebSocketListener>()
          val ws = mockk<WebSocket>(relaxed = true)
          every { ws.close(any(), any()) } returns true

          listener.onFailure(ws, RuntimeException("Connection failed"), null)
          ws
        }

    val relays = listOf(RelayConfig(url = "wss://relay.example.com"))
    val filter = """{"kinds":[39701],"limit":10}"""

    val result = relayPool.subscribeWithTimeout(relays, filter, 5000L)

    assertTrue("Should return empty list when all relays fail", result.isEmpty())
  }

  @Test
  fun `subscribeWithTimeout with empty relay list should return empty list`() = runBlocking {
    val result = relayPool.subscribeWithTimeout(emptyList(), """{"kinds":[39701]}""", 5000L)

    assertTrue("Should return empty list for empty relay list", result.isEmpty())
  }

  @Test
  fun `subscribeWithTimeout should continue on individual relay failure`() = runBlocking {
    val workingState = MockWebSocketState(listOf(createMockEvent("working-event")))

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("failing") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          val ws = mockk<WebSocket>(relaxed = true)
          every { ws.close(any(), any()) } returns true

          listener.onFailure(ws, RuntimeException("Connection failed"), null)
          ws
        }

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("working") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          workingState.setListener(listener)
          workingState.triggerOpen()
          workingState.ws
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://failing.example.com"),
            RelayConfig(url = "wss://working.example.com"))

    val filter = """{"kinds":[39701],"limit":10}"""

    val result = relayPool.subscribeWithTimeout(relays, filter, 5000L)

    assertEquals("Should have 1 event from working relay", 1, result.size)
    assertEquals("Event should be from working relay", "working-event", result.first().id)
  }

  /**
   * Helper class to manage WebSocket mock state for publish operations.
   *
   * Simulates OK responses from relays for EVENT messages.
   */
  private inner class MockPublishWebSocketState(
      private val accepted: Boolean,
      private val message: String = ""
  ) {
    val ws: WebSocket = mockk(relaxed = true)
    private val closed = java.util.concurrent.atomic.AtomicBoolean(false)

    @Volatile private var wsListener: WebSocketListener? = null

    init {
      every { ws.send(any<String>()) } answers
          {
            val msg = firstArg<String>()
            val currentListener = wsListener

            if (currentListener != null && msg.startsWith("[\"EVENT\"")) {
              val jsonArray = Json.parseToJsonElement(msg).jsonArray
              val eventId = jsonArray[1].jsonObject["id"]?.jsonPrimitive?.content ?: ""

              executor.execute {
                try {
                  Thread.sleep(50)
                  if (!closed.get()) {
                    val okResponse = """["OK","$eventId",$accepted,"$message"]"""
                    currentListener.onMessage(ws, okResponse)
                  }
                } catch (_: InterruptedException) {}
              }
            }
            true
          }

      every { ws.close(any(), any()) } answers
          {
            closed.set(true)
            val code = firstArg<Int>()
            val reason = secondArg<String?>() ?: ""
            val currentListener = wsListener
            if (currentListener != null) {
              executor.execute {
                try {
                  Thread.sleep(30)
                  currentListener.onClosed(ws, code, reason)
                } catch (_: InterruptedException) {}
              }
            }
            true
          }
    }

    fun setListener(l: WebSocketListener) {
      wsListener = l
    }

    fun triggerOpen() {
      executor.execute {
        try {
          Thread.sleep(30)
          val mockResponse = mockk<Response>(relaxed = true)
          wsListener?.onOpen(ws, mockResponse)
        } catch (_: InterruptedException) {}
      }
    }
  }

  @Test
  fun `publishEvent should return failure when no write-enabled relays`() = runBlocking {
    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com", write = false),
            RelayConfig(url = "wss://relay2.example.com", write = false))

    val signedEventJson = """{"id":"test123","pubkey":"abc","kind":39701,"content":"test"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should return failure", result.isFailure)
    assertTrue(
        "Error should mention no write-enabled relays",
        result.exceptionOrNull()?.message?.contains("No write-enabled relays") == true)
  }

  @Test
  fun `publishEvent should return failure when signedEventJson is invalid JSON`() = runBlocking {
    val relays = listOf(RelayConfig(url = "wss://relay.example.com", write = true))

    val invalidJson = "not valid json"

    val result = relayPool.publishEvent(relays, invalidJson, 5000L)

    assertTrue("Should return failure", result.isFailure)
    assertTrue(
        "Error should mention invalid JSON",
        result.exceptionOrNull()?.message?.contains("Invalid JSON") == true)
  }

  @Test
  fun `publishEvent should return success when relay accepts`() = runBlocking {
    val publishState = MockPublishWebSocketState(accepted = true)

    every { okHttpClient.newWebSocket(any(), any()) } answers
        {
          val listener = secondArg<WebSocketListener>()
          publishState.setListener(listener)
          publishState.triggerOpen()
          publishState.ws
        }

    val relays = listOf(RelayConfig(url = "wss://relay.example.com", write = true))
    val signedEventJson =
        """{"id":"event123","pubkey":"abc","created_at":1234567890,"kind":39701,"tags":[],"content":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should return success", result.isSuccess)
    val publishResult = result.getOrNull()
    assertEquals("Event ID should match", "event123", publishResult?.eventId)
    assertEquals(
        "Should have 1 successful relay",
        listOf("wss://relay.example.com"),
        publishResult?.successfulRelays)
    assertTrue("Should have no failed relays", publishResult?.failedRelays?.isEmpty() == true)
  }

  @Test
  fun `publishEvent should return failure when all relays reject`() = runBlocking {
    val publishState = MockPublishWebSocketState(accepted = false, message = "blocked: spam")

    every { okHttpClient.newWebSocket(any(), any()) } answers
        {
          val listener = secondArg<WebSocketListener>()
          publishState.setListener(listener)
          publishState.triggerOpen()
          publishState.ws
        }

    val relays = listOf(RelayConfig(url = "wss://relay.example.com", write = true))
    val signedEventJson =
        """{"id":"event123","pubkey":"abc","created_at":1234567890,"kind":39701,"tags":[],"content":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should return failure when all relays reject", result.isFailure)
    assertTrue(
        "Error should mention failed to publish",
        result.exceptionOrNull()?.message?.contains("Failed to publish") == true)
  }

  @Test
  fun `publishEvent should filter only write-enabled relays`() = runBlocking {
    val publishState = MockPublishWebSocketState(accepted = true)

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("write-relay") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          publishState.setListener(listener)
          publishState.triggerOpen()
          publishState.ws
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://read-only-relay.example.com", write = false),
            RelayConfig(url = "wss://write-relay.example.com", write = true))

    val signedEventJson =
        """{"id":"event123","pubkey":"abc","created_at":1234567890,"kind":39701,"tags":[],"content":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should return success", result.isSuccess)
    val publishResult = result.getOrNull()
    assertEquals(
        "Should only have write-enabled relay in success list",
        listOf("wss://write-relay.example.com"),
        publishResult?.successfulRelays)
  }

  @Test
  fun `publishEvent should handle mixed success and failure from multiple relays`() = runBlocking {
    val acceptingState = MockPublishWebSocketState(accepted = true)
    val rejectingState = MockPublishWebSocketState(accepted = false, message = "duplicate")

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("accepting") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          acceptingState.setListener(listener)
          acceptingState.triggerOpen()
          acceptingState.ws
        }

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("rejecting") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          rejectingState.setListener(listener)
          rejectingState.triggerOpen()
          rejectingState.ws
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://accepting.example.com", write = true),
            RelayConfig(url = "wss://rejecting.example.com", write = true))

    val signedEventJson =
        """{"id":"event123","pubkey":"abc","created_at":1234567890,"kind":39701,"tags":[],"content":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should return success when at least one relay accepts", result.isSuccess)
    val publishResult = result.getOrNull()
    assertEquals("Should have 1 successful relay", 1, publishResult?.successfulRelays?.size)
    assertEquals("Should have 1 failed relay", 1, publishResult?.failedRelays?.size)
    assertTrue(
        "Successful relay should be accepting",
        publishResult?.successfulRelays?.contains("wss://accepting.example.com") == true)
    assertTrue(
        "Failed relay should be rejecting",
        publishResult?.failedRelays?.any { it.first == "wss://rejecting.example.com" } == true)
  }
}
