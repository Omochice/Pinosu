package io.github.omochice.pinosu.core.nip.nip65

import io.github.omochice.pinosu.core.model.NostrEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Nip65EventParserTest {

  private lateinit var parser: Nip65EventParser

  @BeforeTest
  fun setup() {
    parser = Nip65EventParserImpl()
  }

  @Test
  fun `parseRelayListEvent with no marker should return relay with both read and write true`() {
    val event = createNip65Event(listOf(listOf("r", "wss://relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return one relay")
    val relay = result.first()
    assertEquals("wss://relay.example.com", relay.url, "URL should match")
    assertTrue(relay.read, "read should be true when no marker")
    assertTrue(relay.write, "write should be true when no marker")
  }

  @Test
  fun `parseRelayListEvent with read marker should return relay with read true and write false`() {
    val event = createNip65Event(listOf(listOf("r", "wss://read-relay.example.com", "read")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return one relay")
    val relay = result.first()
    assertEquals("wss://read-relay.example.com", relay.url, "URL should match")
    assertTrue(relay.read, "read should be true for read marker")
    assertEquals(false, relay.write, "write should be false for read marker")
  }

  @Test
  fun `parseRelayListEvent with write marker should return relay with read false and write true`() {
    val event = createNip65Event(listOf(listOf("r", "wss://write-relay.example.com", "write")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return one relay")
    val relay = result.first()
    assertEquals("wss://write-relay.example.com", relay.url, "URL should match")
    assertEquals(false, relay.read, "read should be false for write marker")
    assertTrue(relay.write, "write should be true for write marker")
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

    assertEquals(3, result.size, "Should return three relays")

    val relay1 = result.find { it.url == "wss://relay1.example.com" }!!
    assertTrue(relay1.read, "relay1 should have read=true")
    assertTrue(relay1.write, "relay1 should have write=true")

    val relay2 = result.find { it.url == "wss://relay2.example.com" }!!
    assertTrue(relay2.read, "relay2 should have read=true")
    assertEquals(false, relay2.write, "relay2 should have write=false")

    val relay3 = result.find { it.url == "wss://relay3.example.com" }!!
    assertEquals(false, relay3.read, "relay3 should have read=false")
    assertTrue(relay3.write, "relay3 should have write=true")
  }

  @Test
  fun `parseRelayListEvent with empty tags should return empty list`() {
    val event = createNip65Event(emptyList())

    val result = parser.parseRelayListEvent(event)

    assertTrue(result.isEmpty(), "Should return empty list for empty tags")
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

    assertEquals(1, result.size, "Should return only r tag relay")
    assertEquals("wss://relay.example.com", result.first().url, "URL should match r tag")
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

    assertTrue(result.isEmpty(), "Should return empty list for wrong kind")
  }

  @Test
  fun `parseRelayListEvent with malformed r tag missing url should skip it`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r"), // Missing URL
                listOf("r", "wss://valid-relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return only valid relay")
    assertEquals("wss://valid-relay.example.com", result.first().url, "URL should be the valid one")
  }

  @Test
  fun `parseRelayListEvent with invalid url scheme should skip it`() {
    val event =
        createNip65Event(
            listOf(
                listOf("r", "http://not-websocket.example.com"),
                listOf("r", "wss://valid-relay.example.com")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return only valid wss relay")
    assertEquals(
        "wss://valid-relay.example.com", result.first().url, "URL should be the valid wss one")
  }

  @Test
  fun `parseRelayListEvent with unknown marker should treat as both read and write`() {
    val event = createNip65Event(listOf(listOf("r", "wss://relay.example.com", "unknown")))

    val result = parser.parseRelayListEvent(event)

    assertEquals(1, result.size, "Should return one relay")
    val relay = result.first()
    assertTrue(relay.read, "read should be true for unknown marker")
    assertTrue(relay.write, "write should be true for unknown marker")
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
