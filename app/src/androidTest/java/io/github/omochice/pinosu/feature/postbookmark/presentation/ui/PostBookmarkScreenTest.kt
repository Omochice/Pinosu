package io.github.omochice.pinosu.feature.postbookmark.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.postbookmark.presentation.viewmodel.PostBookmarkUiState
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertFalse
import org.junit.Rule

/** Compose UI tests for [PostBookmarkScreen] */
class PostBookmarkScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun urlFieldShouldBeReadOnlyInEditMode() {
    var urlChanged = false
    composeTestRule.setContent {
      PostBookmarkScreen(
          uiState =
              PostBookmarkUiState(
                  url = "example.com/article",
                  title = "Title",
                  categories = "tech",
                  comment = "Comment",
                  isEditMode = true),
          onUrlChange = { urlChanged = true },
          onTitleChange = {},
          onCategoriesChange = {},
          onCommentChange = {},
          onPostClick = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule.onNodeWithTag(URL_FIELD_TEST_TAG).assertIsDisplayed()
    assertFalse(urlChanged, "onUrlChange should not be called in read-only mode")
  }

  @Test
  fun titleShouldShowEditBookmarkInEditMode() {
    composeTestRule.setContent {
      PostBookmarkScreen(
          uiState = PostBookmarkUiState(url = "example.com/article", isEditMode = true),
          onUrlChange = {},
          onTitleChange = {},
          onCategoriesChange = {},
          onCommentChange = {},
          onPostClick = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.title_edit_bookmark)).assertExists()
  }

  @Test
  fun titleShouldShowAddBookmarkInCreateMode() {
    composeTestRule.setContent {
      PostBookmarkScreen(
          uiState = PostBookmarkUiState(),
          onUrlChange = {},
          onTitleChange = {},
          onCategoriesChange = {},
          onCommentChange = {},
          onPostClick = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.title_post_bookmark)).assertExists()
  }
}
