package io.github.omochice.pinosu.feature.comment.data.repository

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.core.relay.RelayPool
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test class for RelayCommentRepository
 *
 * Test scenarios:
 * 1. getCommentsForBookmark returns empty list when no events found
 * 2. getCommentsForBookmark parses kind 1111 events into Comment objects
 * 3. getCommentsForBookmark constructs correct NIP-22 A-tag filter
 * 4. createCommentEvent builds correct NIP-22 tags
 * 5. publishComment delegates to relayPool
 * 6. getEventsByIds sends ids filter and returns NostrEvent list
 */
class RelayCommentRepositoryTest {

  private lateinit var relayPool: RelayPool
  private lateinit var relayListProvider: RelayListProvider
  private lateinit var clientTagRepository: ClientTagRepository
  private lateinit var repository: RelayCommentRepository

  private val testRelays = listOf(RelayConfig(url = "wss://relay.test.com"))

  @Before
  fun setup() {
    relayPool = mockk()
    relayListProvider = mockk()
    clientTagRepository = mockk(relaxed = true)
    every { clientTagRepository.clientTagEnabledFlow } returns MutableStateFlow(true)
    repository = RelayCommentRepository(relayPool, relayListProvider, clientTagRepository)

    coEvery { relayListProvider.getRelays() } returns testRelays
  }

  @Test
  fun `getCommentsForBookmark returns empty list when no events found`() = runTest {
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    val result =
        repository.getCommentsForBookmark(rootPubkey = "abc123", identifier = "example.com/article")

    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()!!.isEmpty())
  }

  @Test
  fun `getCommentsForBookmark parses kind 1111 events into Comment objects`() = runTest {
    val event =
        NostrEvent(
            id = "comment-event-id",
            pubkey = "commenter-pubkey",
            createdAt = 1_700_000_000L,
            kind = 1111,
            tags =
                listOf(
                    listOf("A", "39701:abc123:example.com/article"),
                    listOf("E", "event123"),
                    listOf("K", "39701"),
                    listOf("P", "abc123")),
            content = "Nice bookmark!",
            sig = "dummy-sig")

    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)

    val result =
        repository.getCommentsForBookmark(rootPubkey = "abc123", identifier = "example.com/article")

    assertTrue(result.isSuccess)
    val comments = result.getOrNull()!!
    assertEquals(1, comments.size)
    assertEquals("comment-event-id", comments[0].id)
    assertEquals("Nice bookmark!", comments[0].content)
    assertEquals("commenter-pubkey", comments[0].authorPubkey)
    assertEquals(1_700_000_000L, comments[0].createdAt)
  }

  @Test
  fun `getCommentsForBookmark constructs correct NIP-22 A-tag filter`() = runTest {
    val filterSlot = slot<String>()
    coEvery { relayPool.subscribeWithTimeout(any(), capture(filterSlot), any()) } returns
        emptyList()

    repository.getCommentsForBookmark(rootPubkey = "abc123", identifier = "example.com/article")

    val filter = filterSlot.captured
    assertTrue(filter.contains("\"kinds\":[1111]"))
    assertTrue(filter.contains(""""#A":["39701:abc123:example.com/article"]"""))
  }

  @Test
  fun `createCommentEvent builds correct NIP-22 tags`() {
    val event =
        repository.createCommentEvent(
            hexPubkey = "my-pubkey",
            content = "My comment",
            rootPubkey = "root-pubkey",
            identifier = "example.com/article",
            rootEventId = "root-event-id")

    assertEquals(1111, event.kind)
    assertEquals("My comment", event.content)
    assertEquals("my-pubkey", event.pubkey)

    val tagMap = event.tags.groupBy { it[0] }

    assertEquals("39701:root-pubkey:example.com/article", tagMap["A"]?.first()?.get(1))
    assertEquals("root-event-id", tagMap["E"]?.first()?.get(1))
    assertEquals("39701", tagMap["K"]?.first()?.get(1))
    assertEquals("root-pubkey", tagMap["P"]?.first()?.get(1))

    assertEquals("39701:root-pubkey:example.com/article", tagMap["a"]?.first()?.get(1))
    assertEquals("root-event-id", tagMap["e"]?.first()?.get(1))
    assertEquals("39701", tagMap["k"]?.first()?.get(1))
    assertEquals("root-pubkey", tagMap["p"]?.first()?.get(1))
  }

  @Test
  fun `publishComment delegates to relayPool`() = runTest {
    val signedJson = """{"id":"abc","sig":"def"}"""
    val expectedResult =
        PublishResult(
            eventId = "abc",
            successfulRelays = listOf("wss://relay.test.com"),
            failedRelays = emptyList())

    coEvery { relayPool.publishEvent(any(), signedJson, any()) } returns
        Result.success(expectedResult)

    val result = repository.publishComment(signedJson)

    assertTrue(result.isSuccess)
    assertEquals(expectedResult, result.getOrNull())
    coVerify(exactly = 1) { relayPool.publishEvent(any(), signedJson, any()) }
  }

  @Test
  fun `getEventsByIds sends ids filter and returns NostrEvent list`() = runTest {
    val event =
        NostrEvent(
            id = "abc123",
            pubkey = "author-pubkey",
            createdAt = 1_700_000_000L,
            kind = 1,
            tags = emptyList(),
            content = "Hello world",
            sig = "dummy-sig")

    val filterSlot = slot<String>()
    coEvery { relayPool.subscribeWithTimeout(any(), capture(filterSlot), any()) } returns
        listOf(event)

    val result = repository.getEventsByIds(listOf("abc123"))

    assertTrue(result.isSuccess)
    val events = result.getOrNull()!!
    assertEquals(1, events.size)
    assertEquals("abc123", events[0].id)
    assertEquals("Hello world", events[0].content)
    assertEquals(1, events[0].kind)

    val filter = filterSlot.captured
    assertTrue(filter.contains(""""ids":["abc123"]"""))
  }

  @Test
  fun `createCommentEvent includes client tag when clientTagEnabled is true`() {
    every { clientTagRepository.clientTagEnabledFlow } returns MutableStateFlow(true)

    val event =
        repository.createCommentEvent(
            hexPubkey = "my-pubkey",
            content = "My comment",
            rootPubkey = "root-pubkey",
            identifier = "example.com/article",
            rootEventId = "root-event-id")

    val clientTags = event.tags.filter { it.isNotEmpty() && it[0] == "client" }
    assertEquals("Should have exactly one client tag", 1, clientTags.size)
    assertEquals("Pinosu", clientTags[0][1])
  }

  @Test
  fun `createCommentEvent excludes client tag when clientTagEnabled is false`() {
    every { clientTagRepository.clientTagEnabledFlow } returns MutableStateFlow(false)

    val event =
        repository.createCommentEvent(
            hexPubkey = "my-pubkey",
            content = "My comment",
            rootPubkey = "root-pubkey",
            identifier = "example.com/article",
            rootEventId = "root-event-id")

    val clientTags = event.tags.filter { it.isNotEmpty() && it[0] == "client" }
    assertTrue("Should not have client tag", clientTags.isEmpty())
  }
}
