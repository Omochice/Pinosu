package io.github.omochice.pinosu.feature.bookmark.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Unit tests for [BookmarkItem] properties */
class BookmarkItemTest {

  @Test
  fun `rawJson defaults to null when not provided`() {
    val item = BookmarkItem(type = "event")

    assertNull(item.rawJson, "rawJson should default to null")
  }

  @Test
  fun `rawJson retains provided value`() {
    val expectedJson =
        """{"id":"abc","pubkey":"def","created_at":1000,"kind":1,"tags":[],"content":"hello"}"""
    val item = BookmarkItem(type = "event", rawJson = expectedJson)

    assertEquals(expectedJson, item.rawJson, "rawJson should retain provided value")
  }

  @Test
  fun `imageUrl defaults to null when not provided`() {
    val item = BookmarkItem(type = "event")

    assertNull(item.imageUrl, "imageUrl should default to null")
  }

  @Test
  fun `imageUrl retains provided value`() {
    val expectedUrl = "https://example.com/ogp-image.jpg"
    val item = BookmarkItem(type = "event", imageUrl = expectedUrl)

    assertEquals(expectedUrl, item.imageUrl, "imageUrl should retain provided value")
  }

  @Test
  fun `stableKey uses eventId when present`() {
    val item = BookmarkItem(type = "event", eventId = "abc123")

    assertEquals("event:abc123", item.stableKey)
  }

  @Test
  fun `stableKey falls back to hashCode when eventId is null`() {
    val item = BookmarkItem(type = "event", eventId = null)

    assertEquals("event:${item.hashCode()}", item.stableKey)
  }
}
