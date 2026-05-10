package io.github.omochice.pinosu.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

/** Unit tests for [NostrEvent] serialization with sig field */
class NostrEventTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `deserialization parses sig from JSON`() {
    val jsonString =
        """{"id":"abc","pubkey":"def","created_at":1000,"kind":1,"tags":[],"content":"hello","sig":"sigvalue"}"""

    val event = json.decodeFromString<NostrEvent>(jsonString)

    assertEquals("sigvalue", event.sig, "sig should be parsed from JSON")
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
        true,
        serialized.contains(""""sig":"sigvalue""""),
        "sig should be present in serialized JSON")
  }
}
