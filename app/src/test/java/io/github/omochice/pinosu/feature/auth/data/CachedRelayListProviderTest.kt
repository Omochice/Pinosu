package io.github.omochice.pinosu.feature.auth.data

import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Test class for [CachedRelayListProvider] relay list resolution and default fallback */
class CachedRelayListProviderTest {

  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var provider: CachedRelayListProvider

  @Before
  fun setup() {
    localAuthDataSource = mockk()
    provider = CachedRelayListProvider(localAuthDataSource)
  }

  @Test
  fun `getRelays returns cached relays when present`() = runTest {
    val cachedRelays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com"),
            RelayConfig(url = "wss://relay2.example.com"),
        )
    coEvery { localAuthDataSource.getRelayList() } returns cachedRelays

    val result = provider.getRelays()

    assertEquals(cachedRelays, result)
  }

  @Test
  fun `getRelays returns default relay when cached relays are null`() = runTest {
    coEvery { localAuthDataSource.getRelayList() } returns null

    val result = provider.getRelays()

    assertEquals(1, result.size)
    assertEquals("wss://yabu.me", result[0].url)
  }

  @Test
  fun `getRelays returns default relay when cached relays are empty`() = runTest {
    coEvery { localAuthDataSource.getRelayList() } returns emptyList()

    val result = provider.getRelays()

    assertEquals(1, result.size)
    assertEquals("wss://yabu.me", result[0].url)
  }
}
