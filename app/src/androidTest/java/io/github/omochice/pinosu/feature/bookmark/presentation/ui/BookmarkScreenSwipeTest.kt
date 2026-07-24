package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkFilterMode
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkTabState
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

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
                  local = BookmarkTabState(items = listOf(localBookmark)),
                  global = BookmarkTabState(items = listOf(globalBookmark)),
                  selectedTab = BookmarkFilterMode.Local),
          onRefresh = {},
          onLoad = {},
          onTabSelected = { tab -> selectedTab = tab })
    }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    assertEquals(
        BookmarkFilterMode.Global,
        selectedTab,
        "Swiping left from Local tab should select Global tab")
  }

  @Test
  fun swipeRightFromGlobalShouldSwitchToLocalTab() {
    var selectedTab: BookmarkFilterMode? = null

    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  local = BookmarkTabState(items = listOf(localBookmark)),
                  global = BookmarkTabState(items = listOf(globalBookmark)),
                  selectedTab = BookmarkFilterMode.Global),
          onRefresh = {},
          onLoad = {},
          onTabSelected = { tab -> selectedTab = tab })
    }

    composeTestRule.onRoot().performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()

    assertEquals(
        BookmarkFilterMode.Local,
        selectedTab,
        "Swiping right from Global tab should select Local tab")
  }

  @Test
  fun indicatorFollowsSwipeGestureInProgress() {
    composeTestRule.setContent {
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  local = BookmarkTabState(items = listOf(localBookmark)),
                  global = BookmarkTabState(items = listOf(globalBookmark)),
                  selectedTab = BookmarkFilterMode.Local),
          onRefresh = {},
          onLoad = {})
    }

    val restingLeft =
        composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).getBoundsInRoot().left

    composeTestRule.onRoot().performTouchInput {
      down(center)
      repeat(4) { moveBy(Offset(-width / 16f, 0f)) }
    }
    composeTestRule.waitForIdle()

    val draggedLeft =
        composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).getBoundsInRoot().left
    composeTestRule.onRoot().performTouchInput { up() }

    assertTrue(
        draggedLeft > restingLeft,
        "Tab indicator should move toward the Global tab while the swipe is in progress " +
            "(resting left: $restingLeft, dragged left: $draggedLeft)")
  }

  @Test
  fun indicatorRestsUnderGlobalTabAfterCompletedSwipe() {
    composeTestRule.setContent {
      var selectedTab by remember { mutableStateOf(BookmarkFilterMode.Local) }
      BookmarkScreen(
          uiState =
              BookmarkUiState(
                  local = BookmarkTabState(items = listOf(localBookmark)),
                  global = BookmarkTabState(items = listOf(globalBookmark)),
                  selectedTab = selectedTab),
          onRefresh = {},
          onLoad = {},
          onTabSelected = { tab -> selectedTab = tab })
    }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    val indicatorBounds =
        composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).getBoundsInRoot()
    val globalTabBounds = composeTestRule.onNodeWithText("Global").getBoundsInRoot()
    val indicatorCenter = (indicatorBounds.left + indicatorBounds.right) / 2

    assertTrue(
        indicatorCenter > globalTabBounds.left && indicatorCenter < globalTabBounds.right,
        "Tab indicator should rest under the Global tab after a completed swipe " +
            "(indicator center: $indicatorCenter, Global tab: $globalTabBounds)")
  }
}
