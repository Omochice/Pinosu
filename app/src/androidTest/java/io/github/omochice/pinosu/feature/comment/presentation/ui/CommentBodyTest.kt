package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [CommentBody] */
class CommentBodyTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysCommentContent() {
    val comment =
        Comment(
            id = "b1",
            content = "Body content text",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L,
            isAuthorComment = false)

    composeTestRule.setContent { CommentBody(comment = comment) }

    composeTestRule.onNodeWithText("Body content text").assertIsDisplayed()
  }

  @Test
  fun displaysFormattedTimestamp() {
    val timestamp = 1_700_000_000L
    val comment =
        Comment(
            id = "b2",
            content = "Some content",
            authorPubkey = "pk1",
            createdAt = timestamp,
            isAuthorComment = false)

    composeTestRule.setContent { CommentBody(comment = comment) }

    val expected = formatTimestamp(timestamp)
    composeTestRule.onNodeWithText(expected).assertIsDisplayed()
  }
}
