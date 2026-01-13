package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.model.NostrEvent
import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.relay.RelayPool
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RelayCommentRepository
 *
 * Tests cover:
 * - Successful comment fetching with correct filter construction
 * - NostrEvent to Comment transformation
 * - Empty result handling
 * - Error handling
 * - e tag extraction for referenced event ID
 */
class RelayCommentRepositoryTest {

  private lateinit var relayPool: RelayPool
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var commentRepository: CommentRepository

  @Before
  fun setup() {
    relayPool = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    commentRepository = RelayCommentRepository(relayPool, localAuthDataSource)
  }

  @Test
  fun `getCommentsForEvent should construct filter with kind 1111 and e tag`() = runTest {
    val eventId = "abc123def456"
    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    commentRepository.getCommentsForEvent(eventId)

    coVerify {
      relayPool.subscribeWithTimeout(
          any(),
          match { filter ->
            filter.contains("\"kinds\":[1111]") && filter.contains("\"#e\":[\"$eventId\"]")
          },
          any())
    }
  }

  @Test
  fun `getCommentsForEvent should transform NostrEvent to Comment correctly`() = runTest {
    val eventId = "target-event-id"
    val commentEvent =
        NostrEvent(
            id = "comment-id-123",
            pubkey = "author-pubkey",
            createdAt = 1700000000L,
            kind = 1111,
            tags = listOf(listOf("e", eventId, "wss://relay.example.com", "root")),
            content = "This is a comment")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(commentEvent)

    val result = commentRepository.getCommentsForEvent(eventId)

    assertTrue("Result should be success", result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals("Should have 1 comment", 1, comments.size)

    val comment = comments.first()
    assertEquals("comment-id-123", comment.id)
    assertEquals("This is a comment", comment.content)
    assertEquals("author-pubkey", comment.author)
    assertEquals(1700000000L, comment.createdAt)
    assertEquals(eventId, comment.referencedEventId)
  }

  @Test
  fun `getCommentsForEvent should return empty list when no comments found`() = runTest {
    val eventId = "event-with-no-comments"

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    val result = commentRepository.getCommentsForEvent(eventId)

    assertTrue("Result should be success", result.isSuccess)
    val comments = result.getOrNull()!!
    assertTrue("Comments should be empty", comments.isEmpty())
  }

  @Test
  fun `getCommentsForEvent should return failure when relay throws exception`() = runTest {
    val eventId = "some-event-id"

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } throws
        RuntimeException("Network error")

    val result = commentRepository.getCommentsForEvent(eventId)

    assertTrue("Result should be failure", result.isFailure)
  }

  @Test
  fun `getCommentsForEvent should use default relay when no cached relays`() = runTest {
    val eventId = "some-event-id"

    coEvery { localAuthDataSource.getRelayList() } returns null
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    commentRepository.getCommentsForEvent(eventId)

    coVerify {
      relayPool.subscribeWithTimeout(
          match { relays -> relays.any { it.url == "wss://yabu.me" } }, any(), any())
    }
  }

  @Test
  fun `getCommentsForEvent should extract first e tag as referenced event ID`() = runTest {
    val targetEventId = "target-event"
    val commentEvent =
        NostrEvent(
            id = "comment-id",
            pubkey = "author",
            createdAt = 1700000000L,
            kind = 1111,
            tags =
                listOf(
                    listOf("e", targetEventId),
                    listOf("e", "other-event-id"),
                    listOf("p", "some-pubkey")),
            content = "Comment with multiple e tags")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(commentEvent)

    val result = commentRepository.getCommentsForEvent(targetEventId)

    assertTrue("Result should be success", result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals(targetEventId, comments.first().referencedEventId)
  }

  @Test
  fun `getCommentsForEvent should sort comments by createdAt descending`() = runTest {
    val eventId = "target-event"
    val olderComment =
        NostrEvent(
            id = "older",
            pubkey = "author",
            createdAt = 1700000000L,
            kind = 1111,
            tags = listOf(listOf("e", eventId)),
            content = "Older comment")
    val newerComment =
        NostrEvent(
            id = "newer",
            pubkey = "author",
            createdAt = 1700001000L,
            kind = 1111,
            tags = listOf(listOf("e", eventId)),
            content = "Newer comment")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
        listOf(olderComment, newerComment)

    val result = commentRepository.getCommentsForEvent(eventId)

    assertTrue("Result should be success", result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals("newer", comments[0].id)
    assertEquals("older", comments[1].id)
  }
}
