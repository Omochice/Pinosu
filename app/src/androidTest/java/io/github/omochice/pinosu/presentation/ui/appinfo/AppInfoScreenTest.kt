package io.github.omochice.pinosu.presentation.ui.appinfo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for AppInfoScreen
 *
 * Tests cover:
 * - Screen title display
 * - App version display
 */
class AppInfoScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun appInfoScreen_shouldDisplayTitle() {
    composeTestRule.setContent { AppInfoScreen(onNavigateUp = {}) }

    composeTestRule.onNodeWithText("アプリ情報").assertIsDisplayed()
  }

  @Test
  fun appInfoScreen_shouldDisplayVersion() {
    composeTestRule.setContent { AppInfoScreen(onNavigateUp = {}) }

    composeTestRule.onNodeWithText("バージョン").assertIsDisplayed()
  }
}
