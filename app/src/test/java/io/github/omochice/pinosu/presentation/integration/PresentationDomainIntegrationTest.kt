package io.github.omochice.pinosu.presentation.integration

import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.usecase.AmberGetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLoginUseCase
import io.github.omochice.pinosu.domain.usecase.AmberLogoutUseCase
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
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
 * Presentation層とDomain層の統合テスト
 * - LoginViewModel + UseCases統合テスト
 * - エラーハンドリングフロー統合テスト
 * - ログアウトフロー統合テスト
 *
 * テスト方針:
 * - Presentation層: 実際のLoginViewModel
 * - Domain層: 実際のUseCasesImplementations (AmberLoginUseCase, AmberLogoutUseCase,
 *   AmberGetLoginStateUseCase)
 * - Data層: モックされたAuthRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PresentationDomainIntegrationTest {

  private lateinit var authRepository: AuthRepository
  private lateinit var loginUseCase: LoginUseCase
  private lateinit var logoutUseCase: LogoutUseCase
  private lateinit var getLoginStateUseCase: GetLoginStateUseCase
  private lateinit var viewModel: LoginViewModel

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    // Data層はモック
    authRepository = mockk(relaxed = true)

    // Domain層は実際の実装を使用（統合テスト）
    loginUseCase = AmberLoginUseCase(authRepository)
    logoutUseCase = AmberLogoutUseCase(authRepository)
    getLoginStateUseCase = AmberGetLoginStateUseCase(authRepository)

    // Presentation層は実際の実装を使用
    viewModel = LoginViewModel(loginUseCase, logoutUseCase, getLoginStateUseCase, authRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /**
   * ログインボタンタップ → Amber未インストール検出 → ダイアログ表示
   *
   * 統合フロー:
   * 1. ユーザーがログインボタンをタップ (LoginViewModel.onLoginButtonClicked)
   * 2. LoginUseCaseがAmberインストール確認 (LoginUseCase.checkAmberInstalled)
   * 3. AuthRepositoryで未インストール検出
   * 4. ViewModelがUI状態を更新 (showAmberInstallDialog = true)
   */
  @Test
  fun `login flow - when Amber not installed - should show install dialog`() = runTest {
    every { authRepository.checkAmberInstalled() } returns false

    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue("showAmberInstallDialog should be true", state.showAmberInstallDialog)
    assertFalse("isLoading should be false", state.isLoading)
    assertNull("errorMessage should be null", state.errorMessage)

    // AuthRepositoryのcheckAmberInstalled()が呼ばれることを確認
    io.mockk.verify { authRepository.checkAmberInstalled() }
  }

  /**
   * ログイン成功フロー → UI状態更新 → メイン画面表示
   *
   * 統合フロー:
   * 1. Amberレスポンス受信 (LoginViewModel.processAmberResponse)
   * 2. AuthRepositoryでレスポンス処理
   * 3. ユーザー情報保存成功
   * 4. ViewModelがUI状態を更新 (loginSuccess = true, userPubkey設定)
   */
  @Test
  fun `login flow - when Amber response success - should update UI state and navigate to main`() =
      runTest {
        val testPubkey = "npub1" + "a".repeat(59)
        val testUser = User(testPubkey)
        val mockIntent = mockk<android.content.Intent>(relaxed = true)
        coEvery { authRepository.processAmberResponse(any(), any()) } returns
            Result.success(testUser)

        viewModel.processAmberResponse(-1, mockIntent)
        advanceUntilIdle()

        val loginState = viewModel.uiState.first()
        val mainState = viewModel.mainUiState.first()

        assertTrue("loginSuccess should be true", loginState.loginSuccess)
        assertFalse("isLoading should be false", loginState.isLoading)
        assertNull("errorMessage should be null", loginState.errorMessage)
        assertEquals("userPubkey should be set", testPubkey, mainState.userPubkey)

        // AuthRepositoryのprocessAmberResponse()が呼ばれることを確認
        coVerify { authRepository.processAmberResponse(any(), any()) }
      }

  /**
   * アプリ起動時のログイン状態確認 → ログイン済み → メイン画面表示
   *
   * 統合フロー:
   * 1. アプリ起動時にログイン状態確認 (LoginViewModel.checkLoginState)
   * 2. GetLoginStateUseCaseがログイン状態取得
   * 3. AuthRepositoryから保存済みユーザー情報取得
   * 4. ViewModelがメイン画面用UI状態を更新 (userPubkey設定)
   */
  @Test
  fun `startup flow - when user logged in - should restore login state`() = runTest {
    val testPubkey = "npub1" + "b".repeat(59)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertEquals("userPubkey should be restored", testPubkey, state.userPubkey)

    // AuthRepositoryのgetLoginState()が呼ばれることを確認
    coVerify { authRepository.getLoginState() }
  }

  /**
   * アプリ起動時のログイン状態確認 → 未ログイン → ログイン画面表示
   *
   * 統合フロー:
   * 1. アプリ起動時にログイン状態確認 (LoginViewModel.checkLoginState)
   * 2. GetLoginStateUseCaseがログイン状態取得
   * 3. AuthRepositoryがnullを返す（未ログイン）
   * 4. ViewModelのメイン画面用UI状態がnullのまま
   */
  @Test
  fun `startup flow - when user not logged in - should keep null state`() = runTest {
    coEvery { authRepository.getLoginState() } returns null

    viewModel.checkLoginState()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertNull("userPubkey should be null", state.userPubkey)

    // AuthRepositoryのgetLoginState()が呼ばれることを確認
    coVerify { authRepository.getLoginState() }
  }

  /**
   * ユーザー拒否エラー → エラーメッセージ表示 → 再試行可能
   *
   * 統合フロー:
   * 1. Amberレスポンス受信 (LoginViewModel.processAmberResponse)
   * 2. AuthRepositoryでユーザー拒否エラー検出
   * 3. LoginError.UserRejectedエラーが返される
   * 4. ViewModelがエラーメッセージを設定
   * 5. ユーザーが再試行ボタンをタップ (onRetryLogin)
   */
  @Test
  fun `error flow - when user rejected - should show error and allow retry`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.UserRejected
    coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)
    every { authRepository.checkAmberInstalled() } returns true

    viewModel.processAmberResponse(-1, mockIntent)
    advanceUntilIdle()

    val stateAfterError = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", stateAfterError.errorMessage)
    assertFalse("loginSuccess should be false", stateAfterError.loginSuccess)
    assertFalse("isLoading should be false", stateAfterError.isLoading)

    // 再試行が可能であることを確認
    viewModel.onRetryLogin()
    advanceUntilIdle()

    // Amberインストール確認が再度実行される
    io.mockk.verify(atLeast = 1) { authRepository.checkAmberInstalled() }
  }

  /**
   * タイムアウトエラー → タイムアウトメッセージ表示 → 再試行可能
   *
   * 統合フロー:
   * 1. Amberレスポンス受信 (LoginViewModel.processAmberResponse)
   * 2. AuthRepositoryでタイムアウトエラー検出
   * 3. LoginError.Timeoutエラーが返される
   * 4. ViewModelがタイムアウトメッセージを設定
   */
  @Test
  fun `error flow - when timeout - should show timeout error message`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.Timeout
    coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)

    viewModel.processAmberResponse(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertTrue(
        "errorMessage should contain timeout info",
        state.errorMessage?.contains("timed out") == true)
    assertFalse("loginSuccess should be false", state.loginSuccess)
    assertFalse("isLoading should be false", state.isLoading)
  }

  /**
   * ネットワークエラー → エラーメッセージ表示
   *
   * 統合フロー:
   * 1. Amberレスポンス受信 (LoginViewModel.processAmberResponse)
   * 2. AuthRepositoryでネットワークエラー検出
   * 3. LoginError.NetworkErrorエラーが返される
   * 4. ViewModelがエラーメッセージを設定
   */
  @Test
  fun `error flow - when network error - should show network error message`() = runTest {
    val mockIntent = mockk<android.content.Intent>(relaxed = true)
    val error = LoginError.NetworkError("Connection failed")
    coEvery { authRepository.processAmberResponse(any(), any()) } returns Result.failure(error)

    viewModel.processAmberResponse(-1, mockIntent)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull("errorMessage should be set", state.errorMessage)
    assertFalse("loginSuccess should be false", state.loginSuccess)
    assertFalse("isLoading should be false", state.isLoading)
  }

  /**
   * エラーダイアログ閉じる → エラー状態クリア
   *
   * 統合フロー:
   * 1. エラーが発生してダイアログ表示
   * 2. ユーザーがエラーダイアログを閉じる (dismissError)
   * 3. ViewModelがエラー状態をクリア
   */
  @Test
  fun `error flow - when error dismissed - should clear error state`() = runTest {
    every { authRepository.checkAmberInstalled() } returns false
    viewModel.onLoginButtonClicked()
    advanceUntilIdle()

    val stateBeforeDismiss = viewModel.uiState.first()
    assertTrue("showAmberInstallDialog should be true", stateBeforeDismiss.showAmberInstallDialog)

    viewModel.dismissError()
    advanceUntilIdle()

    val stateAfterDismiss = viewModel.uiState.first()
    assertNull("errorMessage should be null", stateAfterDismiss.errorMessage)
    assertFalse("showAmberInstallDialog should be false", stateAfterDismiss.showAmberInstallDialog)
  }

  /**
   * ログアウト成功フロー → ログイン状態クリア → ログイン画面表示
   *
   * 統合フロー:
   * 1. ユーザーがログアウトボタンをタップ (LoginViewModel.onLogoutButtonClicked)
   * 2. LogoutUseCaseがログアウト処理実行
   * 3. AuthRepositoryでログイン状態クリア成功
   * 4. ViewModelがUI状態を更新 (userPubkey = null)
   */
  @Test
  fun `logout flow - when logout success - should clear login state`() = runTest {
    val testPubkey = "npub1" + "c".repeat(59)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser
    viewModel.checkLoginState()
    advanceUntilIdle()

    val stateBeforeLogout = viewModel.mainUiState.first()
    assertEquals("userPubkey should be set", testPubkey, stateBeforeLogout.userPubkey)

    // ログアウト処理が成功するようにモック設定
    coEvery { authRepository.logout() } returns Result.success(Unit)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    val stateAfterLogout = viewModel.mainUiState.first()
    assertNull("userPubkey should be null after logout", stateAfterLogout.userPubkey)
    assertFalse("isLoggingOut should be false", stateAfterLogout.isLoggingOut)

    // AuthRepositoryのlogout()が呼ばれることを確認
    coVerify { authRepository.logout() }
  }

  /**
   * ログアウト失敗フロー → エラーハンドリング → ログイン状態維持
   *
   * 統合フロー:
   * 1. ユーザーがログアウトボタンをタップ (LoginViewModel.onLogoutButtonClicked)
   * 2. LogoutUseCaseがログアウト処理実行
   * 3. AuthRepositoryでストレージエラー発生
   * 4. ViewModelがエラーをハンドリング (isLoggingOut = false)
   */
  @Test
  fun `logout flow - when logout fails - should handle error gracefully`() = runTest {
    val testPubkey = "npub1" + "d".repeat(59)
    val testUser = User(testPubkey)
    coEvery { authRepository.getLoginState() } returns testUser
    viewModel.checkLoginState()
    advanceUntilIdle()

    val error = LogoutError.StorageError("Failed to clear storage")
    coEvery { authRepository.logout() } returns Result.failure(error)

    viewModel.onLogoutButtonClicked()
    advanceUntilIdle()

    val state = viewModel.mainUiState.first()
    assertFalse("isLoggingOut should be false after error", state.isLoggingOut)

    // AuthRepositoryのlogout()が呼ばれることを確認
    coVerify { authRepository.logout() }
  }
}
