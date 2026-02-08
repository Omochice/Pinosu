package io.github.omochice.pinosu.feature.bookmark.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [BookmarkItem] rawJson property */
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
}
