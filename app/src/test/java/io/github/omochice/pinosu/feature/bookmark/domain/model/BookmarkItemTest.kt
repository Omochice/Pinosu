package io.github.omochice.pinosu.feature.bookmark.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [BookmarkItem] properties */
class BookmarkItemTest {

  @Test
  fun `rawJson defaults to null when not provided`() {
    val item = BookmarkItem(type = "event")

    assertNull("rawJson should default to null", item.rawJson)
  }

  @Test
  fun `rawJson retains provided value`() {
    val expectedJson =
        """{"id":"abc","pubkey":"def","created_at":1000,"kind":1,"tags":[],"content":"hello"}"""
    val item = BookmarkItem(type = "event", rawJson = expectedJson)

    assertEquals("rawJson should retain provided value", expectedJson, item.rawJson)
  }

  @Test
  fun `imageUrl defaults to null when not provided`() {
    val item = BookmarkItem(type = "event")

    assertNull("imageUrl should default to null", item.imageUrl)
  }

  @Test
  fun `imageUrl retains provided value`() {
    val expectedUrl = "https://example.com/ogp-image.jpg"
    val item = BookmarkItem(type = "event", imageUrl = expectedUrl)

    assertEquals("imageUrl should retain provided value", expectedUrl, item.imageUrl)
  }
}
