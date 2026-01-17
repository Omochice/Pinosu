package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.model.UnsignedNostrEvent
import io.github.omochice.pinosu.data.relay.PublishResult
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test class for PostBookmarkUseCaseImpl
 *
 * Test scenarios:
 * 1. Failure: User not logged in
 * 2. Failure: Invalid npub format
 * 3. Success: Create unsigned event with valid user
 * 4. Verify: publishSignedEvent delegates to repository
 */
class PostBookmarkUseCaseTest {

  private lateinit var bookmarkRepository: BookmarkRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var postBookmarkUseCase: PostBookmarkUseCase

  @Before
  fun setup() {
    bookmarkRepository = mockk()
    getLoginStateUseCase = mockk()
    postBookmarkUseCase = PostBookmarkUseCaseImpl(bookmarkRepository, getLoginStateUseCase)
  }

  @Test
  fun `createUnsignedEvent returns failure when user not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    val result =
        postBookmarkUseCase.createUnsignedEvent(
            url = "example.com/article",
            title = "Test Article",
            categories = listOf("tech"),
            comment = "Great article")

    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception is IllegalStateException)
    assertEquals("User not logged in", exception?.message)
  }

  @Test
  fun `createUnsignedEvent returns failure when npub format is invalid`() = runTest {
    val invalidNpub = "invalid_npub_format"
    coEvery { getLoginStateUseCase() } returns User(invalidNpub)

    val result =
        postBookmarkUseCase.createUnsignedEvent(
            url = "example.com/article",
            title = "Test Article",
            categories = listOf("tech"),
            comment = "Great article")

    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
    assertTrue(exception is IllegalArgumentException)
    assertEquals("Invalid npub format", exception?.message)
  }

  @Test
  fun `createUnsignedEvent returns success with valid user`() = runTest {
    val validNpub = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqsutg0x"
    val hexPubkey = "00000000000000000000000000000000000000000000000000000000000000e1"
    val expectedEvent =
        UnsignedNostrEvent(
            pubkey = hexPubkey,
            createdAt = 1234567890L,
            kind = 39701,
            tags = listOf(listOf("r", "https://example.com/article")),
            content = "Great article")

    coEvery { getLoginStateUseCase() } returns User(validNpub)
    coEvery {
      bookmarkRepository.createBookmarkEvent(
          hexPubkey = hexPubkey,
          url = "example.com/article",
          title = "Test Article",
          categories = listOf("tech"),
          comment = "Great article")
    } returns expectedEvent

    val result =
        postBookmarkUseCase.createUnsignedEvent(
            url = "example.com/article",
            title = "Test Article",
            categories = listOf("tech"),
            comment = "Great article")

    assertTrue(result.isSuccess)
    assertEquals(expectedEvent, result.getOrNull())
    coVerify(exactly = 1) { getLoginStateUseCase() }
    coVerify(exactly = 1) {
      bookmarkRepository.createBookmarkEvent(
          hexPubkey = hexPubkey,
          url = "example.com/article",
          title = "Test Article",
          categories = listOf("tech"),
          comment = "Great article")
    }
  }

  @Test
  fun `publishSignedEvent delegates to repository`() = runTest {
    val signedEventJson = """{"id":"abc123","sig":"def456"}"""
    val expectedResult =
        PublishResult(
            eventId = "abc123",
            successfulRelays = listOf("wss://relay1.example.com"),
            failedRelays = emptyList())

    coEvery { bookmarkRepository.publishBookmark(signedEventJson) } returns
        Result.success(expectedResult)

    val result = postBookmarkUseCase.publishSignedEvent(signedEventJson)

    assertTrue(result.isSuccess)
    assertEquals(expectedResult, result.getOrNull())
    coVerify(exactly = 1) { bookmarkRepository.publishBookmark(signedEventJson) }
  }

  @Test
  fun `publishSignedEvent returns failure when repository fails`() = runTest {
    val signedEventJson = """{"id":"abc123","sig":"def456"}"""
    val expectedException = Exception("Failed to publish to any relay")

    coEvery { bookmarkRepository.publishBookmark(signedEventJson) } returns
        Result.failure(expectedException)

    val result = postBookmarkUseCase.publishSignedEvent(signedEventJson)

    assertTrue(result.isFailure)
    assertEquals(expectedException, result.exceptionOrNull())
    coVerify(exactly = 1) { bookmarkRepository.publishBookmark(signedEventJson) }
  }
}
