package io.github.omochice.pinosu.feature.auth.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginUiState
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Rule

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

    composeTestRule
        .onNodeWithText(getTestString(R.string.button_login_with_nip55))
        .assertIsDisplayed()
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
    composeTestRule.onNodeWithText(getTestString(R.string.button_login_with_nip55)).performClick()

    assertTrue(clicked, "Login button click should trigger callback")
  }

  @Test
  fun `LoginScreen should display loading indicator when loading`() {
    composeTestRule.setContent {
      LoginScreen(uiState = LoginUiState.Loading, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.message_loading)).assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should hide loading indicator when not loading`() {
    composeTestRule.setContent {
      LoginScreen(uiState = LoginUiState.Idle, onLoginButtonClick = {}, onDismissDialog = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.message_loading)).assertIsNotDisplayed()
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
    composeTestRule.onNodeWithText(getTestString(R.string.button_login_with_nip55)).performClick()

    assertEquals(0, clickCount, "Login button should be disabled when loading")
  }

  @Test
  fun `LoginScreen should display Nip55Signer install dialog when requested`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.RequiresNip55Install,
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule
        .onNodeWithText(getTestString(R.string.dialog_title_nip55_signer_required))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText(getTestString(R.string.dialog_message_nip55_signer_required))
        .assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display generic error dialog when error message exists`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Error.NonRetryable("テストエラーメッセージ"),
          onLoginButtonClick = {},
          onDismissDialog = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.dialog_title_error)).assertIsDisplayed()
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
    composeTestRule.onNodeWithText(getTestString(R.string.button_close)).performClick()

    assertTrue(dismissCalled, "Dismiss dialog should trigger callback")
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
    composeTestRule.onNodeWithText(getTestString(R.string.button_install)).performClick()

    assertTrue(installCalled, "Install button should trigger callback")
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
    composeTestRule.onNodeWithText(getTestString(R.string.button_retry)).performClick()

    assertTrue(retryCalled, "Retry button should trigger callback")
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

    composeTestRule
        .onNodeWithText(getTestString(R.string.message_login_success))
        .assertIsDisplayed()
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
    assertTrue(navigationTriggered, "Navigation should be triggered when login succeeds")
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
        navigationTriggered, "Navigation should not be triggered when login is not successful")
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

    composeTestRule.onNodeWithText(getTestString(R.string.button_ok)).assertIsDisplayed()
  }

  @Test
  fun `LoginScreen should display read-only login button`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Idle,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onReadOnlyLoginSubmit = {})
    }

    composeTestRule
        .onNodeWithText(getTestString(R.string.button_login_read_only))
        .assertIsDisplayed()
  }

  @Test
  fun `LoginScreen read-only button should show npub input`() {
    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Idle,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onReadOnlyLoginSubmit = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.button_login_read_only)).performClick()

    composeTestRule
        .onNodeWithText(getTestString(R.string.button_submit_read_only))
        .assertIsDisplayed()
  }

  @Test
  fun `LoginScreen read-only submit should trigger callback with npub`() {
    var submittedNpub = ""

    composeTestRule.setContent {
      LoginScreen(
          uiState = LoginUiState.Idle,
          onLoginButtonClick = {},
          onDismissDialog = {},
          onReadOnlyLoginSubmit = { submittedNpub = it })
    }

    composeTestRule.onNodeWithText(getTestString(R.string.button_login_read_only)).performClick()

    val npub = "npub1" + "a".repeat(59)
    composeTestRule
        .onNode(
            androidx.compose.ui.test
                .hasSetTextAction()
                .and(
                    androidx.compose.ui.test
                        .hasText("npub1…")
                        .or(androidx.compose.ui.test.hasText(""))))
        .performTextInput(npub)

    composeTestRule.onNodeWithText(getTestString(R.string.button_submit_read_only)).performClick()

    assertEquals(npub, submittedNpub, "Should submit the entered npub")
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

    composeTestRule.onNodeWithText(getTestString(R.string.button_retry)).assertIsDisplayed()

    composeTestRule.onNodeWithText(getTestString(R.string.button_cancel)).assertIsDisplayed()
  }
}
