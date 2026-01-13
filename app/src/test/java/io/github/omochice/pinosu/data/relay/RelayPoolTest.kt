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
}
