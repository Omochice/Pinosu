package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for long-press on bookmark card */
class BookmarkScreenLongPressTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun longPressOnBookmarkCardTriggersCallbackWithRawJson() {
    val expectedRawJson =
        """{"id":"abc","pubkey":"def","created_at":1000,"kind":39701,"tags":[],"content":"hello","sig":"xyz"}"""
    var capturedRawJson: String? = null

    val bookmarks =
        listOf(
            BookmarkItem(
                type = "event",
                eventId = "abc",
                title = "Test Bookmark",
                url = "https://example.com",
                urls = listOf("https://example.com"),
                rawJson = expectedRawJson,
                event =
                    BookmarkedEvent(
                        kind = 39701,
                        content = "hello",
                        author = "def",
                        createdAt = 1000,
                        tags = emptyList())))

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(isLoading = false, allBookmarks = bookmarks, userHexPubkey = "def"),
          onRefresh = {},
          onLoad = {},
          onLongPressBookmark = { rawJson -> capturedRawJson = rawJson })
    }

    composeTestRule.onNodeWithText("Test Bookmark").performTouchInput { longClick() }

    assertEquals("Callback should receive rawJson", expectedRawJson, capturedRawJson)
  }
}
