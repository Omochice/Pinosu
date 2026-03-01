package io.github.omochice.pinosu.core.nip.nip01

import io.github.omochice.pinosu.core.model.NostrEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Test class for [Nip01ProfileParser] */
class Nip01ProfileParserTest {

  private val parser: Nip01ProfileParser = Nip01ProfileParserImpl()

  @Test
  fun `parseProfileEvent returns UserProfile with all fields from valid kind 0 event`() {
    val event =
        NostrEvent(
            id = "evt1",
            pubkey = "aabbccdd",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content =
                """{"name":"Alice","picture":"https://example.com/avatar.png","about":"Hello"}""",
            sig = "sig1")

    val profile = parser.parseProfileEvent(event)

    assertEquals("aabbccdd", profile?.pubkey)
    assertEquals("Alice", profile?.name)
    assertEquals("https://example.com/avatar.png", profile?.picture)
    assertEquals("Hello", profile?.about)
  }

  @Test
  fun `parseProfileEvent returns null for non-kind-0 event`() {
    val event =
        NostrEvent(
            id = "evt2",
            pubkey = "aabbccdd",
            createdAt = 1_700_000_000L,
            kind = 1,
            tags = emptyList(),
            content = """{"name":"Alice","picture":"https://example.com/avatar.png"}""",
            sig = "sig2")

    assertNull(parser.parseProfileEvent(event))
  }

  @Test
  fun `parseProfileEvent returns UserProfile with null fields when JSON has missing fields`() {
    val event =
        NostrEvent(
            id = "evt3",
            pubkey = "aabbccdd",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content = """{"name":"Bob"}""",
            sig = "sig3")

    val profile = parser.parseProfileEvent(event)

    assertEquals("aabbccdd", profile?.pubkey)
    assertEquals("Bob", profile?.name)
    assertNull(profile?.picture)
    assertNull(profile?.about)
  }

  @Test
  fun `parseProfileEvent returns null when content is invalid JSON`() {
    val event =
        NostrEvent(
            id = "evt4",
            pubkey = "aabbccdd",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content = "not valid json",
            sig = "sig4")

    assertNull(parser.parseProfileEvent(event))
  }
}
