package io.github.omochice.pinosu.feature.comment.domain.usecase

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.comment.data.repository.CommentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test class for PostCommentUseCaseImpl
 *
 * Test scenarios:
 * 1. createUnsignedEvent fails when user not logged in
 * 2. createUnsignedEvent delegates to CommentRepository
 * 3. publishSignedEvent delegates to CommentRepository
 */
class PostCommentUseCaseTest {

  private lateinit var commentRepository: CommentRepository
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var useCase: PostCommentUseCase

  @Before
  fun setup() {
    commentRepository = mockk()
    getLoginStateUseCase = mockk()
    useCase = PostCommentUseCaseImpl(commentRepository, getLoginStateUseCase)
  }

  @Test
  fun `createUnsignedEvent fails when user not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    val result =
        useCase.createUnsignedEvent(
            content = "My comment",
            rootPubkey = "root-pubkey",
            dTag = "example.com/article",
            rootEventId = "event-123")

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun `createUnsignedEvent delegates to CommentRepository`() = runTest {
    val validNpub = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq4hl3lg"
    val hexPubkey = "0000000000000000000000000000000000000000000000000000000000000000"
    val expectedEvent =
        UnsignedNostrEvent(
            pubkey = hexPubkey,
            createdAt = 1_700_000_000L,
            kind = 1111,
            tags = emptyList(),
            content = "My comment")

    coEvery { getLoginStateUseCase() } returns User(Pubkey.parse(validNpub)!!)
    coEvery {
      commentRepository.createCommentEvent(
          hexPubkey = any(),
          content = "My comment",
          rootPubkey = "root-pubkey",
          dTag = "example.com/article",
          rootEventId = "event-123")
    } returns expectedEvent

    val result =
        useCase.createUnsignedEvent(
            content = "My comment",
            rootPubkey = "root-pubkey",
            dTag = "example.com/article",
            rootEventId = "event-123")

    assertTrue(result.isSuccess)
    assertEquals(expectedEvent, result.getOrNull())
    coVerify(exactly = 1) {
      commentRepository.createCommentEvent(
          hexPubkey = any(),
          content = "My comment",
          rootPubkey = "root-pubkey",
          dTag = "example.com/article",
          rootEventId = "event-123")
    }
  }

  @Test
  fun `publishSignedEvent delegates to CommentRepository`() = runTest {
    val signedJson = """{"id":"abc","sig":"def"}"""
    val expectedResult =
        PublishResult(
            eventId = "abc",
            successfulRelays = listOf("wss://relay.test.com"),
            failedRelays = emptyList())

    coEvery { commentRepository.publishComment(signedJson) } returns Result.success(expectedResult)

    val result = useCase.publishSignedEvent(signedJson)

    assertTrue(result.isSuccess)
    assertEquals(expectedResult, result.getOrNull())
    coVerify(exactly = 1) { commentRepository.publishComment(signedJson) }
  }
}
