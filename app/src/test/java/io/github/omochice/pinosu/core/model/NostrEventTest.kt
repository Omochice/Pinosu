package io.github.omochice.pinosu.core.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [NostrEvent] serialization with sig field */
class NostrEventTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `deserialization parses sig from JSON`() {
    val jsonString =
        """{"id":"abc","pubkey":"def","created_at":1000,"kind":1,"tags":[],"content":"hello","sig":"sigvalue"}"""

    val event = json.decodeFromString<NostrEvent>(jsonString)

    assertEquals("sig should be parsed from JSON", "sigvalue", event.sig)
  }

  @Test
  fun `serialization includes sig when present`() {
    val event =
        NostrEvent(
            id = "abc",
            pubkey = "def",
            createdAt = 1000,
            kind = 1,
            tags = emptyList(),
            content = "hello",
            sig = "sigvalue")

    val serialized = json.encodeToString(NostrEvent.serializer(), event)

    assertEquals(
        "sig should be present in serialized JSON",
        true,
        serialized.contains(""""sig":"sigvalue""""))
  }
}
