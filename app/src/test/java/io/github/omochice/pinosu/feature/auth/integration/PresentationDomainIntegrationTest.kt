package io.github.omochice.pinosu.feature.auth.integration

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.Nip55LogoutUseCase
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginUiState
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for Presentation and Domain layers
 * - LoginViewModel + UseCases integration test
 * - Error handling flow integration test
 * - Logout flow integration test
 *
 * Test strategy:
 * - Presentation layer: actual LoginViewModel
 * - Domain layer: actual UseCases implementations (Nip55LoginUseCase, Nip55LogoutUseCase,
 *   Nip55GetLoginStateUseCase)
 * - Data layer: mocked AuthRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PresentationDomainIntegrationTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var loginUseCase: LoginUseCase
  private lateinit var logoutUseCase: LogoutUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var fetchRelayListUseCase: FetchRelayListUseCase
  private lateinit var readOnlyLoginUseCase:
      io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCase
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    authRepository = mockk(relaxed = true)
    fetchRelayListUseCase = mockk(relaxed = true)
    readOnlyLoginUseCase = mockk(relaxed = true)

    loginUseCase = Nip55LoginUseCase(authRepository)
    logoutUseCase = Nip55LogoutUseCase(authRepository)
    getLoginStateUseCase = Nip55GetLoginStateUseCase(authRepository)

    viewModel =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase,
            readOnlyLoginUseCase)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /**
   * Login button tap → NIP-55 signer not installed detection → dialog display
   *
   * Integration flow:
   * 1. User taps login button (LoginViewModel.onLoginButtonClicked)
   * 2. LoginUseCase verifies NIP-55 signer installation (LoginUseCase.checkNip55SignerInstalled)
   * 3. NIP-55 signer not installed detected in AuthRepository
   * 4. ViewModel updates UI state (showNip55InstallDialog = true)
   */
  @Test
  fun `login flow - when NIP-55 signer not installed - should show install dialog`() = runTest {
    every { authRepository.checkNip55SignerInstalled() } returns false

    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("state should be RequiresNip55Install", state is LoginUiState.RequiresNip55Install)

    // Verify AuthRepository.checkNip55SignerInstalled() is called
    io.mockk.verify { authRepository.checkNip55SignerInstalled() }
  }

  /**
   * Login success flow → UI state update → display main screen
   *
   * Integration flow:
   * 1. Receive NIP-55 signer response (LoginViewModel.processNip55Response)
   * 2. AuthRepository response handling
   * 3. User info save success
   * 4. ViewModel updates UI state (loginSuccess = true, set userPubkey)
   */
  @Test
  fun `login flow - when NIP-55 signer response success - should update UI state and navigate to main`() =
      runTest {
        val testPubkey = "npub1" + "a".repeat(59)
        val testUser = User(Pubkey.parse(testPubkey)!!)
        val mockIntent = mockk<android.content.Intent>(relaxed = true)
        coEvery { authRepository.processNip55Response(any(), any()) } returns
            Result.success(testUser)

        viewModel.processNip55Response(-1, mockIntent)
        advanceUntilIdle()

        val loginState = viewModel.uiState.first()
        val mainState = viewModel.mainUiState.first()

        assertTrue("state should be Success", loginState is LoginUiState.Success)
        assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)

        coVerify { authRepository.processNip55Response(any(), any()) }
      }

  /**
   * App startup login state check → logged in → display main screen
   *
   * Integration flow:
   * 1. Check login state on app startup (LoginViewModel.checkLoginState)
   * 2. GetLoginStateUseCase retrieves login state
   * 3. Retrieve saved user info from AuthRepository
   * 4. ViewModel updates main screen UI state (set userPubkey)
   */
  @Test
  fun `startup flow - when user logged in - should restore login state`() = runTest {
    val testPubkey = "npub1" + "b".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { authRepository.getLoginState() } returns testUser

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertEquals("userPubkey should be restored", testPubkey, state.userPubkey)

    coVerify { authRepository.getLoginState() }
  }

  /**
   * App startup login state check → not logged in → display login screen
   *
   * Integration flow:
   * 1. Check login state on app startup (LoginViewModel.checkLoginState)
   * 2. GetLoginStateUseCase retrieves login state
   * 3. AuthRepository returns null (not logged in)
   * 4. ViewModel main screen UI state remains null
   */
  @Test
  fun `startup flow - when user not logged in - should keep null state`() = runTest {
    coEvery { authRepository.getLoginState() } returns null

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null", state.userPubkey)

    coVerify { authRepository.getLoginState() }
  }

  /**
   * User rejection error → display error message → retry available
   *
   * Integration flow:
   * 1. Receive NIP-55 signer response (LoginViewModel.processNip55Response)
   * 2. User rejection error detected in AuthRepository
   * 3. LoginError.UserRejected error is returned
   * 4. ViewModel sets error message
   * 5. User taps retry button (onRetryLogin)
   */
  @Test
  fun `error flow - when user rejected - should show error and allow retry`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.UserRejected
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true

    viewModel.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val stateAfterError = viewModel.uiState.first()
    assertTrue(
        "state should be NonRetryable error", stateAfterError is LoginUiState.Error.NonRetryable)

    viewModel.onRetryLogin()
    advanceUntilIdle()

    io.mockk.verify(atLeast = 1) { authRepository.checkNip55SignerInstalled() }
  }

  /**
   * Timeout error → display timeout message → retry available
   *
   * Integration flow:
   * 1. Receive NIP-55 signer response (LoginViewModel.processNip55Response)
   * 2. Timeout error detected in AuthRepository
   * 3. LoginError.Timeout error is returned
   * 4. ViewModel sets timeout message
   */
  @Test
  fun `error flow - when timeout - should show Retryable error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.Timeout
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)

    viewModel.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("state should be Retryable error", state is LoginUiState.Error.Retryable)
  }

  /**
   * Network error → display error message
   *
   * Integration flow:
   * 1. Receive NIP-55 signer response (LoginViewModel.processNip55Response)
   * 2. Network error detected in AuthRepository
   * 3. LoginError.NetworkError error is returned
   * 4. ViewModel sets error message
   */
  @Test
  fun `error flow - when network error - should show Retryable error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.NetworkError("Connection failed")
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)

    viewModel.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("state should be Retryable error", state is LoginUiState.Error.Retryable)
  }

  /**
   * Close error dialog → clear error state
   *
   * Integration flow:
   * 1. Error occurs and dialog is displayed
   * 2. User closes error dialog (dismissError)
   * 3. ViewModel clears error state
   */
  @Test
  fun `error flow - when error dismissed - should reset state to Idle`() = runTest {
    every { authRepository.checkNip55SignerInstalled() } returns false
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    val stateBeforeDismiss = viewModel.uiState.first()
    assertTrue(
        "state should be RequiresNip55Install",
        stateBeforeDismiss is LoginUiState.RequiresNip55Install)

    viewModel.dismissError()
    advanceUntilIdle()

    val stateAfterDismiss = viewModel.uiState.first()
    assertTrue("state should be Idle after dismissError", stateAfterDismiss is LoginUiState.Idle)
  }

  /**
   * Logout success flow → clear login state → display login screen
   *
   * Integration flow:
   * 1. User taps logout button (LoginViewModel.onLogoutButtonClicked)
   * 2. LogoutUseCase executes logout
   * 3. AuthRepository successfully clears login state
   * 4. ViewModel updates UI state (userPubkey = null)
   */
  @Test
  fun `logout flow - when logout success - should clear login state`() = runTest {
    val testPubkey = "npub1" + "c".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { authRepository.getLoginState() } returns testUser
    viewModel.checkLoginState()
    advanceUntilIdle()

    val stateBeforeLogout = viewModel.mainUiState.first()
    assertEquals("userPubkey should be set", testPubkey, stateBeforeLogout.userPubkey)

    coEvery { authRepository.logout() } returns Result.success(Unit)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    val stateAfterLogout = viewModel.mainUiState.first()
    assertNull("userPubkey should be null after logout", stateAfterLogout.userPubkey)
    assertFalse("isLoggingOut should be false", stateAfterLogout.isLoggingOut)

    coVerify { authRepository.logout() }
  }

  /**
   * Logout failure flow → error handling → maintain login state
   *
   * Integration flow:
   * 1. User taps logout button (LoginViewModel.onLogoutButtonClicked)
   * 2. LogoutUseCase executes logout
   * 3. Storage error occurs in AuthRepository
   * 4. ViewModel handles error (isLoggingOut = false)
   */
  @Test
  fun `logout flow - when logout fails - should handle error gracefully`() = runTest {
    val testPubkey = "npub1" + "d".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { authRepository.getLoginState() } returns testUser
    viewModel.checkLoginState()
    advanceUntilIdle()

    val error = LogoutError.StorageError("Failed to clear storage")
    coEvery { authRepository.logout() } returns Result.failure(error)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertFalse("isLoggingOut should be false after error", state.isLoggingOut)

    coVerify { authRepository.logout() }
  }
}
