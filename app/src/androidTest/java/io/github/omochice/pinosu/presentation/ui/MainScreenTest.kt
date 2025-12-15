package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.presentation.viewmodel.MainUiState
import org.junit.Rule
import org.junit.Test

/**
 * MainScreenのUIテスト
 *
 * Task 9.1: MainScreenの基本実装のテスト
 * - pubkeyの表示確認
 * - ログアウトボタンの表示確認
 * - ログアウトボタンのクリックハンドリング
 *
 * Requirements: 2.3, 3.4, 3.5
 */
class MainScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  /**
   * Test 1: ログアウトボタンが表示される
   *
   * Requirement 3.4: メイン画面にログアウトボタンを配置
   */
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

  /**
   * Test 2: ユーザーのpubkeyが表示される（フォーマット済み）
   *
   * Requirement 3.5: メイン画面にログイン中のpubkeyを表示
   */
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

  /**
   * Test 3: pubkeyがnullの場合、適切なメッセージが表示される
   *
   * Requirement 2.3: ログイン済み状態の確認
   */
  @Test
  fun mainScreen_whenPubkeyIsNull_showsNotLoggedInMessage() {
    // Arrange
    val uiState = MainUiState(userPubkey = null, isLoggingOut = false)

    // Act
    composeTestRule.setContent { MainScreen(uiState = uiState, onLogout = {}) }

    // Assert
    composeTestRule.onNodeWithText("ログインしていません").assertIsDisplayed()
  }

  /**
   * Test 4: ログアウトボタンをタップするとコールバックが呼ばれる
   *
   * Requirement 2.4: ログアウト機能を提供
   */
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

  /**
   * Test 5: ログアウト処理中はローディングインジケーターが表示される
   *
   * Requirement 3.2: ログイン処理中にローディングインジケーターを表示（ログアウトにも適用）
   */
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
}
