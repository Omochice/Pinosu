package io.github.omochice.pinosu.feature.auth.presentation.viewmodel

import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LogoutUseCase
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
 * Unit tests for LoginViewModel
 * - UI state management test
 * - User interaction handling test
 * - UseCases invocation test
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

  private lateinit var loginUseCase: LoginUseCase
  private lateinit var logoutUseCase: LogoutUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var authRepository: AuthRepository
  private lateinit var fetchRelayListUseCase: FetchRelayListUseCase
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loginUseCase = mockk(relaxed = true)
    logoutUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    authRepository = mockk(relaxed = true)
    fetchRelayListUseCase = mockk(relaxed = true)
    viewModel =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial LoginUiState should be Idle`() = runTest {
    val state = viewModel.uiState.first()

    assertTrue("state should be Idle", state is LoginUiState.Idle)
  }

  @Test
  fun `initial MainUiState should have default values`() = runTest {
    val state = viewModel.mainUiState.first()

    assertNull("userPubkey should be null", state.userPubkey)
    assertFalse("isLoggingOut should be false", state.isLoggingOut)
  }

  @Test
  fun `checkLoginState should update mainUiState when user is logged in`() = runTest {
    val testPubkey = "npub1" + "a".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    coEvery { getLoginStateUseCase() } returns testUser

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertEquals("userPubkey should be set", testPubkey, state.userPubkey)
    coVerify { getLoginStateUseCase() }
  }

  @Test
  fun `checkLoginState should keep mainUiState null when user is not logged in`() = runTest {
    coEvery { getLoginStateUseCase() } returns null

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null", state.userPubkey)
    coVerify { getLoginStateUseCase() }
  }

  @Test
  fun `onLoginButtonClicked should check if NIP-55 signer is installed`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns true

    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    io.mockk.verify { loginUseCase.checkNip55SignerInstalled() }
  }

  @Test
  fun `onLoginButtonClicked should show install dialog when NIP-55 signer is not installed`() =
      runTest {
        every { loginUseCase.checkNip55SignerInstalled() } returns false

        viewModel.onLoginButtonClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(
            "state should be RequiresNip55Install", state is LoginUiState.RequiresNip55Install)
      }

  @Test
  fun `onLogoutButtonClicked should call logoutUseCase and update state on success`() = runTest {
    coEvery { logoutUseCase() } returns Result.success(Unit)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    coVerify { logoutUseCase() }
    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null after logout", state.userPubkey)
  }

  @Test
  fun `onLogoutButtonClicked should handle logout failure gracefully`() = runTest {
    val error =
        io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError.StorageError("Failed")
    coEvery { logoutUseCase() } returns Result.failure(error)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    coVerify { logoutUseCase() }
    val state = viewModel.mainUiState.first()
    assertFalse("isLoggingOut should be false after failure", state.isLoggingOut)
  }

  @Test
  fun `dismissError should reset state to Idle`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns false
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    viewModel.dismissError()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("state should be Idle after dismissError", state is LoginUiState.Idle)
  }

  @Test
  fun `onRetryLogin should retry login by calling onLoginButtonClicked`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns true

    viewModel.onRetryLogin()
    advanceUntilIdle()

    io.mockk.verify { loginUseCase.checkNip55SignerInstalled() }
  }

  @Test
  fun `processNip55Response should set loading state during processing`() = runTest {
    val testPubkey = "npub1" + "c".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    coEvery { authRepository.processNip55Response(any(), any()) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(testUser)
        }
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)

    advanceUntilIdle()

    coVerify { authRepository.processNip55Response(any(), any()) }
  }

  @Test
  fun `processNip55Response should set Success state on success`() = runTest {
    val testPubkey = "npub1" + "d".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.success(testUser)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val loginState = viewModelWithMock.uiState.first()
    val mainState = viewModelWithMock.mainUiState.first()
    assertTrue("state should be Success", loginState is LoginUiState.Success)
    assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)
  }

  @Test
  fun `processNip55Response should set NonRetryable error on UserRejected error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    val error = io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError.UserRejected
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertTrue("state should be NonRetryable error", state is LoginUiState.Error.NonRetryable)
  }

  @Test
  fun `processNip55Response should set Retryable error on Timeout error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    val error = io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError.Timeout
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertTrue("state should be Retryable error", state is LoginUiState.Error.Retryable)
  }

  @Test
  fun `processNip55Response should set Retryable error on NetworkError`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    val error =
        io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError.NetworkError(
            "Connection failed")
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertTrue("state should be Retryable error", state is LoginUiState.Error.Retryable)
  }

  @Test
  fun `processNip55Response should wait for relay list fetch before setting Success state`() =
      runTest {
        val testPubkey = "npub1" + "e".repeat(59)
        val testUser = User(Pubkey.parse(testPubkey)!!)
        val mockIntent = mockk<android.content.Intent>(relaxed = true)
        val authRepository = mockk<AuthRepository>()
        val fetchRelayListUseCase = mockk<FetchRelayListUseCase>()

        coEvery { authRepository.processNip55Response(any(), any()) } returns
            Result.success(testUser)
        every { authRepository.checkNip55SignerInstalled() } returns true
        coEvery { authRepository.getLoginState() } returns null
        coEvery { authRepository.logout() } returns Result.success(Unit)
        coEvery { fetchRelayListUseCase(any()) } coAnswers
            {
              kotlinx.coroutines.delay(100)
              Result.success(emptyList())
            }

        val viewModelWithMock =
            LoginViewModel(
                loginUseCase,
                logoutUseCase,
                getLoginStateUseCase,
                authRepository,
                fetchRelayListUseCase)

        viewModelWithMock.processNip55Response(-1, mockIntent)
        advanceUntilIdle()

        coVerify { fetchRelayListUseCase(testPubkey) }

        val state = viewModelWithMock.uiState.first()
        assertTrue(
            "state should be Success after relay fetch completes", state is LoginUiState.Success)
      }

  @Test
  fun `processNip55Response should set Success state even if relay list fetch fails`() = runTest {
    val testPubkey = "npub1" + "f".repeat(59)
    val testUser = User(Pubkey.parse(testPubkey)!!)
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<AuthRepository>()
    val fetchRelayListUseCase = mockk<FetchRelayListUseCase>()

    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.success(testUser)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)
    coEvery { fetchRelayListUseCase(any()) } returns
        Result.failure(RuntimeException("Network error"))

    val viewModelWithMock =
        LoginViewModel(
            loginUseCase,
            logoutUseCase,
            getLoginStateUseCase,
            authRepository,
            fetchRelayListUseCase)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertTrue("state should be Success even if relay fetch fails", state is LoginUiState.Success)
  }
}
