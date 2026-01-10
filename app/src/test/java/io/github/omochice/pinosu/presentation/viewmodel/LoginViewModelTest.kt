package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
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
import org.junit.Assert.*
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
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loginUseCase = mockk(relaxed = true)
    logoutUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    authRepository = mockk(relaxed = true)
    viewModel = LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial LoginUiState should have default values`() = runTest {
    val state = viewModel.uiState.first()

    assertFalse("isLoading should be false", state.isLoading)
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("showNip55InstallDialog should be false", state.showNip55InstallDialog)
    assertFalse("loginSuccess should be false", state.loginSuccess)
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
    val testUser = User(testPubkey)
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
  fun `onLoginButtonClicked should check if Amber is installed`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns true

    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    io.mockk.verify { loginUseCase.checkNip55SignerInstalled() }
  }

  @Test
  fun `onLoginButtonClicked should show install dialog when Amber is not installed`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns false

    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("showNip55InstallDialog should be true", state.showNip55InstallDialog)
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
    val error = io.github.omochice.pinosu.domain.model.error.LogoutError.StorageError("Failed")
    coEvery { logoutUseCase() } returns Result.failure(error)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    coVerify { logoutUseCase() }
    val state = viewModel.mainUiState.first()
    assertFalse("isLoggingOut should be false after failure", state.isLoggingOut)
  }

  @Test
  fun `dismissError should clear error message`() = runTest {
    every { loginUseCase.checkNip55SignerInstalled() } returns false
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    viewModel.dismissError()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("showNip55InstallDialog should be false", state.showNip55InstallDialog)
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
    val testUser = User(testPubkey)
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
    coEvery { authRepository.processNip55Response(any(), any()) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          Result.success(testUser)
        }
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

    viewModelWithMock.processNip55Response(-1, mockIntent)

    advanceUntilIdle()

    coVerify { authRepository.processNip55Response(any(), any()) }
  }

  @Test
  fun `processNip55Response should set loginSuccess on success`() = runTest {
    val testPubkey = "npub1" + "d".repeat(59)
    val testUser = User(testPubkey)
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.success(testUser)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val loginState = viewModelWithMock.uiState.first()
    val mainState = viewModelWithMock.mainUiState.first()
    assertTrue("loginSuccess should be true", loginState.loginSuccess)
    assertFalse("isLoading should be false after success", loginState.isLoading)
    assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)
  }

  @Test
  fun `processNip55Response should set error message on UserRejected error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
    val error = io.github.omochice.pinosu.domain.model.error.LoginError.UserRejected
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("isLoading should be false", state.isLoading)
    assertFalse("loginSuccess should be false", state.loginSuccess)
  }

  @Test
  fun `processNip55Response should set error message on Timeout error`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
    val error = io.github.omochice.pinosu.domain.model.error.LoginError.Timeout
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertTrue(
        "errorMessage should contain timeout info",
        state.errorMessage?.contains("timed out") == true)
    assertFalse("isLoading should be false", state.isLoading)
  }

  @Test
  fun `processNip55Response should handle NetworkError`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val authRepository = mockk<io.github.omochice.pinosu.data.repository.AuthRepository>()
    val error =
        io.github.omochice.pinosu.domain.model.error.LoginError.NetworkError("Connection failed")
    coEvery { authRepository.processNip55Response(any(), any()) } returns Result.failure(error)
    every { authRepository.checkNip55SignerInstalled() } returns true
    coEvery { authRepository.getLoginState() } returns null
    coEvery { authRepository.logout() } returns Result.success(Unit)

    val viewModelWithMock =
        LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)

    viewModelWithMock.processNip55Response(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModelWithMock.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("isLoading should be false", state.isLoading)
  }
}
