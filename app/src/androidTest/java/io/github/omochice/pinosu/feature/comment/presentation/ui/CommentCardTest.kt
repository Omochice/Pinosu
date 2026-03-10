package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.getTestString
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [CommentCard] */
class CommentCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysCommentContent() {
    val comment =
        Comment(
            id = "c1",
            content = "A regular comment",
            authorPubkey = "pk1",
            createdAt = 1_700_000_000L)

    composeTestRule.setContent { CommentCard(comment = comment) }

    composeTestRule.onNodeWithText("A regular comment").assertIsDisplayed()
  }

  @Test
  fun displaysTimestamp() {
    val timestamp = 1_700_000_000L
    val comment =
        Comment(id = "c2", content = "Some comment", authorPubkey = "pk1", createdAt = timestamp)

    composeTestRule.setContent { CommentCard(comment = comment) }

    val expected = formatTimestamp(timestamp)
    composeTestRule.onNodeWithText(expected).assertIsDisplayed()
  }

  @Test
  fun displaysFallbackAvatarWhenProfileImageUrlIsNull() {
    val comment =
        Comment(
            id = "c5",
            content = "Comment without avatar",
            authorPubkey = "pk3",
            createdAt = 1_700_000_000L)

    composeTestRule.setContent { CommentCard(comment = comment, profileImageUrl = null) }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_commenter_avatar))
        .assertIsDisplayed()
  }
}
