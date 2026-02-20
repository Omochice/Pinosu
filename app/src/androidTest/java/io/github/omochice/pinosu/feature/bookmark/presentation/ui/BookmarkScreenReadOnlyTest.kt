package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for BookmarkScreen read-only mode
 *
 * Verifies that the FAB is hidden when isReadOnly is true and shown when false.
 */
class BookmarkScreenReadOnlyTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `FAB should be visible when not in read-only mode`() {
    composeTestRule.setContent {
      BookmarkScreen(
          uiState = BookmarkUiState(isLoading = false),
          onRefresh = {},
          onLoad = {},
          isReadOnly = false)
    }

    composeTestRule.onNodeWithContentDescription("ブックマークを追加").assertIsDisplayed()
  }

  @Test
  fun `FAB should be hidden when in read-only mode`() {
    composeTestRule.setContent {
      BookmarkScreen(
          uiState = BookmarkUiState(isLoading = false),
          onRefresh = {},
          onLoad = {},
          isReadOnly = true)
    }

    composeTestRule.onNodeWithContentDescription("ブックマークを追加").assertDoesNotExist()
  }
}
