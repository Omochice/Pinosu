package io.github.omochice.pinosu.feature.auth.data

import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/** Test class for [CachedRelayListProvider] relay list resolution and default fallback */
class CachedRelayListProviderTest {

  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var provider: CachedRelayListProvider

  private val expectedDefaults =
      Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS.map { RelayConfig(url = it) }

  @BeforeTest
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
  fun `getRelays returns default relays when cached relays are null`() = runTest {
    coEvery { localAuthDataSource.getRelayList() } returns null

    val result = provider.getRelays()

    assertEquals(expectedDefaults, result)
  }

  @Test
  fun `getRelays returns default relays when cached relays are empty`() = runTest {
    coEvery { localAuthDataSource.getRelayList() } returns emptyList()

    val result = provider.getRelays()

    assertEquals(expectedDefaults, result)
  }

  @Test
  fun `getUserRelays returns cached relays as is when present`() = runTest {
    val cachedRelays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com"),
            RelayConfig(url = "wss://relay2.example.com", read = false),
        )
    coEvery { localAuthDataSource.getRelayList() } returns cachedRelays

    val result = provider.getUserRelays()

    assertEquals(cachedRelays, result)
  }

  @Test
  fun `getUserRelays returns empty list when cached relays are null`() = runTest {
    coEvery { localAuthDataSource.getRelayList() } returns null

    val result = provider.getUserRelays()

    assertEquals(emptyList(), result)
  }

  @Test
  fun `getUserRelays returns empty list instead of bootstrap relays when cached relays are empty`() =
      runTest {
        coEvery { localAuthDataSource.getRelayList() } returns emptyList()

        val result = provider.getUserRelays()

        assertEquals(emptyList(), result)
      }
}
