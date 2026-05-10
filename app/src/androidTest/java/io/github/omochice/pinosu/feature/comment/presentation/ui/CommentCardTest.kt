package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.Rule

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

    composeTestRule.setContent { CommentCard(comment = comment, onCopyContent = {}) }

    composeTestRule.onNodeWithText("A regular comment").assertIsDisplayed()
  }

  @Test
  fun displaysTimestamp() {
    val timestamp = 1_700_000_000L
    val comment =
        Comment(id = "c2", content = "Some comment", authorPubkey = "pk1", createdAt = timestamp)

    composeTestRule.setContent { CommentCard(comment = comment, onCopyContent = {}) }

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

    composeTestRule.setContent {
      CommentCard(comment = comment, onCopyContent = {}, profileImageUrl = null)
    }

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_commenter_avatar))
        .assertIsDisplayed()
  }

  @Test
  fun selectingCopyNostrLinkInvokesCallback() {
    var invoked = false
    val comment =
        Comment(
            id = "c-nostr",
            content = "A comment to share",
            authorPubkey = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924",
            createdAt = 1_700_000_000L,
            event =
                NostrEvent(
                    id = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
                    pubkey = "64381a1ad1ca81ccb4d264d48904387fc13251bb98d440e0ab4addb6997d7924",
                    createdAt = 1_700_000_000L,
                    kind = Comment.KIND_COMMENT,
                    tags = emptyList(),
                    content = "A comment to share",
                    sig = "sig"))

    composeTestRule.setContent {
      CommentCard(comment = comment, onCopyContent = {}, onCopyNostrLink = { invoked = true })
    }

    composeTestRule.onNodeWithText("A comment to share").performTouchInput { longClick() }
    composeTestRule.onNodeWithText(getTestString(R.string.menu_copy_nostr_link)).performClick()

    assertTrue(invoked, "onCopyNostrLink should be invoked")
  }
}
