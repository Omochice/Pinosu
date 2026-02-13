package io.github.omochice.pinosu.feature.bookmark.data.repository

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.bookmark.data.metadata.UrlMetadata
import io.github.omochice.pinosu.feature.bookmark.data.metadata.UrlMetadataFetcher
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [RelayBookmarkRepository] rawJson population
 *
 * Uses Robolectric because RelayBookmarkRepository depends on android.util.Log and Pubkey (quartz).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RelayBookmarkRepositoryTest {

  private lateinit var relayPool: RelayPool
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var urlMetadataFetcher: UrlMetadataFetcher
  private lateinit var repository: RelayBookmarkRepository

  private val json = Json { ignoreUnknownKeys = true }

  @Before
  fun setup() {
    relayPool = mockk()
    localAuthDataSource = mockk()
    urlMetadataFetcher = mockk()
    repository = RelayBookmarkRepository(relayPool, localAuthDataSource, urlMetadataFetcher)
  }

  @Test
  fun `getBookmarkList populates rawJson with serialized NostrEvent`() = runTest {
    val event =
        NostrEvent(
            id = "event123",
            pubkey = "pubkey456",
            createdAt = 1_700_000_000L,
            kind = RelayBookmarkRepository.KIND_BOOKMARK_LIST,
            tags = listOf(listOf("d", "example.com/article"), listOf("title", "Test Article")),
            content = "",
            sig = "sig789")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)
    coEvery { urlMetadataFetcher.fetchMetadata(any()) } returns
        Result.success(
            UrlMetadata(title = "Fetched Title", imageUrl = "https://example.com/img.jpg"))

    val result = repository.getBookmarkList(TEST_VALID_NPUB)

    assertTrue("Result should be success", result.isSuccess)
    val bookmarkList = result.getOrNull()
    assertNotNull("BookmarkList should not be null", bookmarkList)
    val items = bookmarkList!!.items
    assertEquals("Should have 1 bookmark item", 1, items.size)

    val rawJson = items[0].rawJson
    assertNotNull("rawJson should not be null", rawJson)

    val deserializedEvent = json.decodeFromString<NostrEvent>(rawJson!!)
    assertEquals("Deserialized event id should match", "event123", deserializedEvent.id)
    assertEquals("Deserialized event sig should match", "sig789", deserializedEvent.sig)
    assertEquals(
        "Deserialized event kind should match",
        RelayBookmarkRepository.KIND_BOOKMARK_LIST,
        deserializedEvent.kind)
  }

  @Test
  fun `getBookmarkList populates imageUrl from metadata`() = runTest {
    val event =
        NostrEvent(
            id = "event456",
            pubkey = "pubkey789",
            createdAt = 1_700_000_000L,
            kind = RelayBookmarkRepository.KIND_BOOKMARK_LIST,
            tags = listOf(listOf("d", "example.com/page"), listOf("title", "Page Title")),
            content = "",
            sig = "sig123")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)
    coEvery { urlMetadataFetcher.fetchMetadata(any()) } returns
        Result.success(UrlMetadata(title = "OG Title", imageUrl = "https://example.com/ogp.jpg"))

    val result = repository.getBookmarkList(TEST_VALID_NPUB)

    assertTrue("Result should be success", result.isSuccess)
    val items = result.getOrNull()!!.items
    assertEquals("Should have 1 bookmark item", 1, items.size)
    assertEquals(
        "imageUrl should be populated from metadata",
        "https://example.com/ogp.jpg",
        items[0].imageUrl)
  }

  @Test
  fun `getBookmarkList sets null imageUrl when metadata fetch fails`() = runTest {
    val event =
        NostrEvent(
            id = "event789",
            pubkey = "pubkey012",
            createdAt = 1_700_000_000L,
            kind = RelayBookmarkRepository.KIND_BOOKMARK_LIST,
            tags = listOf(listOf("d", "example.com/fail"), listOf("title", "Fail Title")),
            content = "",
            sig = "sig456")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)
    coEvery { urlMetadataFetcher.fetchMetadata(any()) } returns
        Result.failure(Exception("Network error"))

    val result = repository.getBookmarkList(TEST_VALID_NPUB)

    assertTrue("Result should be success", result.isSuccess)
    val items = result.getOrNull()!!.items
    assertEquals("Should have 1 bookmark item", 1, items.size)
    assertEquals("Title should fall back to title tag", "Fail Title", items[0].title)
    assertEquals("imageUrl should be null when metadata fetch fails", null, items[0].imageUrl)
  }

  @Test
  fun `getBookmarkList skips event when rTags contain only invalid URLs`() = runTest {
    val event =
        NostrEvent(
            id = "eventInvalid",
            pubkey = "pubkeyInvalid",
            createdAt = 1_700_000_000L,
            kind = RelayBookmarkRepository.KIND_BOOKMARK_LIST,
            tags = listOf(listOf("r", "not-a-valid-url")),
            content = "",
            sig = "sigInvalid")

    coEvery { localAuthDataSource.getRelayList() } returns
        listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)

    val result = repository.getBookmarkList(TEST_VALID_NPUB)

    assertTrue("Result should be success", result.isSuccess)
    val items = result.getOrNull()!!.items
    assertEquals("Event with only invalid rTags should be skipped", 0, items.size)
  }

  companion object {
    /**
     * Valid test npub (fiatjaf's pubkey) that passes Bech32 checksum validation in Pubkey.parse()
     */
    const val TEST_VALID_NPUB = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
  }
}
