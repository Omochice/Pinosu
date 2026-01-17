package io.github.omochice.pinosu.data.model

import org.junit.Assert.*
import org.junit.Test

class UnsignedNostrEventTest {

  companion object {
    private const val TEST_PUBKEY =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    private const val TEST_CREATED_AT = 1234567890L
    private const val TEST_KIND = 39701
    private const val TEST_CONTENT = "test content"
  }

  @Test
  fun `calculateId returns 64-character hex string`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = TEST_CONTENT)

    val id = event.calculateId()

    assertEquals("ID should be 64 characters", 64, id.length)
    assertTrue("ID should be hex string", id.all { it in '0'..'9' || it in 'a'..'f' })
  }

  @Test
  fun `calculateId returns consistent hash for same event`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = TEST_CONTENT)

    val id1 = event.calculateId()
    val id2 = event.calculateId()

    assertEquals("Same event should produce same ID", id1, id2)
  }

  @Test
  fun `calculateId returns different hash for different events`() {
    val event1 =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = "content1")

    val event2 =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = "content2")

    assertNotEquals(
        "Different events should produce different IDs", event1.calculateId(), event2.calculateId())
  }

  @Test
  fun `toJsonForSigning produces valid NIP-01 format array`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = TEST_CONTENT)

    val json = event.toJsonForSigning()

    assertTrue("JSON should start with [0,", json.startsWith("[0,"))
    assertTrue("JSON should contain pubkey", json.contains("\"$TEST_PUBKEY\""))
    assertTrue("JSON should contain createdAt", json.contains(TEST_CREATED_AT.toString()))
    assertTrue("JSON should contain kind", json.contains(TEST_KIND.toString()))
    assertTrue("JSON should contain content", json.contains("\"$TEST_CONTENT\""))
  }

  @Test
  fun `toJsonForSigning handles empty tags list`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = TEST_CONTENT)

    val json = event.toJsonForSigning()

    assertTrue("JSON should contain empty tags array", json.contains(",[]"))
  }

  @Test
  fun `toJsonForSigning handles single tag`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = listOf(listOf("d", "example.com")),
            content = TEST_CONTENT)

    val json = event.toJsonForSigning()

    assertTrue("JSON should contain tag", json.contains("""[["d","example.com"]]"""))
  }

  @Test
  fun `toJsonForSigning handles multiple tags`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags =
                listOf(
                    listOf("d", "example.com"),
                    listOf("r", "https://example.com"),
                    listOf("title", "Test Title")),
            content = TEST_CONTENT)

    val json = event.toJsonForSigning()

    assertTrue("JSON should contain d tag", json.contains("""["d","example.com"]"""))
    assertTrue("JSON should contain r tag", json.contains("""["r","https://example.com"]"""))
    assertTrue("JSON should contain title tag", json.contains("""["title","Test Title"]"""))
  }

  @Test
  fun `toJsonForSigning escapes backslash in content`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = """path\to\file""")

    val json = event.toJsonForSigning()

    assertTrue("JSON should escape backslash", json.contains("""path\\to\\file"""))
  }

  @Test
  fun `toJsonForSigning escapes double quote in content`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = """He said "hello"""")

    val json = event.toJsonForSigning()

    assertTrue("JSON should escape double quote", json.contains("""He said \"hello\""""))
  }

  @Test
  fun `toSignedEventJson creates valid signed event JSON`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = listOf(listOf("d", "example.com")),
            content = TEST_CONTENT)
    val signature =
        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"

    val json = event.toSignedEventJson(signature)

    assertTrue("JSON should contain id", json.contains(""""id":""""))
    assertTrue("JSON should contain pubkey", json.contains(""""pubkey":"$TEST_PUBKEY""""))
    assertTrue("JSON should contain created_at", json.contains(""""created_at":$TEST_CREATED_AT"""))
    assertTrue("JSON should contain kind", json.contains(""""kind":$TEST_KIND"""))
    assertTrue("JSON should contain tags", json.contains(""""tags":[["""))
    assertTrue("JSON should contain content", json.contains(""""content":"$TEST_CONTENT""""))
    assertTrue("JSON should contain sig", json.contains(""""sig":"$signature""""))
  }

  @Test
  fun `toSignedEventJson uses correct event ID`() {
    val event =
        UnsignedNostrEvent(
            pubkey = TEST_PUBKEY,
            createdAt = TEST_CREATED_AT,
            kind = TEST_KIND,
            tags = emptyList(),
            content = TEST_CONTENT)
    val signature =
        "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"

    val expectedId = event.calculateId()
    val json = event.toSignedEventJson(signature)

    assertTrue("JSON should contain calculated ID", json.contains(""""id":"$expectedId""""))
  }
}
