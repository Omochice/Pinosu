package io.github.omochice.pinosu.data.relay

import io.github.omochice.pinosu.data.model.NostrEvent
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking
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
        content = "")
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
      val tagsJson =
          event.tags.joinToString(",") { tag -> "[" + tag.joinToString(",") { "\"$it\"" } + "]" }
      return """["EVENT","$subscriptionId",{"id":"${event.id}","pubkey":"${event.pubkey}","created_at":${event.createdAt},"kind":${event.kind},"tags":[$tagsJson],"content":"${event.content}"}]"""
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

  // ========== publishEvent Tests ==========

  /**
   * Helper class to manage WebSocket mock state for EVENT publishing.
   *
   * Sends OK response after receiving EVENT message.
   */
  private inner class MockPublishWebSocketState(
      private val accepted: Boolean,
      private val errorMessage: String? = null,
      private val shouldFail: Boolean = false
  ) {
    val ws: WebSocket = mockk(relaxed = true)
    @Volatile private var wsListener: WebSocketListener? = null

    init {
      every { ws.send(any<String>()) } answers
          {
            val message = firstArg<String>()
            val currentListener = wsListener

            if (currentListener != null && message.startsWith("[\"EVENT\"")) {
              executor.execute {
                try {
                  Thread.sleep(50)
                  if (shouldFail) {
                    currentListener.onFailure(
                        ws, RuntimeException("Publish failed"), mockk(relaxed = true))
                  } else if (accepted) {
                    currentListener.onMessage(ws, """["OK","event-id",true]""")
                  } else {
                    val msg = errorMessage ?: "Event rejected"
                    currentListener.onMessage(ws, """["OK","event-id",false,"$msg"]""")
                  }
                } catch (_: InterruptedException) {}
              }
            }
            true
          }

      every { ws.close(any(), any()) } returns true
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
  fun `publishEvent should succeed when all write relays accept`() = runBlocking {
    val state1 = MockPublishWebSocketState(accepted = true)
    val state2 = MockPublishWebSocketState(accepted = true)

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
            RelayConfig(url = "wss://relay1.example.com", write = true),
            RelayConfig(url = "wss://relay2.example.com", write = true))

    val signedEventJson =
        """{"id":"test","pubkey":"abc","created_at":123,"kind":1,"tags":[],"content":"","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should succeed when all relays accept", result.isSuccess)
  }

  @Test
  fun `publishEvent should succeed with at least one relay accepting`() = runBlocking {
    val acceptingState = MockPublishWebSocketState(accepted = true)
    val rejectingState = MockPublishWebSocketState(accepted = false, errorMessage = "Duplicate")

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

    val signedEventJson = """{"id":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should succeed when at least one relay accepts", result.isSuccess)
  }

  @Test
  fun `publishEvent should fail when no write-enabled relays`() = runBlocking {
    val relays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com", write = false, read = true),
            RelayConfig(url = "wss://relay2.example.com", write = false, read = true))

    val signedEventJson = """{"id":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should fail when no write-enabled relays", result.isFailure)
    assertTrue(
        "Error message should mention write-enabled relays",
        result.exceptionOrNull()?.message?.contains("write-enabled") == true)
  }

  @Test
  fun `publishEvent should fail when all relays reject`() = runBlocking {
    val state1 = MockPublishWebSocketState(accepted = false, errorMessage = "Spam")
    val state2 = MockPublishWebSocketState(accepted = false, errorMessage = "Invalid")

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
            RelayConfig(url = "wss://relay1.example.com", write = true),
            RelayConfig(url = "wss://relay2.example.com", write = true))

    val signedEventJson = """{"id":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should fail when all relays reject", result.isFailure)
    assertTrue(
        "Error message should mention publish failure",
        result.exceptionOrNull()?.message?.contains("Failed to publish") == true)
  }

  @Test
  fun `publishEvent should fail when all relays have connection failures`() = runBlocking {
    val state1 = MockPublishWebSocketState(accepted = false, shouldFail = true)
    val state2 = MockPublishWebSocketState(accepted = false, shouldFail = true)

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
            RelayConfig(url = "wss://relay1.example.com", write = true),
            RelayConfig(url = "wss://relay2.example.com", write = true))

    val signedEventJson = """{"id":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should fail when all relays have connection failures", result.isFailure)
  }

  @Test
  fun `publishEvent should only send to write-enabled relays`() = runBlocking {
    val writeState = MockPublishWebSocketState(accepted = true)

    var readOnlyRelayAccessed = false

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("write") }, any())
    } answers
        {
          val listener = secondArg<WebSocketListener>()
          writeState.setListener(listener)
          writeState.triggerOpen()
          writeState.ws
        }

    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains("readonly") }, any())
    } answers
        {
          readOnlyRelayAccessed = true
          mockk(relaxed = true)
        }

    val relays =
        listOf(
            RelayConfig(url = "wss://write.example.com", write = true, read = true),
            RelayConfig(url = "wss://readonly.example.com", write = false, read = true))

    val signedEventJson = """{"id":"test","sig":"xyz"}"""

    val result = relayPool.publishEvent(relays, signedEventJson, 5000L)

    assertTrue("Should succeed", result.isSuccess)
    assertTrue("Should not access read-only relay", !readOnlyRelayAccessed)
  }
}
