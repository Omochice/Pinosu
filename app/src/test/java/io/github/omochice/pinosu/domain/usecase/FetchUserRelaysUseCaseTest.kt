package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.relay.RelayConfig
import io.github.omochice.pinosu.data.repository.RelayListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FetchUserRelaysUseCase
 *
 * Tests relay list fetching via NIP-65
 */
class FetchUserRelaysUseCaseTest {

  private lateinit var relayListRepository: RelayListRepository
  private lateinit var fetchUserRelaysUseCase: FetchUserRelaysUseCase

  @Before
  fun setup() {
    relayListRepository = mockk(relaxed = true)
    fetchUserRelaysUseCase = FetchUserRelaysUseCaseImpl(relayListRepository)
  }

  @Test
  fun `invoke with valid pubkey should return relay list`() = runBlocking {
    val pubkey = "npub1testpubkey"
    val expectedRelays =
        listOf(
            RelayConfig(url = "wss://relay1.example.com", read = true, write = true),
            RelayConfig(url = "wss://relay2.example.com", read = true, write = false))
    coEvery { relayListRepository.fetchAndCacheUserRelays(pubkey) } returns
        Result.success(expectedRelays)

    val result = fetchUserRelaysUseCase(pubkey)

    assertTrue("Should be successful", result.isSuccess)
    assertEquals("Should return expected relays", expectedRelays, result.getOrNull())
    coVerify { relayListRepository.fetchAndCacheUserRelays(pubkey) }
  }

  @Test
  fun `invoke with empty result should return empty list`() = runBlocking {
    val pubkey = "npub1testpubkey"
    coEvery { relayListRepository.fetchAndCacheUserRelays(pubkey) } returns
        Result.success(emptyList())

    val result = fetchUserRelaysUseCase(pubkey)

    assertTrue("Should be successful", result.isSuccess)
    assertTrue("Should return empty list", result.getOrNull()?.isEmpty() == true)
  }

  @Test
  fun `invoke with error should return failure`() = runBlocking {
    val pubkey = "npub1testpubkey"
    val exception = RuntimeException("Network error")
    coEvery { relayListRepository.fetchAndCacheUserRelays(pubkey) } returns
        Result.failure(exception)

    val result = fetchUserRelaysUseCase(pubkey)

    assertTrue("Should be failure", result.isFailure)
    assertEquals("Should return the exception", exception, result.exceptionOrNull())
  }
}
