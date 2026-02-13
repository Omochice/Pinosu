package io.github.omochice.pinosu.core.nip.nip65

import io.github.omochice.pinosu.core.model.NostrEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Nip65EventParserTest {

  private lateinit var parser: Nip65EventParser

  @Before
  fun setup() {
    parser = Nip65EventParserImpl()
  }

  @Test
  fun `parseRelayListEvent with no marker should return relay with both read and write true`() {
    val event = createNip65Event(listOf(listOf("r", "wss://relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return one relay", 1, result.size)
    val relay = result.first()
    assertEquals("URL should match", "wss://relay.example.com", relay.url)
    assertTrue("read should be true when no marker", relay.read)
    assertTrue("write should be true when no marker", relay.write)
  }

  @Test
  fun `parseRelayListEvent with read marker should return relay with read true and write false`() {
    val event = createNip65Event(listOf(listOf("r", "wss://read-relay.example.com", "read")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return one relay", 1, result.size)
    val relay = result.first()
    assertEquals("URL should match", "wss://read-relay.example.com", relay.url)
    assertTrue("read should be true for read marker", relay.read)
    assertEquals("write should be false for read marker", false, relay.write)
  }

  @Test
  fun `parseRelayListEvent with write marker should return relay with read false and write true`() {
    val event = createNip65Event(listOf(listOf("r", "wss://write-relay.example.com", "write")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return one relay", 1, result.size)
    val relay = result.first()
    assertEquals("URL should match", "wss://write-relay.example.com", relay.url)
    assertEquals("read should be false for write marker", false, relay.read)
    assertTrue("write should be true for write marker", relay.write)
  }

  @Test
  fun `parseRelayListEvent with multiple relays should return all relays`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r", "wss://relay1.example.com"),
                listOf("r", "wss://relay2.example.com", "read"),
                listOf("r", "wss://relay3.example.com", "write")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return three relays", 3, result.size)

    val relay1 = result.find { it.url == "wss://relay1.example.com" }!!
    assertTrue("relay1 should have read=true", relay1.read)
    assertTrue("relay1 should have write=true", relay1.write)

    val relay2 = result.find { it.url == "wss://relay2.example.com" }!!
    assertTrue("relay2 should have read=true", relay2.read)
    assertEquals("relay2 should have write=false", false, relay2.write)

    val relay3 = result.find { it.url == "wss://relay3.example.com" }!!
    assertEquals("relay3 should have read=false", false, relay3.read)
    assertTrue("relay3 should have write=true", relay3.write)
  }

  @Test
  fun `parseRelayListEvent with empty tags should return empty list`() {
    val event = createNip65Event(emptyList())

    val result = parser.parseRelayListEvent(event)

    assertTrue("Should return empty list for empty tags", result.isEmpty())
  }

  @Test
  fun `parseRelayListEvent with non-r tags should ignore them`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r", "wss://relay.example.com"),
                listOf("p", "pubkey123"),
                listOf("e", "eventid456")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return only r tag relay", 1, result.size)
    assertEquals("URL should match r tag", "wss://relay.example.com", result.first().url)
  }

  @Test
  fun `parseRelayListEvent with wrong kind should return empty list`() {
    val event =
        NostrEvent(
            id = "test-id",
            pubkey = "test-pubkey",
            createdAt = 1_234_567_890L,
            kind = 1, // Wrong kind, should be 10002
            tags = listOf(listOf("r", "wss://relay.example.com")),
            content = "",
            sig = "dummy-sig")

    val result = parser.parseRelayListEvent(event)

    assertTrue("Should return empty list for wrong kind", result.isEmpty())
  }

  @Test
  fun `parseRelayListEvent with malformed r tag missing url should skip it`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r"), // Missing URL
                listOf("r", "wss://valid-relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return only valid relay", 1, result.size)
    assertEquals("URL should be the valid one", "wss://valid-relay.example.com", result.first().url)
  }

  @Test
  fun `parseRelayListEvent with invalid url scheme should skip it`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r", "http://not-websocket.example.com"),
                listOf("r", "wss://valid-relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return only valid wss relay", 1, result.size)
    assertEquals(
        "URL should be the valid wss one", "wss://valid-relay.example.com", result.first().url)
  }

  @Test
  fun `parseRelayListEvent with unknown marker should treat as both read and write`() {
    val event = createNip65Event(listOf(listOf("r", "wss://relay.example.com", "unknown")))

    val result = parser.parseRelayListEvent(event)

    assertEquals("Should return one relay", 1, result.size)
    val relay = result.first()
    assertTrue("read should be true for unknown marker", relay.read)
    assertTrue("write should be true for unknown marker", relay.write)
  }

  private fun createNip65Event(tags: List<List<String>>): NostrEvent {
    return NostrEvent(
        id = "test-event-id",
        pubkey = "test-pubkey",
        createdAt = 1_234_567_890L,
        kind = 10002,
        tags = tags,
        content = "",
        sig = "dummy-sig")
  }
}
