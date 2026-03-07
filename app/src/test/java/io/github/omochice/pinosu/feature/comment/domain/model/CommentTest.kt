package io.github.omochice.pinosu.feature.comment.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test class for Comment domain model
 *
 * Test scenarios:
 * 1. Comment holds all required fields
 * 2. kind field preserves the Nostr event kind value
 */
class CommentTest {

  @Test
  fun `Comment holds all required fields`() {
    val comment =
        Comment(
            id = "abc123",
            content = "This is a comment",
            authorPubkey = "pubkey123",
            createdAt = 1_700_000_000L)

    assertEquals("abc123", comment.id)
    assertEquals("This is a comment", comment.content)
    assertEquals("pubkey123", comment.authorPubkey)
    assertEquals(1_700_000_000L, comment.createdAt)
  }

  @Test
  fun `kind field preserves Nostr event kind value`() {
    val kind1Comment =
        Comment(
            id = "text-note-1",
            content = "A text note",
            authorPubkey = "pubkey",
            createdAt = 1_700_000_000L,
            kind = 1)

    assertEquals(1, kind1Comment.kind)

    val kind1111Comment =
        Comment(
            id = "comment-1",
            content = "A NIP-22 comment",
            authorPubkey = "pubkey",
            createdAt = 1_700_000_000L,
            kind = 1111)

    assertEquals(1111, kind1111Comment.kind)
  }
}
