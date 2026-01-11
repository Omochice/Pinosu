package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for BookmarkScreen
 *
 * Tests cover:
 * - Hamburger menu icon display
 * - Hamburger menu click callback
 */
class BookmarkScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bookmarkScreen_shouldDisplayHamburgerMenuIcon() {
    composeTestRule.setContent {
      BookmarkScreen(uiState = BookmarkUiState(), onRefresh = {}, onLoad = {}, onOpenDrawer = {})
    }

    composeTestRule.onNodeWithContentDescription("Open menu").assertIsDisplayed()
  }

  @Test
  fun bookmarkScreen_hamburgerMenuClick_shouldTriggerCallback() {
    var callbackTriggered = false

    composeTestRule.setContent {
      BookmarkScreen(
          uiState = BookmarkUiState(),
          onRefresh = {},
          onLoad = {},
          onOpenDrawer = { callbackTriggered = true })
    }

    composeTestRule.onNodeWithContentDescription("Open menu").performClick()

    assertTrue(callbackTriggered)
  }
}
