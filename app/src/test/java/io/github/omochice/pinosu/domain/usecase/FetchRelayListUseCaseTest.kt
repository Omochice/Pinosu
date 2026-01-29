package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCaseImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [FetchRelayListUseCaseImpl]
 *
 * Uses Robolectric because Bech32.npubToHex depends on quartz library which needs Android runtime.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FetchRelayListUseCaseTest {

  private lateinit var fetcher: Nip65RelayListFetcher
  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var useCase: FetchRelayListUseCase

  companion object {
    /**
     * Valid test npub for testing. This is fiatjaf's npub (well-known Nostr developer) which is a
     * real Bech32-encoded public key that passes checksum validation.
     */
    const val TEST_VALID_NPUB = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
    const val TEST_VALID_HEX = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
  }

  @Before
  fun setup() {
    fetcher = mockk(relaxed = true)
    localAuthDataSource = mockk(relaxed = true)
    useCase = FetchRelayListUseCaseImpl(fetcher, localAuthDataSource)
  }

  @Test
  fun `invoke should fetch relay list and cache it on success`() = runTest {
    val relays = listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { fetcher.fetchRelayList(TEST_VALID_HEX) } returns Result.success(relays)
    coEvery { localAuthDataSource.saveRelayList(relays) } just runs

    val result = useCase(TEST_VALID_NPUB)

    assertTrue("Should return success", result.isSuccess)
    assertEquals("Should return fetched relays", relays, result.getOrNull())
    coVerify { fetcher.fetchRelayList(TEST_VALID_HEX) }
    coVerify { localAuthDataSource.saveRelayList(relays) }
  }

  @Test
  fun `invoke should return failure for invalid npub format`() = runTest {
    val invalidNpub = "not-a-valid-npub"

    val result = useCase(invalidNpub)

    assertTrue("Should return failure for invalid npub", result.isFailure)
  }

  @Test
  fun `invoke should propagate fetch failure`() = runTest {
    val error = RuntimeException("Network error")
    coEvery { fetcher.fetchRelayList(TEST_VALID_HEX) } returns Result.failure(error)

    val result = useCase(TEST_VALID_NPUB)

    assertTrue("Should return failure on fetch error", result.isFailure)
    assertEquals(
        "Should propagate original error message",
        "Network error",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun `invoke should still return success when cache fails`() = runTest {
    val relays = listOf(RelayConfig(url = "wss://relay.example.com"))
    coEvery { fetcher.fetchRelayList(TEST_VALID_HEX) } returns Result.success(relays)
    coEvery { localAuthDataSource.saveRelayList(any()) } throws
        StorageError.WriteError("Cache error")

    val result = useCase(TEST_VALID_NPUB)

    assertTrue("Should return success even when cache fails", result.isSuccess)
    assertEquals("Should return fetched relays", relays, result.getOrNull())
  }

  @Test
  fun `invoke should return empty list when no NIP-65 event found`() = runTest {
    coEvery { fetcher.fetchRelayList(TEST_VALID_HEX) } returns Result.success(emptyList())
    coEvery { localAuthDataSource.saveRelayList(any()) } just runs

    val result = useCase(TEST_VALID_NPUB)

    assertTrue("Should return success", result.isSuccess)
    assertTrue("Should return empty list", result.getOrNull()!!.isEmpty())
    coVerify { localAuthDataSource.saveRelayList(emptyList()) }
  }

  @Test
  fun `invoke should handle hex pubkey as npub`() = runTest {
    val hexPubkey = "a".repeat(64)

    val result = useCase(hexPubkey)

    assertTrue("Should return failure for hex pubkey (not npub format)", result.isFailure)
  }
}
