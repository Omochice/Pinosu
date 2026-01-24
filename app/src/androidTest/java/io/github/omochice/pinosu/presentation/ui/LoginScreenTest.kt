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
 * Compose UI tests for LoginScreen
 * - Login button display test
 * - Loading indicator display test
 * - UI display switching based on state
 */
class LoginScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `LoginScreen should display login button`() {
    val initialState = LoginUiState()

    composeTestRule.setContent {
      LoginScreen(uiState = initialState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen login button click should trigger callback`() {
    val initialState = LoginUiState()
    var clicked = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = initialState, onLoginButtonClick = { clicked = true }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    assert(clicked) { "Login button click should trigger callback" }
  }

  @Test
  fun `LoginScreen should display loading indicator when loading`() {
    val loadingState = LoginUiState(isLoading = true)

    composeTestRule.setContent {
      LoginScreen(uiState = loadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should hide loading indicator when not loading`() {
    val notLoadingState = LoginUiState(isLoading = false)

    composeTestRule.setContent {
      LoginScreen(uiState = notLoadingState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsNotDisplayed()
  }

  @Test
  fun `LoginScreen should disable login button when loading`() {
    val loadingState = LoginUiState(isLoading = true)
    var clickCount = 0

    composeTestRule.setContent {
      LoginScreen(
          uiState = loadingState, onLoginButtonClick = { clickCount++ }, onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    assert(clickCount == 0) { "Login button should be disabled when loading" }
  }

  @Test
  fun `LoginScreen should display Nip55Signer install dialog when requested`() {
    val dialogState = LoginUiState(showNip55InstallDialog = true)

    composeTestRule.setContent {
      LoginScreen(uiState = dialogState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertIsDisplayed()
    composeTestRule.onNodeWithText("このアプリを使用するにはNIP-55対応アプリのインストールが必要です。").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display generic error dialog when error message exists`() {
    val errorState = LoginUiState(errorMessage = "テストエラーメッセージ")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("エラー").assertIsDisplayed()
    composeTestRule.onNodeWithText("テストエラーメッセージ").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen dismiss dialog should call callback`() {
    val dialogState = LoginUiState(showNip55InstallDialog = true)
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
  fun `LoginScreen install button should open Play Store`() {
    val dialogState = LoginUiState(showNip55InstallDialog = true)
    var installCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = dialogState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onInstallNip55Signer = { installCalled = true })
    }
    composeTestRule.onNodeWithText("インストール").performClick()

    assert(installCalled) { "Install button should trigger callback" }
  }

  @Test
  fun `LoginScreen retry button should call callback`() {
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
  fun `LoginScreen should display success message when login succeeds`() {
    val successState = LoginUiState(loginSuccess = true)

    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = {})
    }

    composeTestRule.onNodeWithText("ログインに成功しました").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should trigger navigation when login succeeds`() {
    val successState = LoginUiState(loginSuccess = true)
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = successState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = { navigationTriggered = true })
    }

    composeTestRule.waitUntil(timeoutMillis = 1000) { navigationTriggered }
    assert(navigationTriggered) { "Navigation should be triggered when login succeeds" }
  }

  @Test
  fun `LoginScreen should not trigger navigation when login not successful`() {
    val notSuccessState = LoginUiState(loginSuccess = false)
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = notSuccessState,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = { navigationTriggered = true })
    }

    composeTestRule.waitForIdle()
    assert(!navigationTriggered) {
      "Navigation should not be triggered when login is not successful"
    }
  }

  @Test
  fun `LoginScreen should display user rejection error message`() {
    val errorState = LoginUiState(errorMessage = "ログインがキャンセルされました。再度お試しください。")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("ログインがキャンセルされました。再度お試しください。").assertIsDisplayed()

    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display timeout error with retry option`() {
    val errorState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。NIP-55対応アプリを確認して再試行してください。")

    composeTestRule.setContent {
      LoginScreen(uiState = errorState, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule
        .onNodeWithText("ログイン処理がタイムアウトしました。NIP-55対応アプリを確認して再試行してください。")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("再試行").assertIsDisplayed()

    composeTestRule.onNodeWithText("キャンセル").assertIsDisplayed()
  }
}
