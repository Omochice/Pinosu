package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
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
            bookmarkInfo =
                BookmarkInfo(
                    title = "Test Bookmark", urls = listOf(testUrl), createdAt = 1_700_000_000L),
            onCommentInputChange = {},
            onPostComment = {},
            onNavigateBack = {},
            onDismissError = {})
      }
    }

    composeTestRule.onNodeWithText(testUrl).performClick()

    assertEquals(listOf(testUrl), openedUris)
  }

  @Test
  fun displaysBothKind1AndKind1111Comments() {
    val quoteComment =
        Comment(
            id = "q1",
            content = "Quoted text note content",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            isAuthorComment = false,
            kind = Comment.KIND_TEXT_NOTE)
    val regularComment =
        Comment(
            id = "c1",
            content = "Regular NIP-22 comment",
            authorPubkey = "pk2",
            createdAt = 1_700_000_100L,
            isAuthorComment = false,
            kind = Comment.KIND_COMMENT)

    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(comments = listOf(quoteComment, regularComment)),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test Bookmark",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule.onNodeWithText("Quoted text note content").assertIsDisplayed()
    composeTestRule.onNodeWithText("Regular NIP-22 comment").assertIsDisplayed()
  }

  @Test
  fun commentInputBarShouldBeVisibleWhenNotReadOnly() {
    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test", urls = listOf("https://example.com"), createdAt = 1_700_000_000L),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          isReadOnly = false)
    }

    composeTestRule.onNodeWithContentDescription("コメントを投稿").assertIsDisplayed()
  }

  @Test
  fun commentInputBarShouldBeHiddenWhenReadOnly() {
    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test", urls = listOf("https://example.com"), createdAt = 1_700_000_000L),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          isReadOnly = true)
    }

    composeTestRule.onNodeWithContentDescription("コメントを投稿").assertDoesNotExist()
  }
}
