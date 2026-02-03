package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [QuoteCard] */
class QuoteCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val formatter: DateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

  @Test
  fun displaysCommentContent() {
    val comment =
        Comment(
            id = "q1",
            content = "This is a quoted text note",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            isAuthorComment = false,
            kind = Comment.KIND_TEXT_NOTE)

    composeTestRule.setContent { QuoteCard(comment = comment) }

    composeTestRule.onNodeWithText("This is a quoted text note").assertIsDisplayed()
  }

  @Test
  fun displaysTimestamp() {
    val timestamp = 1_700_000_000L
    val comment =
        Comment(
            id = "q2",
            content = "Another quote",
            authorPubkey = "pk2",
            createdAt = timestamp,
            isAuthorComment = false,
            kind = Comment.KIND_TEXT_NOTE)

    composeTestRule.setContent { QuoteCard(comment = comment) }

    val expected = formatter.format(Instant.ofEpochSecond(timestamp))
    composeTestRule.onNodeWithText(expected).assertIsDisplayed()
  }
}
