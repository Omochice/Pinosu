package io.github.omochice.pinosu.core.nip.nip65

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.relay.RelayPool
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Nip65RelayListFetcherTest {

  private lateinit var relayPool: RelayPool
  private lateinit var parser: Nip65EventParser
  private lateinit var fetcher: Nip65RelayListFetcher

  @Before
  fun setup() {
    relayPool = mockk(relaxed = true)
    parser = Nip65EventParserImpl()
    fetcher = Nip65RelayListFetcherImpl(relayPool, parser)
  }

  @Test
  fun `fetchRelayList should return parsed relay list on successful fetch`() = runTest {
    val hexPubkey = "a".repeat(64)
    val nip65Event = createNip65Event(hexPubkey, listOf(listOf("r", "wss://relay.example.com")))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(nip65Event)

    val result = fetcher.fetchRelayList(hexPubkey)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()!!
    assertEquals("Should return one relay", 1, relays.size)
    assertEquals("Relay URL should match", "wss://relay.example.com", relays.first().url)
  }

  @Test
  fun `fetchRelayList should return empty list when no events found`() = runTest {
    val hexPubkey = "a".repeat(64)
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    val result = fetcher.fetchRelayList(hexPubkey)

    assertTrue("Should return success", result.isSuccess)
    assertTrue("Should return empty list", result.getOrNull()!!.isEmpty())
  }

  @Test
  fun `fetchRelayList should use most recent event when multiple events returned`() = runTest {
    val hexPubkey = "a".repeat(64)
    val olderEvent =
        createNip65Event(
            hexPubkey, listOf(listOf("r", "wss://old-relay.example.com")), createdAt = 1000L)
    val newerEvent =
        createNip65Event(
            hexPubkey, listOf(listOf("r", "wss://new-relay.example.com")), createdAt = 2000L)
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
        listOf(olderEvent, newerEvent)

    val result = fetcher.fetchRelayList(hexPubkey)

    assertTrue("Should return success", result.isSuccess)
    val relays = result.getOrNull()!!
    assertEquals("Should return one relay from newer event", 1, relays.size)
    assertEquals(
        "Should use relay from newer event", "wss://new-relay.example.com", relays.first().url)
  }

  @Test
  fun `fetchRelayList should use correct filter for kind 10002`() = runTest {
    val hexPubkey = "abcd1234".repeat(8)
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    fetcher.fetchRelayList(hexPubkey)

    coVerify {
      relayPool.subscribeWithTimeout(
          any(),
          match { it.contains("\"kinds\":[10002]") && it.contains("\"authors\":[\"$hexPubkey\"]") },
          any())
    }
  }

  @Test
  fun `fetchRelayList should use bootstrap relay for query`() = runTest {
    val hexPubkey = "a".repeat(64)
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    fetcher.fetchRelayList(hexPubkey)

    coVerify {
      relayPool.subscribeWithTimeout(
          match { relays -> relays.any { it.url == "wss://yabu.me" } }, any(), any())
    }
  }

  @Test
  fun `fetchRelayList should return failure for invalid hex pubkey`() = runTest {
    val invalidHexPubkey = "not-a-valid-hex"

    val result = fetcher.fetchRelayList(invalidHexPubkey)

    assertTrue("Should return failure for invalid pubkey", result.isFailure)
  }

  @Test
  fun `fetchRelayList should return failure for wrong length pubkey`() = runTest {
    val shortPubkey = "abcd1234"

    val result = fetcher.fetchRelayList(shortPubkey)

    assertTrue("Should return failure for short pubkey", result.isFailure)
  }

  @Test
  fun `fetchRelayList should handle relay pool exception gracefully`() = runTest {
    val hexPubkey = "a".repeat(64)
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } throws
        IOException("Network error")

    val result = fetcher.fetchRelayList(hexPubkey)

    assertTrue("Should return failure on exception", result.isFailure)
    assertTrue(
        "Exception message should be preserved",
        result.exceptionOrNull()?.message?.contains("Network error") == true)
  }

  private fun createNip65Event(
      pubkey: String,
      tags: List<List<String>>,
      createdAt: Long = 1_234_567_890L
  ): NostrEvent {
    return NostrEvent(
        id = "test-event-id-${createdAt}",
        pubkey = pubkey,
        createdAt = createdAt,
        kind = 10002,
        tags = tags,
        content = "",
        sig = "dummy-sig")
  }
}
