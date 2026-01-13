package io.github.omochice.pinosu.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CommentTest {

  @Test
  fun `Comment should hold all properties correctly`() {
    val comment =
        Comment(
            id = "abc123",
            content = "This is a comment",
            author = "pubkey123",
            createdAt = 1700000000L,
            referencedEventId = "event456")

    assertEquals("abc123", comment.id)
    assertEquals("This is a comment", comment.content)
    assertEquals("pubkey123", comment.author)
    assertEquals(1700000000L, comment.createdAt)
    assertEquals("event456", comment.referencedEventId)
  }

  @Test
  fun `Comment equality should be based on all properties`() {
    val comment1 =
        Comment(
            id = "abc123",
            content = "Test",
            author = "author1",
            createdAt = 1700000000L,
            referencedEventId = "event1")
    val comment2 =
        Comment(
            id = "abc123",
            content = "Test",
            author = "author1",
            createdAt = 1700000000L,
            referencedEventId = "event1")
    val comment3 =
        Comment(
            id = "different",
            content = "Test",
            author = "author1",
            createdAt = 1700000000L,
            referencedEventId = "event1")

    assertEquals(comment1, comment2)
    assertEquals(comment1.hashCode(), comment2.hashCode())
    assert(comment1 != comment3)
  }

  @Test
  fun `Comment copy should create new instance with modified values`() {
    val original =
        Comment(
            id = "abc123",
            content = "Original",
            author = "author1",
            createdAt = 1700000000L,
            referencedEventId = "event1")
    val copied = original.copy(content = "Modified")

    assertEquals("abc123", copied.id)
    assertEquals("Modified", copied.content)
    assertEquals("author1", copied.author)
    assertEquals(1700000000L, copied.createdAt)
    assertEquals("event1", copied.referencedEventId)
  }
}
