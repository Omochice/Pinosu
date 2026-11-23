package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedUiState
import io.github.omochice.pinosu.getTestString
import java.io.IOException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

class FeedScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val item =
      FeedItem(
          id = "1",
          feedUrl = "https://blog.example/feed",
          feedTitle = "Example Blog",
          title = "Hello feeds",
          link = "https://blog.example/hello",
          publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
          summary = "A short summary")

  private fun loaded(vararg items: FeedItem, failedFeeds: List<FeedFetchFailure> = emptyList()) =
      FeedUiState(content = FeedUiState.Content.Loaded(items.toList(), failedFeeds))

  @Test
  fun `shows the title together with the feed title and summary`() {
    composeTestRule.setContent { FeedScreen(uiState = loaded(item), onRefresh = {}) }

    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
    composeTestRule.onNodeWithText("Example Blog", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("A short summary").assertIsDisplayed()
  }

  @Test
  fun `bookmark action passes the link and title of the item`() {
    var bookmarked: Pair<String, String>? = null
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item),
          onRefresh = {},
          onBookmarkItem = { link, title -> bookmarked = link to title },
          isReadOnly = false)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .performClick()

    assertEquals("https://blog.example/hello" to "Hello feeds", bookmarked)
  }

  @Test
  fun `bookmark action is hidden in read-only mode`() {
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item), onRefresh = {}, isReadOnly = true)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
  }

  @Test
  fun `bookmark action is hidden for an item without a link`() {
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item.copy(link = null)), onRefresh = {}, isReadOnly = false)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
  }

  @Test
  fun `tapping an item opens its link`() {
    var opened: String? = null
    val uriHandler =
        object : UriHandler {
          override fun openUri(uri: String) {
            opened = uri
          }
        }
    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        FeedScreen(uiState = loaded(item), onRefresh = {})
      }
    }

    composeTestRule.onNodeWithText("Hello feeds").performClick()

    assertEquals("https://blog.example/hello", opened)
  }

  @Test
  fun `empty state says there are no feeds yet`() {
    composeTestRule.setContent {
      FeedScreen(uiState = FeedUiState(content = FeedUiState.Content.Empty), onRefresh = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.message_no_feeds)).assertIsDisplayed()
  }

  @Test
  fun `feeds that failed are listed in a notice above the items`() {
    val failure = FeedFetchFailure("https://broken.example/feed", IOException("timeout"))
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item, failedFeeds = listOf(failure)), onRefresh = {})
    }

    composeTestRule
        .onNodeWithText(getTestString(R.string.message_feeds_partially_failed))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("https://broken.example/feed").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
  }

  @Test
  fun `error state explains the reason and retries`() {
    var retried = false
    composeTestRule.setContent {
      FeedScreen(
          uiState = FeedUiState(content = FeedUiState.Content.Error(FeedLoadError.NoUserRelays)),
          onRefresh = { retried = true })
    }

    composeTestRule
        .onNodeWithText(getTestString(R.string.error_feed_no_user_relays))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.button_retry)).performClick()

    assertTrue(retried, "retry should reload the timeline")
  }

  @Test
  fun `top bar action opens the subscribed feeds`() {
    var opened = false
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item),
          onRefresh = {},
          onManageSubscriptions = { opened = true },
          isReadOnly = false)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_manage_feed_subscriptions))
        .performClick()

    assertTrue(opened, "the subscribed feeds should open")
  }

  @Test
  fun `top bar action is hidden in read-only mode`() {
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item), onRefresh = {}, isReadOnly = true)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_manage_feed_subscriptions))
        .assertDoesNotExist()
  }

  @Test
  fun `empty state offers to add a feed`() {
    var opened = false
    composeTestRule.setContent {
      FeedScreen(
          uiState = FeedUiState(content = FeedUiState.Content.Empty),
          onRefresh = {},
          onManageSubscriptions = { opened = true },
          isReadOnly = false)
    }

    composeTestRule.onNodeWithText(getTestString(R.string.button_add_first_feed)).performClick()

    assertTrue(opened, "the subscribed feeds should open")
  }

  @Test
  fun `empty state does not offer to add a feed in read-only mode`() {
    composeTestRule.setContent {
      FeedScreen(
          uiState = FeedUiState(content = FeedUiState.Content.Empty),
          onRefresh = {},
          isReadOnly = true)
    }

    composeTestRule.onNodeWithText(getTestString(R.string.message_no_feeds)).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(getTestString(R.string.button_add_first_feed))
        .assertDoesNotExist()
  }

  @Test
  fun `drawer button opens the drawer`() {
    var opened = false
    composeTestRule.setContent {
      FeedScreen(
          uiState = FeedUiState(content = FeedUiState.Content.Empty),
          onRefresh = {},
          onOpenDrawer = { opened = true })
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_open_menu))
        .performClick()

    assertTrue(opened, "drawer should open")
  }
}
