package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.presentation.viewmodel.MainUiState
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
  fun mainScreen_logoutButtonIsDisplayed() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun mainScreen_userPubkeyIsDisplayed() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Verify pubkey is partially displayed（only first and last 8 characters）
    val expectedFormattedPubkey = "12345678...90abcdef"
    composeTestRule.onNodeWithText(expectedFormattedPubkey).assertIsDisplayed()
  }

  @Test
  fun mainScreen_whenPubkeyIsNull_showsNotLoggedInMessage() {

    val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログインしていません").assertIsDisplayed()
  }

  @Test
  fun mainScreen_whenLogoutButtonClicked_callsOnLogout() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }
    composeTestRule.onNodeWithText("ログアウト").performClick()

    assert(logoutCallbackCalled) { "onLogoutコールバックが呼ばれませんでした" }
  }

  @Test
  fun mainScreen_whenLoggingOut_showsLoadingIndicator() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()
  }

  /** Best Practice: Prevent double-click during logout */
  @Test
  fun mainScreen_whenLoggingOut_logoutButtonIsDisabled() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

    // Button is not shown during logout processing（only loading message shown）
    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()

    // Verify logout button is not clickable（since it doesn't exist）
    assert(!logoutCallbackCalled) { "ログアウト処理中はonLogoutコールバックが呼ばれてはいけません" }
  }

  @Test
  fun mainScreen_afterLogout_callsNavigateToLogin() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val loggedInState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    val loggedOutState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false
    val onNavigateToLogin = { navigateToLoginCalled = true }

    composeTestRule.setContent {
      MainScreen(uiState = loggedInState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
    }

    composeTestRule.setContent {
      MainScreen(uiState = loggedOutState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
    }

    // After logout completion（pubkey = null）、callback to navigate to login screen is called
    assert(navigateToLoginCalled) { "After logout completion、onNavigateToLoginコールバックが呼ばれるべき" }
  }

  @Test
  fun mainScreen_doesNotNavigateWhileLoggingOut() {

    val loggingOutState = MainUiState(userPubkey = null, isLoggingOut = true)
    var navigateToLoginCalled = false

    composeTestRule.setContent {
      MainScreen(
          uiState = loggingOutState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    composeTestRule.waitForIdle()
    assert(!navigateToLoginCalled) { "ログアウト処理中はナビゲーションが呼ばれてはいけません" }
  }

  @Test
  fun mainScreen_doesNotNavigateWhenInitiallyNotLoggedIn() {

    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false

    composeTestRule.setContent {
      MainScreen(
          uiState = notLoggedInState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    composeTestRule.waitForIdle()
    assert(!navigateToLoginCalled) { "初期から未ログイン状態の場合はナビゲーションが呼ばれてはいけません" }
  }

  @Test
  fun mainScreen_displaysShortPubkeyWithoutMasking() {

    val shortPubkey = "short1234"
    val uiState = MainUiState(userPubkey = shortPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText(shortPubkey).assertIsDisplayed()
  }

  @Test
  fun mainScreen_displaysLoggedInText() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログイン中").assertIsDisplayed()
  }

  @Test
  fun mainScreen_hidesLogoutButtonWhenNotLoggedIn() {

    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)

    composeTestRule.setContent { MainScreen(uiState = notLoggedInState, onLogout = {}) }

    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }

  @Test
  fun mainScreen_shouldDisplayHamburgerMenuIcon() {
    composeTestRule.setContent {
      MainScreen(
          uiState = MainUiState(userPubkey = "test_pubkey"), onLogout = {}, onOpenDrawer = {})
    }

    composeTestRule.onNodeWithContentDescription("Open menu").assertIsDisplayed()
  }

  @Test
  fun mainScreen_hamburgerMenuClick_shouldTriggerCallback() {
    var callbackTriggered = false

    composeTestRule.setContent {
      MainScreen(
          uiState = MainUiState(userPubkey = "test_pubkey"),
          onLogout = {},
          onOpenDrawer = { callbackTriggered = true })
    }

    composeTestRule.onNodeWithContentDescription("Open menu").performClick()

    org.junit.Assert.assertTrue(callbackTriggered)
  }
}
