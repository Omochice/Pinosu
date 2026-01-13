package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.CommentRepository
import io.github.omochice.pinosu.domain.model.Comment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetCommentsForBookmarkUseCase
 *
 * Tests cover:
 * - Successful delegation to CommentRepository
 * - Result propagation (success and failure)
 */
class GetCommentsForBookmarkUseCaseTest {

  private lateinit var commentRepository: CommentRepository
  private lateinit var useCase: GetCommentsForBookmarkUseCase

  @Before
  fun setup() {
    commentRepository = mockk(relaxed = true)
    useCase = GetCommentsForBookmarkUseCaseImpl(commentRepository)
  }

  @Test
  fun `invoke should delegate to CommentRepository`() = runTest {
    val eventId = "test-event-id"
    coEvery { commentRepository.getCommentsForEvent(eventId) } returns Result.success(emptyList())

    useCase(eventId)

    coVerify { commentRepository.getCommentsForEvent(eventId) }
  }

  @Test
  fun `invoke should return success with comments when repository succeeds`() = runTest {
    val eventId = "test-event-id"
    val expectedComments =
        listOf(
            Comment(
                id = "comment1",
                content = "Test comment",
                author = "author1",
                createdAt = 1700000000L,
                referencedEventId = eventId))
    coEvery { commentRepository.getCommentsForEvent(eventId) } returns
        Result.success(expectedComments)

    val result = useCase(eventId)

    assertTrue("Result should be success", result.isSuccess)
    assertEquals(expectedComments, result.getOrNull())
  }

  @Test
  fun `invoke should return empty list when repository returns empty`() = runTest {
    val eventId = "test-event-id"
    coEvery { commentRepository.getCommentsForEvent(eventId) } returns Result.success(emptyList())

    val result = useCase(eventId)

    assertTrue("Result should be success", result.isSuccess)
    assertTrue("Comments should be empty", result.getOrNull()!!.isEmpty())
  }

  @Test
  fun `invoke should propagate failure from repository`() = runTest {
    val eventId = "test-event-id"
    val exception = RuntimeException("Network error")
    coEvery { commentRepository.getCommentsForEvent(eventId) } returns Result.failure(exception)

    val result = useCase(eventId)

    assertTrue("Result should be failure", result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }
}
