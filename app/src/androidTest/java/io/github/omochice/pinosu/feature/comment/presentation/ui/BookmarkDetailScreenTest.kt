package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.feature.comment.presentation.viewmodel.BookmarkDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [BookmarkDetailScreen] */
class BookmarkDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun tappingUrlOpensUriHandler() {
    val openedUris = mutableListOf<String>()
    val fakeUriHandler =
        object : UriHandler {
          override fun openUri(uri: String) {
            openedUris.add(uri)
          }
        }
    val testUrl = "https://example.com/article"

    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
        BookmarkDetailScreen(
            uiState = BookmarkDetailUiState(),
            title = "Test Bookmark",
            urls = listOf(testUrl),
            createdAt = 1_700_000_000L,
            onCommentInputChange = {},
            onPostComment = {},
            onNavigateBack = {},
            onDismissError = {})
      }
    }

    composeTestRule.onNodeWithText(testUrl).performClick()

    assertEquals(listOf(testUrl), openedUris)
  }
}
