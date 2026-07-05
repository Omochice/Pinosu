package io.github.omochice.pinosu.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Unit tests for [UnsignedNostrEvent] JSON round-tripping used by SavedStateHandle persistence. */
class UnsignedNostrEventTest {

  @Test
  fun `fromJson reverses toJson`() {
    val event =
        UnsignedNostrEvent(
            pubkey = "abc123",
            createdAt = 1_700_000_000L,
            kind = 39_701,
            tags = listOf(listOf("d", "example.com"), listOf("t", "tech")),
            content = "a comment with \"quotes\" and , commas")

    val restored = UnsignedNostrEvent.fromJson(event.toJson())

    assertEquals(event, restored, "fromJson should reconstruct the exact event produced by toJson")
  }

  @Test
  fun `fromJson returns null for malformed json`() {
    assertNull(UnsignedNostrEvent.fromJson("this is not json"))
  }

  @Test
  fun `fromJson returns null when required fields are missing`() {
    assertNull(UnsignedNostrEvent.fromJson("""{"pubkey":"abc"}"""))
  }
}
