package io.github.omochice.pinosu.feature.comment.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test class for Comment domain model
 *
 * Test scenarios:
 * 1. Comment holds all required fields
 * 2. isAuthorComment flag distinguishes author content from relay comments
 */
class CommentTest {

  @Test
  fun `Comment holds all required fields`() {
    val comment =
        Comment(
            id = "abc123",
            content = "This is a comment",
            authorPubkey = "pubkey123",
            createdAt = 1_700_000_000L,
            isAuthorComment = false)

    assertEquals("abc123", comment.id)
    assertEquals("This is a comment", comment.content)
    assertEquals("pubkey123", comment.authorPubkey)
    assertEquals(1_700_000_000L, comment.createdAt)
    assertFalse(comment.isAuthorComment)
  }

  @Test
  fun `isAuthorComment true for bookmark author content`() {
    val authorComment =
        Comment(
            id = "author-content",
            content = "My bookmark note",
            authorPubkey = "bookmarkAuthorPubkey",
            createdAt = 1_700_000_000L,
            isAuthorComment = true)

    assertTrue(authorComment.isAuthorComment)
  }
}
