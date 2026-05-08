package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.nip.nipb0.NipB0
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.getTestString
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [BookmarkItemCard] */
class BookmarkItemCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun bookmark(
      rawJson: String? = "{\"id\":\"abc\",\"kind\":39701}",
      dTag: String? = "abc"
  ) =
      BookmarkItem(
          type = "event",
          eventId = "abc",
          title = "Test Bookmark",
          url = "https://example.com",
          urls = listOf("https://example.com"),
          rawJson = rawJson,
          event =
              BookmarkedEvent(
                  kind = NipB0.KIND_BOOKMARK_LIST,
                  content = "hello",
                  author = "def",
                  createdAt = 1000L,
                  tags = dTag?.let { listOf(listOf(NipB0.Tag.IDENTIFIER, it)) } ?: emptyList()))

  @Test
  fun longPressOpensDropdownMenuWithCopyRawJsonItem() {
    composeTestRule.setContent {
      BookmarkItemCard(bookmark = bookmark(), onClick = {}, onLongPress = {})
    }

    composeTestRule.onNodeWithText("Test Bookmark").performTouchInput { longClick() }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_copy_raw_json)).assertIsDisplayed()
  }
}
