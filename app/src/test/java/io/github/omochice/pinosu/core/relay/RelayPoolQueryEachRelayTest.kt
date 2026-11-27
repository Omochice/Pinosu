package io.github.omochice.pinosu.core.relay

import io.mockk.mockk
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
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

  @Test
  fun `EOSE after events should yield Eose with those events`() = runBlocking {
    val event1 = relayTestEvent("event1")
    val event2 = relayTestEvent("event2")
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor) { subId ->
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
    okHttpClient.stubRelay(
        "relay1", ScriptedRelayWebSocket(executor) { subId -> listOf("""["EOSE","$subId"]""") })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 5000L)
            .single()

    assertTrue(result.events.isEmpty())
    assertEquals(RelayTermination.Eose, result.termination)
  }

  @Test
  fun `CLOSED with auth-required prefix should yield an access-denied Closed`() = runBlocking {
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor) { subId ->
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
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor) { subId ->
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
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor) { subId ->
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
    okHttpClient.stubFailingRelay("relay1", "Connection refused")

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
    val event = relayTestEvent("event1")
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor, closeAfterScript = true) { subId ->
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
        okHttpClient.stubRelay(
            "relay1", ScriptedRelayWebSocket(executor) { listOf("""["EVENT"]""") })

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
    val event = relayTestEvent("event1")
    okHttpClient.stubRelay(
        "relay1", ScriptedRelayWebSocket(executor) { subId -> listOf(eventMessage(subId, event)) })

    val result =
        relayPool
            .queryEachRelay(listOf(RelayConfig("wss://relay1.example.com")), filters, 1000L)
            .single()

    assertEquals(RelayTermination.TimedOut, result.termination)
    assertEquals(listOf(event), result.events)
  }

  @Test
  fun `AUTH challenge should be ignored and not end the query`() = runBlocking {
    val event = relayTestEvent("event1")
    val state =
        ScriptedRelayWebSocket(executor) { subId ->
          listOf(
              """["AUTH","challenge-string"]""",
              eventMessage(subId, event),
              """["EOSE","$subId"]""")
        }
    okHttpClient.stubRelay("relay1", state)

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
    val event = relayTestEvent("event1")
    okHttpClient.stubRelay(
        "relay1",
        ScriptedRelayWebSocket(executor) { subId ->
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
        val sharedEvent = relayTestEvent("shared")
        okHttpClient.stubRelay(
            "relay1",
            ScriptedRelayWebSocket(executor) { subId ->
              listOf(eventMessage(subId, sharedEvent), """["EOSE","$subId"]""")
            })
        okHttpClient.stubRelay(
            "relay2",
            ScriptedRelayWebSocket(executor) { subId ->
              listOf(eventMessage(subId, sharedEvent), """["EOSE","$subId"]""")
            })
        okHttpClient.stubRelay(
            "relay3",
            ScriptedRelayWebSocket(executor) { subId ->
              listOf("""["CLOSED","$subId","restricted: no"]""")
            })

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
