package io.github.omochice.pinosu.presentation.viewmodel

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
 * LoginViewModelの単体テスト
 *
 * Task 7.1: LoginViewModelの実装
 * - UI状態管理のテスト
 * - ユーザー操作ハンドリングのテスト
 * - UseCases呼び出しのテスト
 *
 * Requirements: 1.1, 1.5, 2.2, 2.3, 2.4, 3.2, 3.3, 3.5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

  private lateinit var loginUseCase: LoginUseCase
  private lateinit var logoutUseCase: LogoutUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    loginUseCase = mockk(relaxed = true)
    logoutUseCase = mockk(relaxed = true)
    getLoginStateUseCase = mockk(relaxed = true)
    viewModel = LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ========== 初期状態のテスト ==========

  @Test
  fun `initial LoginUiState should have default values`() = runTest {
    // When: ViewModelを初期化
    val state = viewModel.uiState.first()

    // Then: 初期状態が正しい
    assertFalse("isLoading should be false", state.isLoading)
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("showAmberInstallDialog should be false", state.showAmberInstallDialog)
    assertFalse("loginSuccess should be false", state.loginSuccess)
  }

  @Test
  fun `initial MainUiState should have default values`() = runTest {
    // When: ViewModelを初期化
    val state = viewModel.mainUiState.first()

    // Then: 初期状態が正しい
    assertNull("userPubkey should be null", state.userPubkey)
    assertFalse("isLoggingOut should be false", state.isLoggingOut)
  }

  // ========== checkLoginState() のテスト ==========

  @Test
  fun `checkLoginState should update mainUiState when user is logged in`() = runTest {
    // Given: ログイン済みユーザー
    val testPubkey = "a".repeat(64)
    val testUser = User(testPubkey)
    coEvery { getLoginStateUseCase() } returns testUser

    // When: checkLoginState()を呼び出す
    viewModel.checkLoginState()
    advanceUntilIdle()

    // Then: mainUiStateが更新される
    val state = viewModel.mainUiState.first()
    assertEquals("userPubkey should be set", testPubkey, state.userPubkey)
    coVerify { getLoginStateUseCase() }
  }

  @Test
  fun `checkLoginState should keep mainUiState null when user is not logged in`() = runTest {
    // Given: 未ログイン状態
    coEvery { getLoginStateUseCase() } returns null

    // When: checkLoginState()を呼び出す
    viewModel.checkLoginState()
    advanceUntilIdle()

    // Then: mainUiStateはnullのまま
    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null", state.userPubkey)
    coVerify { getLoginStateUseCase() }
  }

  // ========== onLoginButtonClicked() のテスト ==========

  @Test
  fun `onLoginButtonClicked should check if Amber is installed`() = runTest {
    // Given: Amberがインストール済み
    every { loginUseCase.checkAmberInstalled() } returns true

    // When: onLoginButtonClicked()を呼び出す
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    // Then: checkAmberInstalled()が呼ばれる
    io.mockk.verify { loginUseCase.checkAmberInstalled() }
  }

  @Test
  fun `onLoginButtonClicked should show install dialog when Amber is not installed`() = runTest {
    // Given: Amberが未インストール
    every { loginUseCase.checkAmberInstalled() } returns false

    // When: onLoginButtonClicked()を呼び出す
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    // Then: showAmberInstallDialogがtrueになる
    val state = viewModel.uiState.first()
    assertTrue("showAmberInstallDialog should be true", state.showAmberInstallDialog)
  }

  // ========== onLogoutButtonClicked() のテスト ==========

  @Test
  fun `onLogoutButtonClicked should call logoutUseCase and update state on success`() = runTest {
    // Given: ログアウトが成功
    coEvery { logoutUseCase() } returns Result.success(Unit)

    // When: onLogoutButtonClicked()を呼び出す
    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    // Then: logoutUseCase()が呼ばれ、mainUiStateがクリアされる
    coVerify { logoutUseCase() }
    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null after logout", state.userPubkey)
  }

  @Test
  fun `onLogoutButtonClicked should handle logout failure gracefully`() = runTest {
    // Given: ログアウトが失敗
    val error = io.github.omochice.pinosu.domain.model.error.LogoutError.StorageError("Failed")
    coEvery { logoutUseCase() } returns Result.failure(error)

    // When: onLogoutButtonClicked()を呼び出す
    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    // Then: logoutUseCase()が呼ばれ、isLoggingOutがfalseに戻る
    coVerify { logoutUseCase() }
    val state = viewModel.mainUiState.first()
    assertFalse("isLoggingOut should be false after failure", state.isLoggingOut)
  }

  // ========== dismissError() のテスト ==========

  @Test
  fun `dismissError should clear error message`() = runTest {
    // Given: エラーメッセージが設定されている状態
    // (これはonLoginButtonClicked等でエラーが発生した後を想定)
    every { loginUseCase.checkAmberInstalled() } returns false
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    // When: dismissError()を呼び出す
    viewModel.dismissError()
    advanceUntilIdle()

    // Then: エラー関連の状態がクリアされる
    val state = viewModel.uiState.first()
    assertNull("errorMessage should be null", state.errorMessage)
    assertFalse("showAmberInstallDialog should be false", state.showAmberInstallDialog)
  }

  // ========== onRetryLogin() のテスト ==========

  @Test
  fun `onRetryLogin should retry login by calling onLoginButtonClicked`() = runTest {
    // Given: Amberがインストール済み
    every { loginUseCase.checkAmberInstalled() } returns true

    // When: onRetryLogin()を呼び出す
    viewModel.onRetryLogin()
    advanceUntilIdle()

    // Then: checkAmberInstalled()が呼ばれる（onLoginButtonClickedと同じ動作）
    io.mockk.verify { loginUseCase.checkAmberInstalled() }
  }
}
