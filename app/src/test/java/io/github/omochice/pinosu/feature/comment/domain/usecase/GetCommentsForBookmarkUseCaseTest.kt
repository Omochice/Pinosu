package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.feature.comment.data.repository.CommentRepository
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test class for GetCommentsForBookmarkUseCaseImpl
 *
 * Test scenarios:
 * 1. Prepends author content as first comment when non-empty
 * 2. Returns only relay comments when author content is empty
 * 3. Sorts relay comments by createdAt ascending
 */
class GetCommentsForBookmarkUseCaseTest {

  private lateinit var commentRepository: CommentRepository
  private lateinit var useCase: GetCommentsForBookmarkUseCase

  @Before
  fun setup() {
    commentRepository = mockk()
    useCase = GetCommentsForBookmarkUseCaseImpl(commentRepository)
  }

  @Test
  fun `invoke prepends author content as first comment when non-empty`() = runTest {
    val relayComment =
        Comment(
            id = "relay-1",
            content = "Great bookmark!",
            authorPubkey = "other-pubkey",
            createdAt = 1_700_000_100L,
            isAuthorComment = false)

    coEvery { commentRepository.getCommentsForBookmark("root-pubkey", "d-tag", "event-id") } returns
        Result.success(listOf(relayComment))

    val result =
        useCase(
            rootPubkey = "root-pubkey",
            dTag = "d-tag",
            rootEventId = "event-id",
            authorContent = "My bookmark note",
            authorCreatedAt = 1_700_000_000L)

    assertTrue(result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals(2, comments.size)
    assertTrue(comments[0].isAuthorComment)
    assertEquals("My bookmark note", comments[0].content)
    assertFalse(comments[1].isAuthorComment)
  }

  @Test
  fun `invoke returns only relay comments when author content is empty`() = runTest {
    val relayComment =
        Comment(
            id = "relay-1",
            content = "A comment",
            authorPubkey = "other-pubkey",
            createdAt = 1_700_000_100L,
            isAuthorComment = false)

    coEvery { commentRepository.getCommentsForBookmark("root-pubkey", "d-tag", "event-id") } returns
        Result.success(listOf(relayComment))

    val result =
        useCase(
            rootPubkey = "root-pubkey",
            dTag = "d-tag",
            rootEventId = "event-id",
            authorContent = "",
            authorCreatedAt = 1_700_000_000L)

    assertTrue(result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals(1, comments.size)
    assertFalse(comments[0].isAuthorComment)
  }

  @Test
  fun `invoke sorts relay comments by createdAt ascending`() = runTest {
    val newer =
        Comment(
            id = "newer",
            content = "Newer",
            authorPubkey = "p1",
            createdAt = 1_700_000_200L,
            isAuthorComment = false)
    val older =
        Comment(
            id = "older",
            content = "Older",
            authorPubkey = "p2",
            createdAt = 1_700_000_100L,
            isAuthorComment = false)

    coEvery { commentRepository.getCommentsForBookmark("root-pubkey", "d-tag", "event-id") } returns
        Result.success(listOf(newer, older))

    val result =
        useCase(
            rootPubkey = "root-pubkey",
            dTag = "d-tag",
            rootEventId = "event-id",
            authorContent = "",
            authorCreatedAt = 1_700_000_000L)

    assertTrue(result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals(2, comments.size)
    assertEquals("older", comments[0].id)
    assertEquals("newer", comments[1].id)
  }
}
