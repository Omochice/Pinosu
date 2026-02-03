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

/** Compose UI tests for [CommentCard] */
class CommentCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val formatter: DateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

  @Test
  fun displaysCommentContent() {
    val comment =
        Comment(
            id = "c1",
            content = "A regular comment",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            isAuthorComment = false)

    composeTestRule.setContent { CommentCard(comment = comment) }

    composeTestRule.onNodeWithText("A regular comment").assertIsDisplayed()
  }

  @Test
  fun displaysTimestamp() {
    val timestamp = 1_700_000_000L
    val comment =
        Comment(
            id = "c2",
            content = "Some comment",
            authorPubkey = "pk1",
            createdAt = timestamp,
            isAuthorComment = false)

    composeTestRule.setContent { CommentCard(comment = comment) }

    val expected = formatter.format(Instant.ofEpochSecond(timestamp))
    composeTestRule.onNodeWithText(expected).assertIsDisplayed()
  }

  @Test
  fun displaysAuthorLabelForAuthorComment() {
    val comment =
        Comment(
            id = "c3",
            content = "Author's note",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            isAuthorComment = true)

    composeTestRule.setContent { CommentCard(comment = comment) }

    composeTestRule.onNodeWithText("投稿者のコメント").assertIsDisplayed()
  }

  @Test
  fun doesNotDisplayAuthorLabelForRegularComment() {
    val comment =
        Comment(
            id = "c4",
            content = "Not an author comment",
            authorPubkey = "pk2",
            createdAt = 1_700_000_000L,
            isAuthorComment = false)

    composeTestRule.setContent { CommentCard(comment = comment) }

    composeTestRule.onNodeWithText("投稿者のコメント").assertDoesNotExist()
  }
}
