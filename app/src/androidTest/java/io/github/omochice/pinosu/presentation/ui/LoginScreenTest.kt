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

    val initialState = LoginUiState()

    composeTestRule.setContent {
      LoginScreen(uiState = initialState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("Amberでログイン").assertIsDisplayed()
  }

  @Test
  fun loginScreen_loginButtonClickTriggersCallback() {

    val initialState = LoginUiState()
    var clicked = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = initialState, onLoginButtonClick = { clicked = true }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    assert(clicked) { "Login button click should trigger callback" }
  }

  // ========== ローディングインジケーター表示のテスト ==========

  @Test
  fun loginScreen_displaysLoadingIndicatorWhenLoading() {

    val loadingState = LoginUiState(isLoading = true)

    composeTestRule.setContent {
      LoginScreen(uiState = loadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsDisplayed()
  }

  @Test
  fun loginScreen_hidesLoadingIndicatorWhenNotLoading() {

    val notLoadingState = LoginUiState(isLoading = false)

    composeTestRule.setContent {
      LoginScreen(uiState = notLoadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsNotDisplayed()
  }

  // ========== ボタン無効化のテスト ==========

  @Test
  fun loginScreen_disablesLoginButtonWhenLoading() {

    val loadingState = LoginUiState(isLoading = true)
    var clickCount = 0

    composeTestRule.setContent {
      LoginScreen(
          uiState = loadingState, onLoginButtonClick = { clickCount++ }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    assert(clickCount == 0) { "Login button should be disabled when loading" }
  }

  @Test
  fun loginScreen_displaysAmberInstallDialogWhenRequested() {

    val dialogState = LoginUiState(showAmberInstallDialog = true)

    composeTestRule.setContent {
      LoginScreen(uiState = dialogState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("Amberアプリが必要です").assertIsDisplayed()
    composeTestRule.onNodeWithText("このアプリを使用するにはAmberアプリのインストールが必要です。").assertIsDisplayed()
  }

  @Test
  fun loginScreen_displaysGenericErrorDialogWhenErrorMessageExists() {

    val errorState = LoginUiState(errorMessage = "テストエラーメッセージ")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("エラー").assertIsDisplayed()
    composeTestRule.onNodeWithText("テストエラーメッセージ").assertIsDisplayed()
  }

  @Test
  fun loginScreen_dismissDialogCallsCallback() {

    val dialogState = LoginUiState(showAmberInstallDialog = true)
    var dismissCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = dialogState,
          onLoginButtonClick = {},
          onDismissDialog = { dismissCalled = true })
    }
    composeTestRule.onNodeWithText("閉じる").performClick()

    assert(dismissCalled) { "Dismiss dialog should trigger callback" }
  }

  @Test
  fun loginScreen_installButtonOpensPlayStore() {

    val dialogState = LoginUiState(showAmberInstallDialog = true)
    var installCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = dialogState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onInstallAmber = { installCalled = true })
    }
    composeTestRule.onNodeWithText("インストール").performClick()

    assert(installCalled) { "Install button should trigger callback" }
  }

  @Test
  fun loginScreen_retryButtonCallsCallback() {

    val errorState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。")
    var retryCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = errorState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onRetry = { retryCalled = true })
    }
    composeTestRule.onNodeWithText("再試行").performClick()

    assert(retryCalled) { "Retry button should trigger callback" }
  }

  @Test
  fun loginScreen_displaysSuccessMessageWhenLoginSucceeds() {

    val successState = LoginUiState(loginSuccess = true)

    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = {})
    }

    composeTestRule.onNodeWithText("ログインに成功しました").assertIsDisplayed()
  }

  @Test
  fun loginScreen_triggersNavigationWhenLoginSucceeds() {

    val successState = LoginUiState(loginSuccess = true)
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = { navigationTriggered = true })
    }

    composeTestRule.waitUntil(timeoutMillis = 1000) { navigationTriggered }
    assert(navigationTriggered) { "Navigation should be triggered when login succeeds" }
  }

  @Test
  fun loginScreen_doesNotTriggerNavigationWhenLoginNotSuccessful() {

    val notSuccessState = LoginUiState(loginSuccess = false)
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = notSuccessState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onNavigateToMain = { navigationTriggered = true })
    }

    composeTestRule.waitForIdle()
    assert(!navigationTriggered) {
      "Navigation should not be triggered when login is not successful"
    }
  }

  @Test
  fun loginScreen_displaysUserRejectionErrorMessage() {

    val errorState = LoginUiState(errorMessage = "ログインがキャンセルされました。再度お試しください。")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("ログインがキャンセルされました。再度お試しください。").assertIsDisplayed()

    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun loginScreen_displaysTimeoutErrorWithRetryOption() {

    val errorState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。Amberアプリを確認して再試行してください。")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("ログイン処理がタイムアウトしました。Amberアプリを確認して再試行してください。").assertIsDisplayed()

    composeTestRule.onNodeWithText("再試行").assertIsDisplayed()

    composeTestRule.onNodeWithText("キャンセル").assertIsDisplayed()
  }
}
