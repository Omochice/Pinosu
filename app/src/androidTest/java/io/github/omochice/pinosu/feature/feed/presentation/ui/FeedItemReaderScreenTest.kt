package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.getTestString
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

class FeedItemReaderScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val item =
      FeedItem(
          id = "1",
          feedUrl = "https://blog.example/feed",
          feedTitle = "Example Blog",
          title = "Hello feeds",
          link = "https://blog.example/hello",
          publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
          summary = "A short summary",
          contentHtml = "<p>First paragraph of the article</p><p>Second <b>bold</b> paragraph</p>")

  private val openedUris = mutableListOf<String>()

  private val uriHandler =
      object : UriHandler {
        override fun openUri(uri: String) {
          openedUris += uri
        }
      }

  private fun setReader(
      item: FeedItem?,
      onNavigateUp: () -> Unit = {},
      onBookmark: (String, String) -> Unit = { _, _ -> },
      isReadOnly: Boolean = false,
  ) {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        FeedItemReaderScreen(
            item = item,
            onNavigateUp = onNavigateUp,
            onBookmark = onBookmark,
            isReadOnly = isReadOnly)
      }
    }
  }

  @Test
  fun `shows the title and feed title and the body`() {
    setReader(item)

    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()
    composeTestRule.onNodeWithText("Example Blog", substring = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithText("First paragraph of the article", substring = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Second bold paragraph", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("A short summary").assertDoesNotExist()
  }

  @Test
  fun `shows the summary when the item has no body`() {
    setReader(item.copy(contentHtml = null))

    composeTestRule.onNodeWithText("A short summary").assertIsDisplayed()
  }

  @Test
  fun `says there is no content when the item has neither body nor summary`() {
    setReader(item.copy(contentHtml = null, summary = ""))

    composeTestRule
        .onNodeWithText(getTestString(R.string.message_feed_item_no_content))
        .assertIsDisplayed()
  }

  @Test
  fun `tapping a link in the body opens it in the browser`() {
    setReader(item.copy(contentHtml = "<a href=\"https://blog.example/linked\">Linked article</a>"))

    composeTestRule.onNodeWithText("Linked article", substring = true).performClick()

    assertEquals(listOf("https://blog.example/linked"), openedUris)
  }

  @Test
  fun `a heading is laid out apart from the paragraphs around it`() {
    setReader(
        item.copy(
            contentHtml = "<p>Intro paragraph</p><h2>Section heading</h2><p>After paragraph</p>"))

    val intro = composeTestRule.onNodeWithText("Intro paragraph").getUnclippedBoundsInRoot()
    val heading =
        composeTestRule
            .onNodeWithText("Section heading")
            .assertTextEquals("Section heading")
            .getUnclippedBoundsInRoot()
    val after = composeTestRule.onNodeWithText("After paragraph").getUnclippedBoundsInRoot()

    val gapBelowHeading = after.top - heading.bottom
    assertTrue(gapBelowHeading >= 8.dp, "gap below the heading was $gapBelowHeading")
    assertTrue(
        heading.top - intro.bottom > gapBelowHeading,
        "a heading should be further from the text above it than from the text below it")
  }

  @Test
  fun `title and body break lines at phrases`() {
    setReader(item.copy(contentHtml = "<h2>Section heading</h2><p>First paragraph</p>"))

    listOf("Hello feeds", "Section heading", "First paragraph").forEach { text ->
      assertEquals(LineBreak.WordBreak.Phrase, wordBreakOf(text), text)
    }
  }

  private fun wordBreakOf(text: String): LineBreak.WordBreak {
    val layouts = mutableListOf<TextLayoutResult>()
    composeTestRule
        .onNodeWithText(text)
        .fetchSemanticsNode()
        .config[SemanticsActions.GetTextLayoutResult]
        .action
        ?.invoke(layouts)
    return layouts.single().layoutInput.style.lineBreak.wordBreak
  }

  @Test
  fun `back button navigates up`() {
    var navigatedUp = false
    setReader(item, onNavigateUp = { navigatedUp = true })

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_navigate_up))
        .performClick()

    assertTrue(navigatedUp, "back should leave the reader")
  }

  @Test
  fun `open in browser action opens the item link`() {
    setReader(item)

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_open_in_browser))
        .performClick()

    assertEquals(listOf("https://blog.example/hello"), openedUris)
  }

  @Test
  fun `open in browser action is hidden for an item without a link`() {
    setReader(item.copy(link = null))

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_open_in_browser))
        .assertDoesNotExist()
  }

  @Test
  fun `bookmark action passes the link and title of the item`() {
    var bookmarked: Pair<String, String>? = null
    setReader(item, onBookmark = { link, title -> bookmarked = link to title })

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .performClick()

    assertEquals("https://blog.example/hello" to "Hello feeds", bookmarked)
  }

  @Test
  fun `bookmark action is hidden in read-only mode`() {
    setReader(item, isReadOnly = true)

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_open_in_browser))
        .assertIsDisplayed()
  }

  @Test
  fun `bookmark action is hidden for an item without a link`() {
    setReader(item.copy(link = null))

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
  }

  @Test
  fun `an item that is no longer loaded shows a message with a way back`() {
    var navigatedUp = false
    setReader(null, onNavigateUp = { navigatedUp = true })

    composeTestRule
        .onNodeWithText(getTestString(R.string.message_feed_item_not_found))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_open_in_browser))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
    composeTestRule.onNodeWithText(getTestString(R.string.button_back_to_feeds)).performClick()

    assertTrue(navigatedUp, "the button should leave the reader")
  }
}
