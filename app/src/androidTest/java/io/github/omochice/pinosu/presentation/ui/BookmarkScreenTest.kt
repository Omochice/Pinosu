package io.github.omochice.pinosu.presentation.ui

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkUiState
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for BookmarkScreen
 * - Title source indicator display tests
 */
class BookmarkScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `BookmarkItemCard should not display title source indicator`() {
    val bookmark =
        BookmarkItem(
            type = "event",
            eventId = "test123",
            title = "Test Bookmark Title",
            url = "https://example.com",
            urls = listOf("https://example.com"),
            event =
                BookmarkedEvent(
                    kind = 39701,
                    content = "Test content",
                    author = "testauthor",
                    createdAt = System.currentTimeMillis() / 1000,
                    tags = emptyList()))

    val uiState =
        BookmarkUiState(
            isLoading = false, bookmarks = listOf(bookmark), displayMode = BookmarkDisplayMode.List)

    composeTestRule.setContent { BookmarkScreen(uiState = uiState, onRefresh = {}, onLoad = {}) }

    composeTestRule.onNodeWithText("(OGメタデータから取得したタイトル)").assertDoesNotExist()
  }
}
