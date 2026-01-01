package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.presentation.viewmodel.LoginUiState
import org.junit.Rule
import org.junit.Test

/**
 * LoginScreenのCompose UIテスト
 * - ログインボタン表示のテスト
 * - ローディングインジケーター表示のテスト
 * - UI状態に応じた表示切り替えのテスト
 */
class LoginScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ========== ログインボタン表示のテスト ==========

  @Test
  fun loginScreen_displaysLoginButton() {
    // Given: 初期状態のLoginUiState
    val initialState = LoginUiState()

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = initialState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: 「Amberでログイン」ボタンが表示される
    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  @Test
  fun loginScreen_loginButtonClickTriggersCallback() {
    // Given: 初期状態のLoginUiState
    val initialState = LoginUiState()
    var clicked = false

    // When: LoginScreenを表示してボタンをクリック
    composeTestRule.setContent {
      LoginScreen(
          uiState = initialState, onLoginButtonClick = { clicked = true }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Then: コールバックが呼ばれる
    assert(clicked) { "Login button click should trigger callback" }
  }

  // ========== ローディングインジケーター表示のテスト ==========

  @Test
  fun loginScreen_displaysLoadingIndicatorWhenLoading() {
    // Given: isLoading = true のLoginUiState
    val loadingState = LoginUiState(isLoading = true)

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = loadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: ローディングインジケーターが表示される
    composeTestRule.onNodeWithText("読み込み中...").assertIsDisplayed()
  }

  @Test
  fun loginScreen_hidesLoadingIndicatorWhenNotLoading() {
    // Given: isLoading = false のLoginUiState
    val notLoadingState = LoginUiState(isLoading = false)

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = notLoadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: ローディングインジケーターが表示されない
    composeTestRule.onNodeWithText("読み込み中...").assertIsNotDisplayed()
  }

  // ========== ボタン無効化のテスト ==========

  @Test
  fun loginScreen_disablesLoginButtonWhenLoading() {
    // Given: isLoading = true のLoginUiState
    val loadingState = LoginUiState(isLoading = true)
    var clickCount = 0

    // When: LoginScreenを表示してボタンをクリック
    composeTestRule.setContent {
      LoginScreen(
          uiState = loadingState, onLoginButtonClick = { clickCount++ }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Then: ボタンが無効化されているためクリックイベントが発火しない
    assert(clickCount == 0) { "Login button should be disabled when loading" }
  }

  @Test
  fun loginScreen_displaysAmberInstallDialogWhenRequested() {
    // Given: showAmberInstallDialog = true のLoginUiState
    val dialogState = LoginUiState(showAmberInstallDialog = true)

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = dialogState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: Amber未インストールダイアログが表示される
    composeTestRule.onNodeWithText("Amberアプリが必要です").assertIsDisplayed()
    composeTestRule.onNodeWithText("このアプリを使用するにはAmberアプリのインストールが必要です。").assertIsDisplayed()
  }

  @Test
  fun loginScreen_displaysGenericErrorDialogWhenErrorMessageExists() {
    // Given: errorMessage が設定されたLoginUiState
    val errorState = LoginUiState(errorMessage = "テストエラーメッセージ")

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: エラーダイアログが表示される
    composeTestRule.onNodeWithText("エラー").assertIsDisplayed()
    composeTestRule.onNodeWithText("テストエラーメッセージ").assertIsDisplayed()
  }

  @Test
  fun loginScreen_dismissDialogCallsCallback() {
    // Given: showAmberInstallDialog = true のLoginUiState
    val dialogState = LoginUiState(showAmberInstallDialog = true)
    var dismissCalled = false

    // When: LoginScreenを表示してダイアログを閉じる
    composeTestRule.setContent {
      LoginScreen(
          uiState = dialogState,
          onLoginButtonClick = {},
          onDismissDialog = { dismissCalled = true })
    }
    composeTestRule.onNodeWithText("閉じる").performClick()

    // Then: dismissコールバックが呼ばれる
    assert(dismissCalled) { "Dismiss dialog should trigger callback" }
  }

  @Test
  fun loginScreen_installButtonOpensPlayStore() {
    // Given: showAmberInstallDialog = true のLoginUiState
    val dialogState = LoginUiState(showAmberInstallDialog = true)
    var installCalled = false

    // When: LoginScreenを表示してインストールボタンをクリック
    composeTestRule.setContent {
      LoginScreen(
          uiState = dialogState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onInstallAmber = { installCalled = true })
    }
    composeTestRule.onNodeWithText("インストール").performClick()

    // Then: installコールバックが呼ばれる
    assert(installCalled) { "Install button should trigger callback" }
  }

  @Test
  fun loginScreen_retryButtonCallsCallback() {
    // Given: タイムアウトエラーメッセージのLoginUiState
    val errorState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。")
    var retryCalled = false

    // When: LoginScreenを表示して再試行ボタンをクリック
    composeTestRule.setContent {
      LoginScreen(
          uiState = errorState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onRetry = { retryCalled = true })
    }
    composeTestRule.onNodeWithText("再試行").performClick()

    // Then: retryコールバックが呼ばれる
    assert(retryCalled) { "Retry button should trigger callback" }
  }

  @Test
  fun loginScreen_displaysSuccessMessageWhenLoginSucceeds() {
    // Given: loginSuccess = true のLoginUiState
    val successState = LoginUiState(loginSuccess = true)

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = {})
    }

    // Then: ログイン成功メッセージが表示される
    composeTestRule.onNodeWithText("ログインに成功しました").assertIsDisplayed()
  }

  @Test
  fun loginScreen_triggersNavigationWhenLoginSucceeds() {
    // Given: loginSuccess = true のLoginUiState
    val successState = LoginUiState(loginSuccess = true)
    var navigationTriggered = false

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = { navigationTriggered = true })
    }

    // Then: ナビゲーションコールバックが呼ばれる（LaunchedEffectによる自動遷移）
    composeTestRule.waitUntil(timeoutMillis = 1000) { navigationTriggered }
    assert(navigationTriggered) { "Navigation should be triggered when login succeeds" }
  }

  @Test
  fun loginScreen_doesNotTriggerNavigationWhenLoginNotSuccessful() {
    // Given: loginSuccess = false のLoginUiState
    val notSuccessState = LoginUiState(loginSuccess = false)
    var navigationTriggered = false

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(
          uiState = notSuccessState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = { navigationTriggered = true })
    }

    // Then: ナビゲーションコールバックは呼ばれない
    composeTestRule.waitForIdle()
    assert(!navigationTriggered) {
      "Navigation should not be triggered when login is not successful"
    }
  }

  @Test
  fun loginScreen_displaysUserRejectionErrorMessage() {
    // Given: ユーザー拒否エラーメッセージのLoginUiState
    val errorState = LoginUiState(errorMessage = "ログインがキャンセルされました。再度お試しください。")

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: ユーザー拒否エラーメッセージが表示される
    composeTestRule.onNodeWithText("ログインがキャンセルされました。再度お試しください。").assertIsDisplayed()

    // Then: OKボタンが表示される（タイムアウトではないため再試行ボタンは表示されない）
    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun loginScreen_displaysTimeoutErrorWithRetryOption() {
    // Given: タイムアウトエラーメッセージのLoginUiState
    val errorState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。Amberアプリを確認して再試行してください。")

    // When: LoginScreenを表示
    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    // Then: タイムアウトエラーメッセージが表示される
    composeTestRule.onNodeWithText("ログイン処理がタイムアウトしました。Amberアプリを確認して再試行してください。").assertIsDisplayed()

    // Then: 再試行ボタンが表示される
    composeTestRule.onNodeWithText("再試行").assertIsDisplayed()

    // Then: キャンセルボタンも表示される
    composeTestRule.onNodeWithText("キャンセル").assertIsDisplayed()
  }
}
