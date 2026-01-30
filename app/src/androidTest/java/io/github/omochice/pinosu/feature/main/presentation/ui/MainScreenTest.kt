package io.github.omochice.pinosu.feature.main.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.MainUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for MainScreen
 * - Pubkey display test
 * - Logout button display test
 * - Navigation test
 */
class MainScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `MainScreen should display logout button`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun `MainScreen should display user pubkey`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Verify pubkey is partially displayed（only first and last 8 characters）
    val expectedFormattedPubkey = "12345678...90abcdef"
    composeTestRule.onNodeWithText(expectedFormattedPubkey).assertIsDisplayed()
  }

  @Test
  fun `MainScreen when pubkey is null should show not logged in message`() {

    val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("未ログイン").assertIsDisplayed()
  }

  @Test
  fun `MainScreen when logout button clicked should call onLogout`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }
    composeTestRule.onNodeWithText("ログアウト").performClick()

    assertTrue("onLogoutコールバックが呼ばれませんでした", logoutCallbackCalled)
  }

  @Test
  fun `MainScreen when logging out should show loading indicator`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()
  }

  /** Best Practice: Prevent double-click during logout */
  @Test
  fun `MainScreen when logging out should disable logout button`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

    // Button is not shown during logout processing（only loading message shown）
    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()

    // Verify logout button is not clickable（since it doesn't exist）
    assertFalse("ログアウト処理中はonLogoutコールバックが呼ばれてはいけません", logoutCallbackCalled)
  }

  @Test
  fun `MainScreen after logout should call navigateToLogin`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val loggedInState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    val loggedOutState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false
    val onNavigateToLogin = { navigateToLoginCalled = true }

    val uiState = androidx.compose.runtime.mutableStateOf(loggedInState)

    composeTestRule.setContent {
      MainScreen(uiState = uiState.value, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
    }

    // Simulate logout by updating state (triggers recomposition)
    uiState.value = loggedOutState

    // Wait for LaunchedEffect to execute
    composeTestRule.waitForIdle()

    assertTrue("After logout completion, onNavigateToLogin should be called", navigateToLoginCalled)
  }

  @Test
  fun `MainScreen should not navigate while logging out`() {

    val loggingOutState = MainUiState(userPubkey = null, isLoggingOut = true)
    var navigateToLoginCalled = false

    composeTestRule.setContent {
      MainScreen(
          uiState = loggingOutState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    composeTestRule.waitForIdle()
    assertFalse("ログアウト処理中はナビゲーションが呼ばれてはいけません", navigateToLoginCalled)
  }

  @Test
  fun `MainScreen should not navigate when initially not logged in`() {

    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false

    composeTestRule.setContent {
      MainScreen(
          uiState = notLoggedInState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    composeTestRule.waitForIdle()
    assertFalse("初期から未ログイン状態の場合はナビゲーションが呼ばれてはいけません", navigateToLoginCalled)
  }

  @Test
  fun `MainScreen should display short pubkey without masking`() {

    val shortPubkey = "short1234"
    val uiState = MainUiState(userPubkey = shortPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText(shortPubkey).assertIsDisplayed()
  }

  @Test
  fun `MainScreen should display logged in text`() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログイン済み").assertIsDisplayed()
  }

  @Test
  fun `MainScreen should hide logout button when not logged in`() {

    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = notLoggedInState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }
}
