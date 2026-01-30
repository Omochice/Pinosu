package io.github.omochice.pinosu.feature.auth.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    composeTestRule.setContent {
      LoginScreen(uiState = LoginUiState.Idle, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen login button click should trigger callback`() {
    var clicked = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Idle,
          onLoginButtonClick = { clicked = true },
          onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    assertTrue("Login button click should trigger callback", clicked)
  }

  @Test
  fun `LoginScreen should display loading indicator when loading`() {
    composeTestRule.setContent {
      LoginScreen(uiState = LoginUiState.Loading, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should hide loading indicator when not loading`() {
    composeTestRule.setContent {
      LoginScreen(uiState = LoginUiState.Idle, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("読み込み中...").assertIsNotDisplayed()
  }

  @Test
  fun `LoginScreen should disable login button when loading`() {
    var clickCount = 0

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Loading,
          onLoginButtonClick = { clickCount++ },
          onDismissDialog = {})
    }
    composeTestRule.onNodeWithText("NIP-55対応アプリでログイン").performClick()

    assertEquals("Login button should be disabled when loading", 0, clickCount)
  }

  @Test
  fun `LoginScreen should display Nip55Signer install dialog when requested`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.RequiresNip55Install,
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("NIP-55対応アプリが必要です").assertIsDisplayed()
    composeTestRule.onNodeWithText("このアプリを使用するにはNIP-55対応アプリのインストールが必要です。").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display generic error dialog when error message exists`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Error.NonRetryable("テストエラーメッセージ"),
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("エラー").assertIsDisplayed()
    composeTestRule.onNodeWithText("テストエラーメッセージ").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen dismiss dialog should call callback`() {
    var dismissCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.RequiresNip55Install,
          onLoginButtonClick = {},
          onDismissDialog = { dismissCalled = true })
    }
    composeTestRule.onNodeWithText("閉じる").performClick()

    assertTrue("Dismiss dialog should trigger callback", dismissCalled)
  }

  @Test
  fun `LoginScreen install button should open Play Store`() {
    var installCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.RequiresNip55Install,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onInstallNip55Signer = { installCalled = true })
    }
    composeTestRule.onNodeWithText("インストール").performClick()

    assertTrue("Install button should trigger callback", installCalled)
  }

  @Test
  fun `LoginScreen retry button should call callback`() {
    var retryCalled = false

    composeTestRule.setContent {
      LoginScreen(
          uiState =
              LoginUiState.Error.Retryable(
                  "Login process timed out. Please check the NIP-55 signer app and retry."),
          onLoginButtonClick = {},
          onDismissDialog = {},
          onRetry = { retryCalled = true })
    }
    composeTestRule.onNodeWithText("再試行").performClick()

    assertTrue("Retry button should trigger callback", retryCalled)
  }

  @Test
  fun `LoginScreen should display success message when login succeeds`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Success,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = {})
    }

    composeTestRule.onNodeWithText("ログインに成功しました").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should trigger navigation when login succeeds`() {
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Success,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = { navigationTriggered = true })
    }

    composeTestRule.waitUntil(timeoutMillis = 1000) { navigationTriggered }
    assertTrue("Navigation should be triggered when login succeeds", navigationTriggered)
  }

  @Test
  fun `LoginScreen should not trigger navigation when login not successful`() {
    var navigationTriggered = false

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Idle,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onLoginSuccess = { navigationTriggered = true })
    }

    composeTestRule.waitForIdle()
    assertFalse(
        "Navigation should not be triggered when login is not successful", navigationTriggered)
  }

  @Test
  fun `LoginScreen should display user rejection error message`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Error.NonRetryable("ログインがキャンセルされました。再度お試しください。"),
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule.onNodeWithText("ログインがキャンセルされました。再度お試しください。").assertIsDisplayed()

    composeTestRule.onNodeWithText("OK").assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display timeout error with retry option`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState =
              LoginUiState.Error.Retryable(
                  "Login process timed out. Please check the NIP-55 signer app and retry."),
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule
        .onNodeWithText("Login process timed out. Please check the NIP-55 signer app and retry.")
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("再試行").assertIsDisplayed()

    composeTestRule.onNodeWithText("キャンセル").assertIsDisplayed()
  }
}
