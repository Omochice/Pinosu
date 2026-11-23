package io.github.omochice.pinosu.core.relay

import io.github.omochice.pinosu.core.model.NostrEvent
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit tests for [RelayPoolImpl.queryEachRelay] */
@RunWith(RobolectricTestRunner::class)
class RelayPoolQueryEachRelayTest {
  private lateinit var okHttpClient: OkHttpClient
  private lateinit var relayPool: RelayPoolImpl
  private val executor = Executors.newSingleThreadScheduledExecutor()
  private val filters = listOf("""{"kinds":[30078],"limit":1}""")

  @BeforeTest
  fun setup() {
    okHttpClient = mockk(relaxed = true)
    relayPool = RelayPoolImpl(okHttpClient)
  }

  @AfterTest
  fun tearDown() {
    executor.shutdown()
  }

  private fun createMockEvent(id: String): NostrEvent {
    return NostrEvent(
        id = id,
        pubkey = "testpubkey",
        createdAt = 1_700_000_000L,
        kind = 1,
        tags = listOf(listOf("d", "test")),
        content = "",
        sig = "dummy-sig")
  }

  private fun eventMessage(subscriptionId: String, event: NostrEvent): String {
    val eventJson = Json.encodeToString(NostrEvent.serializer(), event)
    return """["EVENT","$subscriptionId",$eventJson]"""
  }

  /**
   * WebSocket mock that answers a REQ with relay messages built from the subscription ID.
   *
   * When [closeAfterScript] is set, the relay drops the connection after the last message instead
   * of waiting for the client to close it.
   */
  private inner class ScriptedWebSocketState(
      private val closeAfterScript: Boolean = false,
      private val script: (subscriptionId: String) -> List<String>,
  ) {
    val ws: WebSocket = mockk(relaxed = true)
    val sentMessages = CopyOnWriteArrayList<String>()
    private val closed = AtomicBoolean(false)

    @Volatile private var wsListener: WebSocketListener? = null

    init {
      every { ws.send(any<String>()) } answers
          {
            val message = firstArg<String>()
            sentMessages.add(message)
            val currentListener = wsListener
            if (currentListener != null && message.startsWith("[\"REQ\"")) {
              val subscriptionId = extractSubscriptionId(message)
              executor.execute {
                Thread.sleep(50)
                for (reply in script(subscriptionId)) {
                  if (!closed.get()) {
                    currentListener.onMessage(ws, reply)
                    Thread.sleep(10)
                  }
                }
                if (closeAfterScript && !closed.get()) {
                  currentListener.onClosing(ws, 1001, "going away")
                }
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
                Thread.sleep(30)
                currentListener.onClosed(ws, code, reason)
              }
            }
            true
          }
    }

    private fun extractSubscriptionId(reqMessage: String): String {
      val regex = """\["REQ","([^"]+)".*\]""".toRegex()
      return regex.find(reqMessage)?.groupValues?.get(1) ?: "unknown"
    }

    fun connect(listener: WebSocketListener): WebSocket {
      wsListener = listener
      executor.execute {
        Thread.sleep(30)
        listener.onOpen(ws, mockk<Response>(relaxed = true))
      }
      return ws
    }
  }

  private fun stubRelay(urlPart: String, state: ScriptedWebSocketState) {
    every {
      okHttpClient.newWebSocket(match { it.url.toString().contains(urlPart) }, any())
    } answers { state.connect(secondArg()) }
  }

  @Test
  fun `EOSE after events should yield Eose with those events`() = runBlocking {
    val event1 = createMockEvent("event1")
    val event2 = createMockEvent("event2")
    stubRelay(
        "relay1",
        ScriptedWebSocketState { subId ->
          listOf(eventMessage(subId, event1), eventMessage(subId, event2), """["EOSE","$subId"]""")
        })

    val results =
        relayPool.queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)

    assertEquals(1, results.size)
    val result = results.single()
    assertEquals("wss://relay1.example.com", result.relayUrl)
    assertEquals(listOf(event1, event2), result.events)
    assertEquals(RelayTermination.Eose, result.termination)
  }

  @Test
  fun `EOSE without events should yield Eose with no events`() = runBlocking {
    stubRelay("relay1", ScriptedWebSocketState { subId -> listOf("""["EOSE","$subId"]""") })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    assertTrue(result.events.isEmpty())
    assertEquals(RelayTermination.Eose, result.termination)
  }

  @Test
  fun `CLOSED with auth-required prefix should yield an access-denied Closed`() = runBlocking {
    stubRelay(
        "relay1",
        ScriptedWebSocketState { subId ->
          listOf("""["CLOSED","$subId","auth-required: we only serve authenticated users"]""")
        })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    val termination = assertIs<RelayTermination.Closed>(result.termination)
    assertEquals("auth-required: we only serve authenticated users", termination.message)
    assertTrue(termination.isAccessDenied)
  }

  @Test
  fun `CLOSED with restricted prefix should yield an access-denied Closed`() = runBlocking {
    stubRelay(
        "relay1",
        ScriptedWebSocketState { subId ->
          listOf("""["CLOSED","$subId","restricted: you are not a member"]""")
        })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    val termination = assertIs<RelayTermination.Closed>(result.termination)
    assertTrue(termination.isAccessDenied)
  }

  @Test
  fun `CLOSED with another prefix should yield a Closed that is not access-denied`() = runBlocking {
    stubRelay(
        "relay1",
        ScriptedWebSocketState { subId ->
          listOf("""["CLOSED","$subId","rate-limited: slow down"]""")
        })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    val termination = assertIs<RelayTermination.Closed>(result.termination)
    assertFalse(termination.isAccessDenied)
  }

  @Test
  fun `socket failure should yield Failed`() = runBlocking {
    every { okHttpClient.newWebSocket(any(), any()) } answers
        {
          val listener = secondArg<WebSocketListener>()
          val ws = mockk<WebSocket>(relaxed = true)
          listener.onFailure(ws, IOException("Connection refused"), null)
          ws
        }

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    val termination = assertIs<RelayTermination.Failed>(result.termination)
    assertEquals("Connection refused", termination.message)
    assertTrue(result.events.isEmpty())
  }

  @Test
  fun `connection closed by relay before EOSE should yield Failed`() = runBlocking {
    val event = createMockEvent("event1")
    stubRelay(
        "relay1",
        ScriptedWebSocketState(closeAfterScript = true) { subId ->
          listOf(eventMessage(subId, event))
        })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    assertIs<RelayTermination.Failed>(result.termination)
    assertEquals(listOf(event), result.events)
  }

  @Test
  fun `unparsable relay message should yield Failed`() =
      runBlocking<Unit> {
        stubRelay("relay1", ScriptedWebSocketState { listOf("""["EVENT"]""") })

        val result =
            relayPool
                .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
                .single()

        assertIs<RelayTermination.Failed>(result.termination)
      }

  @Test
  fun `malformed relay URL should yield Failed`() = runBlocking {
    val result = relayPool.queryEachRelay(listOf(RelayConfig("not a url")), filters, 5000L).single()

    assertEquals("not a url", result.relayUrl)
    assertIs<RelayTermination.Failed>(result.termination)
    assertTrue(result.events.isEmpty())
  }

  @Test
  fun `timeout should yield TimedOut and keep events received before it`() = runBlocking {
    val event = createMockEvent("event1")
    stubRelay("relay1", ScriptedWebSocketState { subId -> listOf(eventMessage(subId, event)) })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 1000L)
            .single()

    assertEquals(RelayTermination.TimedOut, result.termination)
    assertEquals(listOf(event), result.events)
  }

  @Test
  fun `AUTH challenge should be ignored and not end the query`() = runBlocking {
    val event = createMockEvent("event1")
    val state = ScriptedWebSocketState { subId ->
      listOf("""["AUTH","challenge-string"]""", eventMessage(subId, event), """["EOSE","$subId"]""")
    }
    stubRelay("relay1", state)

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    assertEquals(RelayTermination.Eose, result.termination)
    assertEquals(listOf(event), result.events)
    assertTrue(state.sentMessages.none { it.startsWith("""["AUTH"""") })
  }

  @Test
  fun `duplicate events from one relay should be returned once`() = runBlocking {
    val event = createMockEvent("event1")
    stubRelay(
        "relay1",
        ScriptedWebSocketState { subId ->
          listOf(eventMessage(subId, event), eventMessage(subId, event), """["EOSE","$subId"]""")
        })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    assertEquals(listOf(event), result.events)
  }

  @Test
  fun `results should be one per relay in input order without merging events`() =
      runBlocking<Unit> {
        val sharedEvent = createMockEvent("shared")
        stubRelay(
            "relay1",
            ScriptedWebSocketState { subId ->
              listOf(eventMessage(subId, sharedEvent), """["EOSE","$subId"]""")
            })
        stubRelay(
            "relay2",
            ScriptedWebSocketState { subId ->
              listOf(eventMessage(subId, sharedEvent), """["EOSE","$subId"]""")
            })
        stubRelay(
            "relay3",
            ScriptedWebSocketState { subId -> listOf("""["CLOSED","$subId","restricted: no"]""") })

        val results =
            relayPool.queryEachRelay(
                listOf(
                    RelayConfig("wss://relay1.example.com"),
                    RelayConfig("wss://relay2.example.com"),
                    RelayConfig("wss://relay3.example.com")),
                filters,
                5000L)

        assertEquals(
            listOf(
                "wss://relay1.example.com", "wss://relay2.example.com", "wss://relay3.example.com"),
            results.map { it.relayUrl })
        assertEquals(listOf(sharedEvent), results[0].events)
        assertEquals(listOf(sharedEvent), results[1].events)
        assertTrue(results[2].events.isEmpty())
        assertIs<RelayTermination.Closed>(results[2].termination)
      }

  @Test
  fun `empty relay list should yield an empty result`() = runBlocking {
    val results = relayPool.queryEachRelay(emptyList(), filters, 5000L)

    assertTrue(results.isEmpty())
  }

  @Test
  fun `empty filter list should be rejected`() {
    assertFailsWith<IllegalArgumentException> {
      runBlocking {
        relayPool.queryEachRelay(
            listOf(RelayConfig("wss://relay1.example.com")), emptyList(), 5000L)
      }
    }
  }
}
