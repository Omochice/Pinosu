package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.data.model.NostrEvent
import io.github.omochice.pinosu.data.relay.Nip65RelayListParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NIP-65 relay list parsing
 *
 * Tests the parsing of kind:10002 events to extract relay configurations according to NIP-65
 * specification.
 */
class RelayListRepositoryTest {

  private fun createRelayListEvent(
      pubkey: String = "testpubkey",
      tags: List<List<String>> = emptyList()
  ): NostrEvent {
    return NostrEvent(
        id = "test-event-id",
        pubkey = pubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = Nip65RelayListParser.KIND_RELAY_LIST,
        tags = tags,
        content = "")
  }

  @Test
  fun `parseRelayListFromEvent with read-write relay should return RelayConfig with both flags true`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertTrue("read should be true", result[0].read)
    assertTrue("write should be true", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with read-only relay should return RelayConfig with read true and write false`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "read")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertTrue("read should be true", result[0].read)
    assertFalse("write should be false", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with write-only relay should return RelayConfig with read false and write true`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "write")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertFalse("read should be false", result[0].read)
    assertTrue("write should be true", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with multiple relays should return all relay configs`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("r", "wss://relay1.example.com"),
                    listOf("r", "wss://relay2.example.com", "read"),
                    listOf("r", "wss://relay3.example.com", "write")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 3 relays", 3, result.size)

    val relay1 = result.find { it.url == "wss://relay1.example.com" }
    assertTrue("relay1 should be read-write", relay1?.read == true && relay1.write)

    val relay2 = result.find { it.url == "wss://relay2.example.com" }
    assertTrue("relay2 should be read-only", relay2?.read == true && relay2.write == false)

    val relay3 = result.find { it.url == "wss://relay3.example.com" }
    assertTrue("relay3 should be write-only", relay3?.read == false && relay3.write == true)
  }

  @Test
  fun `parseRelayListFromEvent with empty tags should return empty list`() {
    val event = createRelayListEvent(tags = emptyList())

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertTrue("Should return empty list", result.isEmpty())
  }

  @Test
  fun `parseRelayListFromEvent should ignore non-r tags`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("e", "event-id"),
                    listOf("p", "pubkey"),
                    listOf("r", "wss://relay.example.com"),
                    listOf("d", "identifier")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent should ignore r tags without URL`() {
    val event =
        createRelayListEvent(tags = listOf(listOf("r"), listOf("r", "wss://relay.example.com")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent should ignore invalid URLs`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("r", "not-a-valid-url"),
                    listOf("r", "wss://valid.relay.com"),
                    listOf("r", "http://http-relay.com"),
                    listOf("r", "")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 valid relay", 1, result.size)
    assertEquals("URL should be wss://valid.relay.com", "wss://valid.relay.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent with unknown marker should default to read-write`() {
    val event =
        createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "unknown")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertTrue("read should be true for unknown marker", result[0].read)
    assertTrue("write should be true for unknown marker", result[0].write)
  }
}
