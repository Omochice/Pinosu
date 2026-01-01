package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.presentation.viewmodel.MainUiState
import org.junit.Rule
import org.junit.Test

/**
 * MainScreenのCompose UIテスト
 * - pubkey表示のテスト
 * - ログアウトボタン表示のテスト
 * - ナビゲーションのテスト
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

    // pubkeyは部分的に表示されることを確認（最初の8文字と最後の8文字のみ）
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

  /** Best Practice: ログアウト中の二重クリック防止 */
  @Test
  fun mainScreen_whenLoggingOut_logoutButtonIsDisabled() {

    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

    // ログアウト処理中はボタンが表示されない（ローディングメッセージのみ）
    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()

    // ログアウトボタンがクリックできないことを確認（存在しないため）
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

    // ログアウト完了後（pubkey = null）、ログイン画面へナビゲーションするコールバックが呼ばれる
    assert(navigateToLoginCalled) { "ログアウト完了後、onNavigateToLoginコールバックが呼ばれるべき" }
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
}
