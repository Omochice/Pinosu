package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedUiState
import io.github.omochice.pinosu.getTestString
import java.io.IOException
import java.time.Instant
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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

  private val newsItem =
      item.copy(
          id = "n",
          feedUrl = "https://news.example/feed",
          title = "Election results",
          link = "https://news.example/election",
          feedCategory = "News")

  private val techItem =
      item.copy(
          id = "t",
          feedUrl = "https://tech.example/feed",
          title = "Kotlin release",
          link = "https://tech.example/kotlin",
          feedCategory = "Tech")

  private val artItem =
      item.copy(
          id = "a",
          feedUrl = "https://art.example/feed",
          title = "Gallery opening",
          link = "https://art.example/gallery",
          feedCategory = "Art")

  private val categorized
    get() =
        loaded(newsItem, techItem, artItem, item).copy(categories = listOf("News", "Tech", "Art"))

  /** Feed screen whose selected tab follows the screen's own selections, as the ViewModel does */
  @Composable
  private fun CategorizedFeedScreen(
      initialCategory: String? = null,
      onRefresh: () -> Unit = {},
      onLoadArticleImage: suspend (String) -> Unit = {},
  ) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    FeedScreen(
        uiState = categorized.copy(selectedCategory = selectedCategory),
        onRefresh = onRefresh,
        onSelectCategory = { selectedCategory = it },
        onLoadArticleImage = onLoadArticleImage)
  }

  private fun onItemImage() =
      composeTestRule.onNodeWithContentDescription("Hello feeds", useUnmergedTree = true)

  private fun requestedArticleImages(uiState: FeedUiState): List<String> {
    val requested = mutableListOf<String>()
    composeTestRule.setContent {
      FeedScreen(uiState = uiState, onRefresh = {}, onLoadArticleImage = { requested.add(it) })
    }
    composeTestRule.waitForIdle()
    return requested
  }

  private fun loaded(vararg items: FeedItem, failedFeeds: List<FeedFetchFailure> = emptyList()) =
      FeedUiState(content = FeedUiState.Content.Loaded(items.toList(), failedFeeds))

  @Test
  fun `shows the title together with the feed title but not the summary`() {
    composeTestRule.setContent { FeedScreen(uiState = loaded(item), onRefresh = {}) }

    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
    composeTestRule.onNodeWithText("Example Blog", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("A short summary").assertDoesNotExist()
  }

  @Test
  fun `card shows the image the feed gives the item`() {
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item.copy(imageUrl = "https://blog.example/hello.png")), onRefresh = {})
    }

    onItemImage().assertIsDisplayed()
    composeTestRule.onNodeWithText(NO_IMAGE, useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun `card without any image shows the placeholder instead of an image`() {
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item),
          onRefresh = {},
          articleImages = mapOf("https://blog.example/hello" to null))
    }

    onItemImage().assertDoesNotExist()
    composeTestRule.onNodeWithText(NO_IMAGE, useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun `card shows the article image once it is known`() {
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item),
          onRefresh = {},
          articleImages = mapOf("https://blog.example/hello" to "https://blog.example/og.png"))
    }

    onItemImage().assertIsDisplayed()
  }

  @Test
  fun `shown card without a feed image asks for the article image of its link`() {
    val requested = requestedArticleImages(loaded(item))

    assertEquals(listOf("https://blog.example/hello"), requested)
  }

  @Test
  fun `card with a feed image does not ask for the article image`() {
    val requested =
        requestedArticleImages(loaded(item.copy(imageUrl = "https://blog.example/hello.png")))

    assertTrue(requested.isEmpty(), "no article image should be asked for")
  }

  @Test
  fun `card without a link does not ask for an article image`() {
    val requested = requestedArticleImages(loaded(item.copy(link = null)))

    assertTrue(requested.isEmpty(), "no article image should be asked for")
  }

  @Test
  fun `only cards that are shown ask for their article image`() {
    val many =
        (1..50).map { index ->
          item.copy(id = "$index", title = "Item $index", link = "https://blog.example/$index")
        }

    val requested = requestedArticleImages(loaded(*many.toTypedArray()))

    assertTrue(requested.isNotEmpty(), "the cards on screen should ask")
    assertTrue("https://blog.example/50" !in requested, "a card far below should not ask yet")
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
  fun `tapping an item opens it in the app instead of the browser`() {
    var opened: FeedItem? = null
    var browsed: String? = null
    val uriHandler =
        object : UriHandler {
          override fun openUri(uri: String) {
            browsed = uri
          }
        }
    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        FeedScreen(uiState = loaded(item), onRefresh = {}, onOpenItem = { opened = it })
      }
    }

    composeTestRule.onNodeWithText("Hello feeds").performClick()

    assertEquals(item.feedUrl to item.id, opened?.let { it.feedUrl to it.id })
    assertNull(browsed, "the browser should not open")
  }

  @Test
  fun `tapping an item without a link still opens it`() {
    var opened: FeedItem? = null
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item.copy(link = null)), onRefresh = {}, onOpenItem = { opened = it })
    }

    composeTestRule.onNodeWithText("Hello feeds").performClick()

    assertEquals(item.id, opened?.id)
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
  fun `category tabs follow the All tab`() {
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item).copy(categories = listOf("News", "Tech")), onRefresh = {})
    }

    composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).assertIsSelected()
    composeTestRule.onNodeWithText("News").assertIsDisplayed().assertIsNotSelected()
    composeTestRule.onNodeWithText("Tech").assertIsDisplayed().assertIsNotSelected()
  }

  @Test
  fun `selecting a category tab passes its name`() {
    val selected = mutableListOf<String?>()
    composeTestRule.setContent {
      FeedScreen(
          uiState = loaded(item).copy(categories = listOf("News", "Tech")),
          onRefresh = {},
          onSelectCategory = { selected.add(it) })
    }

    composeTestRule.onNodeWithText("Tech").performClick()

    assertEquals(listOf<String?>("Tech"), selected)
  }

  @Test
  fun `selecting the All tab passes no category`() {
    val selected = mutableListOf<String?>()
    composeTestRule.setContent {
      FeedScreen(
          uiState =
              loaded(item).copy(categories = listOf("News", "Tech"), selectedCategory = "Tech"),
          onRefresh = {},
          onSelectCategory = { selected.add(it) })
    }

    composeTestRule.onNodeWithText("Tech").assertIsSelected()
    composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).performClick()

    assertEquals(listOf<String?>(null), selected)
  }

  @Test
  fun `the page of the selected category shows only its items`() {
    composeTestRule.setContent {
      FeedScreen(uiState = categorized.copy(selectedCategory = "Tech"), onRefresh = {})
    }

    composeTestRule.onNodeWithText("Kotlin release").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello feeds").assertDoesNotExist()
    composeTestRule.onNodeWithText("Election results").assertDoesNotExist()
  }

  @Test
  fun `swiping left moves to the next tab and shows its items`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("News").assertIsSelected()
    composeTestRule.onNodeWithText("Election results").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello feeds").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Kotlin release").assertIsNotDisplayed()
  }

  @Test
  fun `swiping right returns to the previous tab`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()
    composeTestRule.onRoot().performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).assertIsSelected()
    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
  }

  @Test
  fun `tapping a tab moves the pager to its category`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    composeTestRule.onNodeWithText("Tech").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Tech").assertIsSelected()
    composeTestRule.onNodeWithText("Kotlin release").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello feeds").assertIsNotDisplayed()
  }

  @Test
  fun `the indicator rests under the tab swiped to`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    val indicator =
        composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).getBoundsInRoot()
    val newsTab = composeTestRule.onNodeWithText("News").getBoundsInRoot()
    val indicatorCenter = (indicator.left + indicator.right) / 2
    assertTrue(
        indicatorCenter > newsTab.left && indicatorCenter < newsTab.right,
        "indicator center  should be under the News tab ")
  }

  @Test
  fun `two category tabs share the width evenly`() {
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(newsItem, item).copy(categories = listOf("News")), onRefresh = {})
    }

    val root = composeTestRule.onRoot().getUnclippedBoundsInRoot()
    val allTab =
        composeTestRule
            .onNodeWithText(getTestString(R.string.tab_all_feeds))
            .getUnclippedBoundsInRoot()
    val newsTab = composeTestRule.onNodeWithText("News").getUnclippedBoundsInRoot()
    val half = (root.right - root.left) / 2
    assertTrue(abs((allTab.right - allTab.left - half).value) < 1f, "All tab was $allTab")
    assertTrue(abs((newsTab.left - half).value) < 1f, "News tab was $newsTab")
  }

  @Test
  fun `the first category tab starts at the content margin`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    val allTab =
        composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).getBoundsInRoot()
    assertTrue(abs((allTab.left - 16.dp).value) < 1f, "first tab started at ${allTab.left}")
  }

  @Test
  fun `a category tab with a short name is only as wide as its name`() {
    composeTestRule.setContent { CategorizedFeedScreen() }

    val allTab =
        composeTestRule
            .onNodeWithText(getTestString(R.string.tab_all_feeds))
            .getUnclippedBoundsInRoot()
    assertTrue(allTab.right - allTab.left < 90.dp, "tab was ${allTab.right - allTab.left} wide")
  }

  @Test
  fun `the indicator is centered under the selected tab`() {
    composeTestRule.setContent { CategorizedFeedScreen(initialCategory = "Tech") }
    composeTestRule.waitForIdle()

    val indicator =
        composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).getBoundsInRoot()
    val techTab = composeTestRule.onNodeWithText("Tech").getBoundsInRoot()
    val indicatorCenter = (indicator.left + indicator.right) / 2
    val tabCenter = (techTab.left + techTab.right) / 2
    assertTrue(
        abs((indicatorCenter - tabCenter).value) < 1f,
        "indicator center $indicatorCenter should match the Tech tab center $tabCenter")
  }

  @Test
  fun `pulling down on a category page refreshes the timeline`() {
    var refreshed = false
    composeTestRule.setContent {
      CategorizedFeedScreen(initialCategory = "Tech", onRefresh = { refreshed = true })
    }

    composeTestRule.onRoot().performTouchInput {
      swipeDown(startY = height * PULL_START_FRACTION, endY = bottom)
    }
    composeTestRule.waitForIdle()

    assertTrue(refreshed, "pulling down should refresh")
  }

  @Test
  fun `only cards on the shown page ask for their article image`() {
    val requested = mutableListOf<String>()
    composeTestRule.setContent {
      CategorizedFeedScreen(initialCategory = "News", onLoadArticleImage = { requested.add(it) })
    }
    composeTestRule.waitForIdle()

    assertEquals(listOf(newsItem.link), requested)
  }

  @Test
  fun `the page beyond the one swiped to does not ask for article images`() {
    val requested = mutableListOf<String>()
    composeTestRule.setContent {
      CategorizedFeedScreen(initialCategory = "News", onLoadArticleImage = { requested.add(it) })
    }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Tech").assertIsSelected()
    assertTrue(techItem.link in requested, "the card swiped to should ask")
    assertTrue(artItem.link !in requested, "the card on the next page should not ask yet")
  }

  @Test
  fun `the pager shows All when the selected category disappears`() {
    val selected = mutableListOf<String?>()
    var uiState by mutableStateOf(categorized.copy(selectedCategory = "Art"))
    composeTestRule.setContent {
      FeedScreen(uiState = uiState, onRefresh = {}, onSelectCategory = { selected.add(it) })
    }
    composeTestRule.onNodeWithText("Gallery opening").assertIsDisplayed()

    uiState = loaded(newsItem, techItem, item).copy(categories = listOf("News", "Tech"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).assertIsSelected()
    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
    assertTrue(selected.isEmpty(), "no category should be selected by the pager")
  }

  @Test
  fun `swiping without categories keeps the single list`() {
    val selected = mutableListOf<String?>()
    composeTestRule.setContent {
      FeedScreen(uiState = loaded(item), onRefresh = {}, onSelectCategory = { selected.add(it) })
    }

    composeTestRule.onRoot().performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tabIndicator", useUnmergedTree = true).assertDoesNotExist()
    assertTrue(selected.isEmpty(), "no category should be selected")
  }

  @Test
  fun `no tabs are shown when no feed has a category`() {
    composeTestRule.setContent { FeedScreen(uiState = loaded(item), onRefresh = {}) }

    composeTestRule.onNodeWithText(getTestString(R.string.tab_all_feeds)).assertDoesNotExist()
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

  companion object {
    private const val NO_IMAGE = "NO IMAGE"
    private const val PULL_START_FRACTION = 0.3f
  }
}
