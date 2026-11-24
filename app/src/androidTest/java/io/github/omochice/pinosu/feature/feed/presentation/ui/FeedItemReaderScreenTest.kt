package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.getTestString
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
      imageUrl: String? = null,
  ) {
    composeTestRule.setContent {
      CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        FeedItemReaderScreen(
            item = item,
            imageUrl = imageUrl,
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
  fun `shows the article image above the title`() {
    setReader(item, imageUrl = "https://blog.example/cover.png")

    val image = composeTestRule.onNodeWithContentDescription("Hello feeds")
    image.assertExists()
    val title = composeTestRule.onNodeWithText("Hello feeds").getUnclippedBoundsInRoot()
    assertTrue(
        image.getUnclippedBoundsInRoot().bottom <= title.top, "the image should be above the title")
  }

  @Test
  fun `shows no image when the item has none`() {
    setReader(item, imageUrl = null)

    composeTestRule.onNodeWithContentDescription("Hello feeds").assertDoesNotExist()
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

  @Test
  fun `shows the caption and every cell of a table`() {
    setReader(item.copy(contentHtml = scoreTable))

    listOf("Scores", "Name", "Score", "Alice", "90", "Bob", "75").forEach { text ->
      composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
  }

  @Test
  fun `a table caption is shown above the table`() {
    setReader(item.copy(contentHtml = scoreTable))

    val caption = composeTestRule.onNodeWithText("Scores").getUnclippedBoundsInRoot()
    val header = composeTestRule.onNodeWithText("Name").getUnclippedBoundsInRoot()

    assertTrue(caption.bottom <= header.top, "caption $caption should be above header $header")
  }

  @Test
  fun `cells of a table row are side by side and columns line up across rows`() {
    setReader(item.copy(contentHtml = scoreTable))

    val name = composeTestRule.onNodeWithText("Name").getUnclippedBoundsInRoot()
    val score = composeTestRule.onNodeWithText("Score").getUnclippedBoundsInRoot()
    val alice = composeTestRule.onNodeWithText("Alice").getUnclippedBoundsInRoot()
    val ninety = composeTestRule.onNodeWithText("90").getUnclippedBoundsInRoot()

    assertTrue(name.right <= score.left, "header cells should be side by side")
    assertTrue(name.bottom <= alice.top, "rows should be stacked")
    assertEquals(name.left, alice.left)
    assertEquals(score.left, ninety.left)
  }

  @Test
  fun `a header cell is shown in bold and a data cell is not`() {
    setReader(item.copy(contentHtml = scoreTable))

    assertEquals(FontWeight.Bold, layoutOf("Name").layoutInput.style.fontWeight)
    assertNotEquals(FontWeight.Bold, layoutOf("Alice").layoutInput.style.fontWeight)
  }

  @Test
  fun `table cells break lines at phrases`() {
    setReader(item.copy(contentHtml = scoreTable))

    listOf("Name", "Alice").forEach { text ->
      assertEquals(LineBreak.WordBreak.Phrase, wordBreakOf(text), text)
    }
  }

  @Test
  fun `tapping a link in a table cell opens it in the browser`() {
    setReader(
        item.copy(
            contentHtml =
                "<table><tr><th>Site</th><th>Note</th></tr>" +
                    "<tr><td><a href=\"https://blog.example/cell\">Cell link</a></td>" +
                    "<td>Plain</td></tr></table>"))

    composeTestRule.onNodeWithText("Cell link").performClick()

    assertEquals(listOf("https://blog.example/cell"), openedUris)
  }

  @Test
  fun `a wide table scrolls sideways on its own to its far right cell`() {
    setReader(item.copy(contentHtml = wideTable))

    val leftBefore = composeTestRule.onNodeWithText("Leftmost cell").getUnclippedBoundsInRoot()
    composeTestRule.onNodeWithText("Far right cell").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()

    composeTestRule.onNode(isHorizontallyScrollable).performTouchInput { swipeLeft() }

    val leftAfter = composeTestRule.onNodeWithText("Leftmost cell").getUnclippedBoundsInRoot()
    assertTrue(leftAfter.left < leftBefore.left, "the table should move left when swiped")
    assertEquals(leftBefore.top, leftAfter.top)
    composeTestRule.onNodeWithText("Hello feeds").assertIsDisplayed()

    composeTestRule.onNodeWithText("Far right cell").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `swiping up on a wide table scrolls the article`() {
    setReader(item.copy(contentHtml = wideTable))

    val before = composeTestRule.onNodeWithText("Leftmost cell").getUnclippedBoundsInRoot()

    composeTestRule.onNode(isHorizontallyScrollable).performTouchInput { swipeUp() }

    val after = composeTestRule.onNodeWithText("Leftmost cell").getUnclippedBoundsInRoot()
    assertTrue(after.top < before.top, "the article should scroll up when the table is swiped up")
    assertEquals(before.left, after.left)
  }

  private val scoreTable =
      "<table><caption>Scores</caption>" +
          "<thead><tr><th>Name</th><th>Score</th></tr></thead>" +
          "<tbody><tr><td>Alice</td><td>90</td></tr><tr><td>Bob</td><td>75</td></tr></tbody>" +
          "</table>"

  private val wideTable =
      "<table><tr><th>Leftmost cell</th>" +
          (1..8).joinToString("") { "<th>Column $it with a long header that needs room</th>" } +
          "<th>Far right cell</th></tr>" +
          "<tr><td>Row</td>" +
          (1..8).joinToString("") { "<td>Value $it in a wide table</td>" } +
          "<td>End</td></tr></table>" +
          (1..30).joinToString("") { "<p>Paragraph $it after the table</p>" }

  private val isHorizontallyScrollable =
      SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)

  private fun wordBreakOf(text: String): LineBreak.WordBreak =
      layoutOf(text).layoutInput.style.lineBreak.wordBreak

  private fun layoutOf(text: String): TextLayoutResult {
    val layouts = mutableListOf<TextLayoutResult>()
    composeTestRule
        .onNodeWithText(text)
        .fetchSemanticsNode()
        .config[SemanticsActions.GetTextLayoutResult]
        .action
        ?.invoke(layouts)
    return layouts.single()
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
  fun `open in browser button opens the item link`() {
    setReader(item)

    composeTestRule.onNodeWithText(getTestString(R.string.button_open_in_browser)).performClick()

    assertEquals(listOf("https://blog.example/hello"), openedUris)
  }

  @Test
  fun `open in browser button sits between the byline and the body`() {
    setReader(item)

    val byline =
        composeTestRule.onNodeWithText("Example Blog", substring = true).getUnclippedBoundsInRoot()
    val button =
        composeTestRule
            .onNodeWithText(getTestString(R.string.button_open_in_browser))
            .getUnclippedBoundsInRoot()
    val body =
        composeTestRule
            .onNodeWithText("First paragraph of the article", substring = true)
            .getUnclippedBoundsInRoot()
    assertTrue(byline.bottom <= button.top, "the button should be below the byline")
    assertTrue(button.bottom <= body.top, "the button should be above the body")
  }

  @Test
  fun `open in browser button is hidden for an item without a link`() {
    setReader(item.copy(link = null))

    composeTestRule
        .onNodeWithText(getTestString(R.string.button_open_in_browser))
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
        .onNodeWithText(getTestString(R.string.button_open_in_browser))
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
        .onNodeWithText(getTestString(R.string.button_open_in_browser))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_bookmark_feed_item))
        .assertDoesNotExist()
    composeTestRule.onNodeWithText(getTestString(R.string.button_back_to_feeds)).performClick()

    assertTrue(navigatedUp, "the button should leave the reader")
  }
}
