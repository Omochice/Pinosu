package io.github.omochice.pinosu.core.nip.nip01

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.model.UserProfile
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Test class for [Nip01ProfileFetcher] */
class Nip01ProfileFetcherTest {

  private lateinit var relayPool: RelayPool
  private lateinit var parser: Nip01ProfileParser
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var fetcher: Nip01ProfileFetcherImpl

  @Before
  fun setup() {
    relayPool = mockk(relaxed = true)
    parser = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    coEvery { localAuthDataSource.getRelayList() } returns null
    fetcher = Nip01ProfileFetcherImpl(relayPool, parser, localAuthDataSource)
  }

  @Test
  fun `fetchProfiles returns profiles for given pubkeys`() = runTest {
    val event =
        NostrEvent(
            id = "evt1",
            pubkey = "aabb",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content = """{"name":"Alice","picture":"https://example.com/a.png"}""",
            sig = "sig1")

    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)
    every { parser.parseProfileEvent(event) } returns
        UserProfile(pubkey = "aabb", name = "Alice", picture = "https://example.com/a.png")

    val result = fetcher.fetchProfiles(listOf("aabb"))

    assertEquals(1, result.size)
    assertEquals("Alice", result["aabb"]?.name)
    assertEquals("https://example.com/a.png", result["aabb"]?.picture)
  }

  @Test
  fun `fetchProfiles returns empty map when no events found`() = runTest {
    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

    val result = fetcher.fetchProfiles(listOf("aabb"))

    assertTrue(result.isEmpty())
  }

  @Test
  fun `fetchProfiles caches results and does not re-query relays`() = runTest {
    val event =
        NostrEvent(
            id = "evt1",
            pubkey = "aabb",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content = """{"name":"Alice"}""",
            sig = "sig1")

    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns listOf(event)
    every { parser.parseProfileEvent(event) } returns UserProfile(pubkey = "aabb", name = "Alice")

    fetcher.fetchProfiles(listOf("aabb"))
    fetcher.fetchProfiles(listOf("aabb"))

    coVerify(exactly = 1) { relayPool.subscribeWithTimeout(any(), any(), any()) }
  }

  @Test
  fun `fetchProfiles uses most recent event when multiple kind 0 events exist`() = runTest {
    val oldEvent =
        NostrEvent(
            id = "evt-old",
            pubkey = "aabb",
            createdAt = 1_600_000_000L,
            kind = 0,
            tags = emptyList(),
            content = """{"name":"OldName"}""",
            sig = "sig-old")
    val newEvent =
        NostrEvent(
            id = "evt-new",
            pubkey = "aabb",
            createdAt = 1_700_000_000L,
            kind = 0,
            tags = emptyList(),
            content = """{"name":"NewName"}""",
            sig = "sig-new")

    coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
        listOf(oldEvent, newEvent)
    every { parser.parseProfileEvent(oldEvent) } returns
        UserProfile(pubkey = "aabb", name = "OldName")
    every { parser.parseProfileEvent(newEvent) } returns
        UserProfile(pubkey = "aabb", name = "NewName")

    val result = fetcher.fetchProfiles(listOf("aabb"))

    assertEquals("NewName", result["aabb"]?.name)
  }

  @Test
  fun `fetchProfiles returns empty map for empty pubkey list`() = runTest {
    val result = fetcher.fetchProfiles(emptyList())

    assertTrue(result.isEmpty())
    coVerify(exactly = 0) { relayPool.subscribeWithTimeout(any(), any(), any()) }
  }
}
