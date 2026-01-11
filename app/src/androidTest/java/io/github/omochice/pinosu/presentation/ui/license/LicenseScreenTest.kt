package io.github.omochice.pinosu.presentation.ui.license

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for LicenseScreen
 *
 * Tests cover:
 * - License screen rendering
 * - TopAppBar with title display
 */
class LicenseScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun licenseScreen_shouldDisplayTitleInTopAppBar() {
    composeTestRule.setContent { LicenseScreen(onNavigateUp = {}) }

    composeTestRule.onNodeWithText("オープンソースライセンス").assertIsDisplayed()
  }
}
