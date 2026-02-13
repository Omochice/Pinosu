package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkFilterMode
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for swipe gesture tab switching on bookmark screen */
class BookmarkScreenSwipeTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val localBookmark =
      BookmarkItem(
          type = "event",
          eventId = "local1",
          title = "Local Bookmark",
          url = "https://example.com/local",
          urls = listOf("https://example.com/local"),
          event =
              BookmarkedEvent(
                  kind = 39701,
                  content = "local content",
                  author = "my-pubkey",
                  createdAt = 1000,
                  tags = emptyList()))

  private val globalBookmark =
      BookmarkItem(
          type = "event",
          eventId = "global1",
          title = "Global Bookmark",
          url = "https://example.com/global",
          urls = listOf("https://example.com/global"),
          event =
              BookmarkedEvent(
                  kind = 39701,
                  content = "global content",
                  author = "other-pubkey",
                  createdAt = 2000,
                  tags = emptyList()))

  @Test
  fun swipeLeftShouldSwitchToGlobalTab() {
    var selectedTab: BookmarkFilterMode? = null

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  isLoading = false,
                  allBookmarks = listOf(localBookmark, globalBookmark),
                  selectedTab = BookmarkFilterMode.Local,
                  userHexPubkey = "my-pubkey"),
          onRefresh = {},
          onLoad = {},
          onTabSelected = { tab -> selectedTab = tab })
    }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    assertEquals(
        "Swiping left from Local tab should select Global tab",
        BookmarkFilterMode.Global,
        selectedTab)
  }

  @Test
  fun swipeRightFromGlobalShouldSwitchToLocalTab() {
    var selectedTab: BookmarkFilterMode? = null

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  isLoading = false,
                  allBookmarks = listOf(localBookmark, globalBookmark),
                  selectedTab = BookmarkFilterMode.Global,
                  userHexPubkey = "my-pubkey"),
          onRefresh = {},
          onLoad = {},
          onTabSelected = { tab -> selectedTab = tab })
    }

    composeTestRule.onRoot().performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()

    assertEquals(
        "Swiping right from Global tab should select Local tab",
        BookmarkFilterMode.Local,
        selectedTab)
  }
}
