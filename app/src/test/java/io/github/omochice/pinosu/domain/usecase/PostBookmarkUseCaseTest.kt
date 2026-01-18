package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PostBookmarkUseCaseImpl
 *
 * Tests cover:
 * - Unsigned event creation (success, URL validation, repository delegation)
 * - Signed event publishing (success, failure propagation)
 */
class PostBookmarkUseCaseTest {

  private lateinit var bookmarkRepository: BookmarkRepository
  private lateinit var useCase: PostBookmarkUseCase

  @Before
  fun setup() {
    bookmarkRepository = mockk(relaxed = true)
    useCase = PostBookmarkUseCaseImpl(bookmarkRepository)
  }

  // ========== createUnsignedEvent Tests ==========

  @Test
  fun `createUnsignedEvent should delegate to repository on success`() = runTest {
    val testEvent = createTestUnsignedEvent()
    val pubkey = "testpubkey"
    val url = "https://example.com"
    val title = "Test Title"
    val categories = listOf("tech", "news")
    val comment = "Test comment"

    coEvery {
      bookmarkRepository.createBookmarkEvent(pubkey, url, title, categories, comment)
    } returns Result.success(testEvent)

    val result = useCase.createUnsignedEvent(pubkey, url, title, categories, comment)

    assertTrue("Should return success", result.isSuccess)
    assertEquals("Should return the created event", testEvent, result.getOrNull())
    coVerify { bookmarkRepository.createBookmarkEvent(pubkey, url, title, categories, comment) }
  }

  @Test
  fun `createUnsignedEvent should fail when URL is blank`() = runTest {
    val pubkey = "testpubkey"
    val url = ""
    val title = "Test Title"
    val categories = listOf("tech")
    val comment = "Test comment"

    val result = useCase.createUnsignedEvent(pubkey, url, title, categories, comment)

    assertTrue("Should return failure for blank URL", result.isFailure)
    assertTrue(
        "Should contain IllegalArgumentException",
        result.exceptionOrNull() is IllegalArgumentException)
    assertEquals(
        "Should have correct error message",
        "URL cannot be blank",
        result.exceptionOrNull()?.message)
    coVerify(exactly = 0) {
      bookmarkRepository.createBookmarkEvent(any(), any(), any(), any(), any())
    }
  }

  @Test
  fun `createUnsignedEvent should fail when URL is whitespace`() = runTest {
    val pubkey = "testpubkey"
    val url = "   "
    val title = "Test Title"
    val categories = listOf("tech")
    val comment = "Test comment"

    val result = useCase.createUnsignedEvent(pubkey, url, title, categories, comment)

    assertTrue("Should return failure for whitespace URL", result.isFailure)
    assertEquals("URL cannot be blank", result.exceptionOrNull()?.message)
  }

  @Test
  fun `createUnsignedEvent should propagate repository failure`() = runTest {
    val pubkey = "testpubkey"
    val url = "https://example.com"
    val errorMessage = "Repository error"

    coEvery { bookmarkRepository.createBookmarkEvent(any(), any(), any(), any(), any()) } returns
        Result.failure(RuntimeException(errorMessage))

    val result = useCase.createUnsignedEvent(pubkey, url, null, emptyList(), "")

    assertTrue("Should return failure when repository fails", result.isFailure)
    assertEquals("Should propagate error message", errorMessage, result.exceptionOrNull()?.message)
  }

  @Test
  fun `createUnsignedEvent with null title should delegate correctly`() = runTest {
    val testEvent = createTestUnsignedEvent()
    val pubkey = "testpubkey"
    val url = "https://example.com"
    val categories = listOf("tech")
    val comment = "Test comment"

    coEvery {
      bookmarkRepository.createBookmarkEvent(pubkey, url, null, categories, comment)
    } returns Result.success(testEvent)

    val result = useCase.createUnsignedEvent(pubkey, url, null, categories, comment)

    assertTrue("Should return success with null title", result.isSuccess)
    coVerify { bookmarkRepository.createBookmarkEvent(pubkey, url, null, categories, comment) }
  }

  @Test
  fun `createUnsignedEvent with empty categories should delegate correctly`() = runTest {
    val testEvent = createTestUnsignedEvent()
    val pubkey = "testpubkey"
    val url = "https://example.com"
    val title = "Test"

    coEvery { bookmarkRepository.createBookmarkEvent(pubkey, url, title, emptyList(), "") } returns
        Result.success(testEvent)

    val result = useCase.createUnsignedEvent(pubkey, url, title, emptyList(), "")

    assertTrue("Should return success with empty categories", result.isSuccess)
    coVerify { bookmarkRepository.createBookmarkEvent(pubkey, url, title, emptyList(), "") }
  }

  // ========== publishSignedEvent Tests ==========

  @Test
  fun `publishSignedEvent should delegate to repository on success`() = runTest {
    val signedEventJson = """{"id":"abc","sig":"xyz"}"""

    coEvery { bookmarkRepository.publishBookmark(signedEventJson) } returns Result.success(Unit)

    val result = useCase.publishSignedEvent(signedEventJson)

    assertTrue("Should return success", result.isSuccess)
    coVerify { bookmarkRepository.publishBookmark(signedEventJson) }
  }

  @Test
  fun `publishSignedEvent should propagate repository failure`() = runTest {
    val signedEventJson = """{"id":"abc","sig":"xyz"}"""
    val errorMessage = "Publish failed"

    coEvery { bookmarkRepository.publishBookmark(signedEventJson) } returns
        Result.failure(RuntimeException(errorMessage))

    val result = useCase.publishSignedEvent(signedEventJson)

    assertTrue("Should return failure when repository fails", result.isFailure)
    assertEquals("Should propagate error message", errorMessage, result.exceptionOrNull()?.message)
  }

  // ========== Helper Methods ==========

  private fun createTestUnsignedEvent(): UnsignedNostrEvent {
    return UnsignedNostrEvent(
        pubkey = "testpubkey",
        createdAt = 1234567890,
        kind = 39701,
        tags = listOf(listOf("d", "example.com"), listOf("r", "https://example.com")),
        content = "Test comment")
  }
}
