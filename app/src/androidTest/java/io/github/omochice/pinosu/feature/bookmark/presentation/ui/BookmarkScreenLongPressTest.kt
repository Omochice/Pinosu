package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.nip.nipb0.NipB0
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Rule

/** Compose UI tests for the long-press menu integration on [BookmarkScreen] */
class BookmarkScreenLongPressTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleBookmark =
      BookmarkItem(
          type = "event",
          eventId = "abc",
          title = "Test Bookmark",
          url = "https://example.com",
          urls = listOf("https://example.com"),
          rawJson =
              """{"id":"abc","pubkey":"64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924","created_at":1000,"kind":39701,"tags":[],"content":"hello","sig":"xyz"}""",
          event =
              BookmarkedEvent(
                  kind = NipB0.KIND_BOOKMARK_LIST,
                  content = "hello",
                  author = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924",
                  createdAt = 1000,
                  tags = listOf(listOf(NipB0.Tag.IDENTIFIER, "abc"))))

  @Test
  fun selectingCopyRawJsonFromMenuInvokesCallbackWithRawJson() {
    var capturedRawJson: String? = null

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  isLoading = false,
                  allBookmarks = listOf(sampleBookmark),
                  userHexPubkey =
                      "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"),
          onRefresh = {},
          onLoad = {},
          onLongPressBookmark = { rawJson -> capturedRawJson = rawJson })
    }

    composeTestRule.onNodeWithText("Test Bookmark").performTouchInput { longClick() }
    composeTestRule.onNodeWithText(getTestString(R.string.menu_copy_raw_json)).performClick()

    assertEquals(
        sampleBookmark.rawJson,
        capturedRawJson,
        "Tapping Copy raw JSON should pass rawJson to the callback")
  }

  @Test
  fun selectingCopyNostrLinkFromMenuInvokesCallbackWithNAddr() {
    var capturedNostrLink: String? = null

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  isLoading = false,
                  allBookmarks = listOf(sampleBookmark),
                  userHexPubkey =
                      "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"),
          onRefresh = {},
          onLoad = {},
          onCopyNostrLink = { encoded -> capturedNostrLink = encoded })
    }

    composeTestRule.onNodeWithText("Test Bookmark").performTouchInput { longClick() }
    composeTestRule.onNodeWithText(getTestString(R.string.menu_copy_nostr_link)).performClick()

    val link =
        assertNotNull(capturedNostrLink, "Tapping Copy nostr link should invoke the callback")
    assertTrue(
        link.startsWith("nostr:naddr1"),
        "Encoded value should be a nostr:naddr1 URI but was '$link'")
  }

  @Test
  fun copyNostrLinkIsHiddenWhenBookmarkHasNoDTag() {
    val bookmarkWithoutDTag =
        sampleBookmark.copy(event = sampleBookmark.event!!.copy(tags = emptyList()))

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  isLoading = false,
                  allBookmarks = listOf(bookmarkWithoutDTag),
                  userHexPubkey =
                      "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924"),
          onRefresh = {},
          onLoad = {})
    }

    composeTestRule.onNodeWithText("Test Bookmark").performTouchInput { longClick() }
    composeTestRule
        .onNodeWithText(getTestString(R.string.menu_copy_nostr_link))
        .assertDoesNotExist()
  }
}
