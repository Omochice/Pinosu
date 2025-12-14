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
 *
 * Task 8.1: LoginScreenの基本実装
 * - ログインボタン表示のテスト
 * - ローディングインジケーター表示のテスト
 * - UI状態に応じた表示切り替えのテスト
 *
 * Requirements: 3.1, 3.2
 */
class LoginScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ========== ログインボタン表示のテスト ==========

  @Test
  fun loginScreen_displaysLoginButton() {
    // Given: 初期状態のLoginUiState
    val initialState = LoginUiState()

    // When: LoginScreenを表示
    composeTestRule.setContent { LoginScreen(uiState = initialState, onLoginButtonClick = {}) }

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
      LoginScreen(uiState = initialState, onLoginButtonClick = { clicked = true })
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
    composeTestRule.setContent { LoginScreen(uiState = loadingState, onLoginButtonClick = {}) }

    // Then: ローディングインジケーターが表示される
    composeTestRule.onNodeWithText("読み込み中...").assertIsDisplayed()
  }

  @Test
  fun loginScreen_hidesLoadingIndicatorWhenNotLoading() {
    // Given: isLoading = false のLoginUiState
    val notLoadingState = LoginUiState(isLoading = false)

    // When: LoginScreenを表示
    composeTestRule.setContent { LoginScreen(uiState = notLoadingState, onLoginButtonClick = {}) }

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
      LoginScreen(uiState = loadingState, onLoginButtonClick = { clickCount++ })
    }
    composeTestRule.onNodeWithText("Amberでログイン").performClick()

    // Then: ボタンが無効化されているためクリックイベントが発火しない
    assert(clickCount == 0) { "Login button should be disabled when loading" }
  }
}
