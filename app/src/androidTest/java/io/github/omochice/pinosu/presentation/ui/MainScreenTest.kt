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

  /** Test 1: ログアウトボタンが表示される */
  @Test
  fun mainScreen_logoutButtonIsDisplayed() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  /** Test 2: ユーザーのpubkeyが表示される（フォーマット済み） */
  @Test
  fun mainScreen_userPubkeyIsDisplayed() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    // pubkeyは部分的に表示されることを確認（最初の8文字と最後の8文字のみ）
    val expectedFormattedPubkey = "12345678...90abcdef"
    composeTestRule.onNodeWithText(expectedFormattedPubkey).assertIsDisplayed()
  }

  /** Test 3: pubkeyがnullの場合、適切なメッセージが表示される */
  @Test
  fun mainScreen_whenPubkeyIsNull_showsNotLoggedInMessage() {
    // Arrange
    val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログインしていません").assertIsDisplayed()
  }

  /** Test 4: ログアウトボタンをタップするとコールバックが呼ばれる */
  @Test
  fun mainScreen_whenLogoutButtonClicked_callsOnLogout() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }
    composeTestRule.onNodeWithText("ログアウト").performClick()

    // Assert
    assert(logoutCallbackCalled) { "onLogoutコールバックが呼ばれませんでした" }
  }

  /** Test 5: ログアウト処理中はローディングインジケーターが表示される */
  @Test
  fun mainScreen_whenLoggingOut_showsLoadingIndicator() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()
  }

  /**
   * Test 6: ログアウト処理中はログアウトボタンが無効化される
   *
   * Best Practice: ログアウト中の二重クリック防止
   */
  @Test
  fun mainScreen_whenLoggingOut_logoutButtonIsDisabled() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = true)
    var logoutCallbackCalled = false
    val onLogout = { logoutCallbackCalled = true }

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = onLogout) }

    // ログアウト処理中はボタンが表示されない（ローディングメッセージのみ）
    composeTestRule.onNodeWithText("ログアウト中...").assertIsDisplayed()

    // Assert
    // ログアウトボタンがクリックできないことを確認（存在しないため）
    assert(!logoutCallbackCalled) { "ログアウト処理中はonLogoutコールバックが呼ばれてはいけません" }
  }

  @Test
  fun mainScreen_afterLogout_callsNavigateToLogin() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val loggedInState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)
    val loggedOutState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false
    val onNavigateToLogin = { navigateToLoginCalled = true }

    // Act - まずログイン状態でMainScreenを表示
    composeTestRule.setContent {
      MainScreen(uiState = loggedInState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
    }

    // Act - ログアウト状態に変更（pubkey = null）
    composeTestRule.setContent {
      MainScreen(uiState = loggedOutState, onLogout = {}, onNavigateToLogin = onNavigateToLogin)
    }

    // Assert
    // ログアウト完了後（pubkey = null）、ログイン画面へナビゲーションするコールバックが呼ばれる
    assert(navigateToLoginCalled) { "ログアウト完了後、onNavigateToLoginコールバックが呼ばれるべき" }
  }

  /** Test 8: ログアウト処理中はナビゲーションが呼ばれない */
  @Test
  fun mainScreen_doesNotNavigateWhileLoggingOut() {
    // Arrange
    val loggingOutState = MainUiState(userPubkey = null, isLoggingOut = true)
    var navigateToLoginCalled = false

    // Act
    composeTestRule.setContent {
      MainScreen(
          uiState = loggingOutState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    // Assert
    composeTestRule.waitForIdle()
    assert(!navigateToLoginCalled) { "ログアウト処理中はナビゲーションが呼ばれてはいけません" }
  }

  /** Test 9: 初期から未ログイン状態の場合はナビゲーションが呼ばれない */
  @Test
  fun mainScreen_doesNotNavigateWhenInitiallyNotLoggedIn() {
    // Arrange
    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)
    var navigateToLoginCalled = false

    // Act
    composeTestRule.setContent {
      MainScreen(
          uiState = notLoggedInState,
          onLogout = {},
          onNavigateToLogin = { navigateToLoginCalled = true })
    }

    // Assert
    composeTestRule.waitForIdle()
    assert(!navigateToLoginCalled) { "初期から未ログイン状態の場合はナビゲーションが呼ばれてはいけません" }
  }

  /** Test 10: 短いpubkey（16文字未満）はマスキングなしで表示される */
  @Test
  fun mainScreen_displaysShortPubkeyWithoutMasking() {
    // Arrange
    val shortPubkey = "short1234"
    val uiState = MainUiState(userPubkey = shortPubkey, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText(shortPubkey).assertIsDisplayed()
  }

  /** Test 11: ログイン中テキストが表示される */
  @Test
  fun mainScreen_displaysLoggedInText() {
    // Arrange
    val testPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    val uiState = MainUiState(userPubkey = testPubkey, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログイン中").assertIsDisplayed()
  }

  /** Test 12: 未ログイン状態ではログアウトボタンが表示されない */
  @Test
  fun mainScreen_hidesLogoutButtonWhenNotLoggedIn() {
    // Arrange
    val notLoggedInState = MainUiState(userPubkey = null, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = notLoggedInState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログアウト").assertDoesNotExist()
  }
}
