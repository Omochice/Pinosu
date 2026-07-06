package io.github.omochice.pinosu.core.relay

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Unit tests for [NostrRelayMessageSerializer] OK message parsing.
 *
 * Focuses on the `accepted` flag: a well-formed boolean parses, and a malformed non-boolean flag is
 * reported as a [SerializationException] so callers that only guard against serialization errors do
 * not treat it as a transport failure.
 */
class NostrRelayMessageTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `OK message with boolean accepted flag parses`() {
    val message = json.decodeFromString<NostrRelayMessage>("""["OK","event-id",true,"stored"]""")

    val ok = message as NostrRelayMessage.Ok
    assertEquals("event-id", ok.eventId)
    assertTrue(ok.accepted, "accepted should reflect the boolean true")
    assertEquals("stored", ok.message)
  }

  @Test
  fun `OK message with non-boolean accepted flag throws SerializationException`() {
    // A value whose content is neither "true" nor "false" (here the string "yes") is what
    // previously
    // leaked an IllegalStateException from JsonPrimitive.boolean past the caller's guards.
    assertFailsWith<SerializationException> {
      json.decodeFromString<NostrRelayMessage>("""["OK","event-id","yes","stored"]""")
    }
  }

  @Test
  fun `OK message with non-primitive accepted flag throws SerializationException`() {
    assertFailsWith<SerializationException> {
      json.decodeFromString<NostrRelayMessage>("""["OK","event-id",{},"stored"]""")
    }
  }

  @Test
  fun `OK message with non-primitive event id throws SerializationException`() {
    assertFailsWith<SerializationException> {
      json.decodeFromString<NostrRelayMessage>("""["OK",{},true,"stored"]""")
    }
  }

  @Test
  fun `OK message with non-primitive message field is treated as empty`() {
    val message = json.decodeFromString<NostrRelayMessage>("""["OK","event-id",true,{}]""")

    val ok = message as NostrRelayMessage.Ok
    assertEquals("", ok.message)
  }
}
