package io.github.omochice.pinosu.feature.auth.integration

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LogoutUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCase
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginUiState
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for the NIP-55 login path, from the signer's ActivityResult to the login UI
 * state.
 *
 * Test strategy:
 * - Presentation layer: actual LoginViewModel
 * - Domain layer: actual UseCases
 * - Data layer: actual Nip55SignerClient and Nip55AuthRepository, mocked LocalAuthDataSource
 *
 * The repository is real here on purpose: mocking it would bypass the decoding of the signer's
 * response, which is the behavior under test. Robolectric supplies a working Intent and the Bech32
 * encoder used to normalize the pubkey.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class Nip55LoginIntegrationTest {

  private lateinit var localAuthDataSource: LocalAuthDataSource
  private lateinit var authRepository: AuthRepository
  private lateinit var fetchRelayListUseCase: FetchRelayListUseCase
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    val context = mockk<Context>(relaxed = true)
    val signerClient = Nip55SignerClient(context)

    localAuthDataSource = mockk(relaxed = true)
    fetchRelayListUseCase = mockk()
    coEvery { fetchRelayListUseCase(any()) } returns Result.success(emptyList())

    authRepository = Nip55AuthRepository(signerClient, localAuthDataSource)

    viewModel =
        LoginViewModel(
            Nip55LoginUseCase(authRepository),
            Nip55LogoutUseCase(authRepository),
            Nip55GetLoginStateUseCase(authRepository),
            authRepository,
            fetchRelayListUseCase,
            mockk<ReadOnlyLoginUseCase>(relaxed = true))
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `login succeeds when signer returns a hex pubkey`() = runTest {
    val response = Intent().putExtra("result", TEST_VALID_HEX)

    viewModel.processNip55Response(Activity.RESULT_OK, response)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is LoginUiState.Success, "state should be Success but was $state")
  }

  @Test
  fun `hex and npub responses log in the same account`() = runTest {
    viewModel.processNip55Response(Activity.RESULT_OK, Intent().putExtra("result", TEST_VALID_HEX))
    advanceUntilIdle()
    val fromHex = viewModel.mainUiState.value.userPubkey

    viewModel.processNip55Response(Activity.RESULT_OK, Intent().putExtra("result", TEST_VALID_NPUB))
    advanceUntilIdle()
    val fromNpub = viewModel.mainUiState.value.userPubkey

    assertEquals(TEST_VALID_NPUB, fromHex, "hex response should resolve to the matching npub")
    assertEquals(fromNpub, fromHex, "both encodings should log in the same account")
  }

  companion object {
    /**
     * Well-known Nostr public key (fiatjaf) in the two encodings a NIP-55 signer may return. Amber
     * 6.3.0 returns the hex form from get_public_key.
     */
    const val TEST_VALID_HEX = "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"
    const val TEST_VALID_NPUB = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
  }
}
