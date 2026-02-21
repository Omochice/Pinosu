package io.github.omochice.pinosu.feature.postbookmark.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.bookmark.domain.repository.BookmarkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
  fun `createUnsignedEvent returns failure when npub has invalid checksum`() = runTest {
    val invalidChecksumNpub = "npub1" + "a".repeat(58)
    val pubkeyWithInvalidChecksum = Pubkey.parse(invalidChecksumNpub)!!
    val user = User(pubkeyWithInvalidChecksum)
    coEvery { getLoginStateUseCase() } returns user

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
  }

  @Test
  fun `createUnsignedEvent returns success with valid user`() = runTest {
    val validNpub = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq4hl3lg"
    val hexPubkey = "0000000000000000000000000000000000000000000000000000000000000000"
    val expectedEvent =
        UnsignedNostrEvent(
            pubkey = hexPubkey,
            createdAt = 1_234_567_890L,
            kind = 39701,
            tags = listOf(listOf("r", "https://example.com/article")),
            content = "Great article")

    coEvery { getLoginStateUseCase() } returns User(Pubkey.parse(validNpub)!!)
    coEvery {
      bookmarkRepository.createBookmarkEvent(
          hexPubkey = any(),
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
          hexPubkey = any(),
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
