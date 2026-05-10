package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.feature.comment.presentation.viewmodel.BookmarkDetailUiState
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Rule

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
                    title = "Test Bookmark",
                    urls = listOf(testUrl),
                    createdAt = 1_700_000_000L,
                    authorPubkey = "pk_author"),
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
            kind = Comment.KIND_TEXT_NOTE)
    val regularComment =
        Comment(
            id = "c1",
            content = "Regular NIP-22 comment",
            authorPubkey = "pk2",
            createdAt = 1_700_000_100L,
            kind = Comment.KIND_COMMENT)

    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(comments = listOf(quoteComment, regularComment)),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test Bookmark",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
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
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          isReadOnly = false)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_post_comment))
        .assertIsDisplayed()
  }

  @Test
  fun editButtonShouldBeVisibleWhenOnEditBookmarkIsProvided() {
    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          onEditBookmark = {})
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_edit_bookmark))
        .assertIsDisplayed()
  }

  @Test
  fun editButtonShouldBeHiddenWhenOnEditBookmarkIsNull() {
    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_edit_bookmark))
        .assertDoesNotExist()
  }

  @Test
  fun commentInputBarShouldBeHiddenWhenReadOnly() {
    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          isReadOnly = true)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_post_comment))
        .assertDoesNotExist()
  }

  @Test
  fun selectingCopyNostrLinkOnKind1111CommentInvokesCallbackWithNEvent() {
    var capturedNostrLink: String? = null
    val pubkey = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"
    val eventId = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"
    val comment =
        Comment(
            id = eventId,
            content = "Shareable comment",
            authorPubkey = pubkey,
            createdAt = 1_700_000_000L,
            kind = Comment.KIND_COMMENT,
            event =
                NostrEvent(
                    id = eventId,
                    pubkey = pubkey,
                    createdAt = 1_700_000_000L,
                    kind = Comment.KIND_COMMENT,
                    tags = emptyList(),
                    content = "Shareable comment",
                    sig = "sig"))

    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(comments = listOf(comment)),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {},
          onCopyNostrLink = { encoded -> capturedNostrLink = encoded })
    }

    composeTestRule.onNodeWithText("Shareable comment").performTouchInput { longClick() }
    composeTestRule.onNodeWithText(getTestString(R.string.menu_copy_nostr_link)).performClick()

    assertNotNull(capturedNostrLink, "Tapping Copy nostr link should invoke the callback")
    assertTrue(
        capturedNostrLink!!.startsWith("nostr:nevent1"),
        "Encoded value should be a nostr:nevent1 URI but was '$capturedNostrLink'")
  }

  @Test
  fun copyNostrLinkIsHiddenForSyntheticAuthorCommentWithoutEvent() {
    val syntheticAuthorComment =
        Comment(
            id = "author",
            content = "Synthetic author comment",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            kind = Comment.KIND_COMMENT,
            event = null)

    composeTestRule.setContent {
      BookmarkDetailScreen(
          uiState = BookmarkDetailUiState(comments = listOf(syntheticAuthorComment)),
          bookmarkInfo =
              BookmarkInfo(
                  title = "Test",
                  urls = listOf("https://example.com"),
                  createdAt = 1_700_000_000L,
                  authorPubkey = "pk_author"),
          onCommentInputChange = {},
          onPostComment = {},
          onNavigateBack = {},
          onDismissError = {})
    }

    composeTestRule.onNodeWithText("Synthetic author comment").performTouchInput { longClick() }
    composeTestRule
        .onNodeWithText(getTestString(R.string.menu_copy_nostr_link))
        .assertDoesNotExist()
  }
}
