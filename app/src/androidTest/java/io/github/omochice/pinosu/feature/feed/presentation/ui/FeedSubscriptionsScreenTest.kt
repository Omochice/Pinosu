package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.model.UnsignedNostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsDialog
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsMessage
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsUiState
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

class FeedSubscriptionsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val added = mutableListOf<String>()
  private val removed = mutableListOf<String>()
  private var confirmed = false
  private var dismissed = false
  private var retried = false

  private val callbacks =
      FeedSubscriptionsCallbacks(
          onNavigateUp = {},
          onRetry = { retried = true },
          onAdd = { added.add(it) },
          onRemove = { removed.add(it) },
          onConfirmPublicNotice = { confirmed = true },
          onDismissDialog = { dismissed = true },
          onMessageShown = {})

  private val event =
      UnsignedNostrEvent(
          pubkey = "f".repeat(64),
          createdAt = 1L,
          kind = Nip78.KIND_APP_DATA,
          tags = emptyList(),
          content = "")

  private fun loaded(vararg feedUrls: String) =
      FeedSubscriptionsUiState(content = FeedSubscriptionsUiState.Content.Loaded(feedUrls.toList()))

  private fun show(uiState: FeedSubscriptionsUiState, isReadOnly: Boolean = false) {
    composeTestRule.setContent {
      FeedSubscriptionsScreen(uiState = uiState, callbacks = callbacks, isReadOnly = isReadOnly)
    }
  }

  @Test
  fun `removing a feed passes its url`() {
    show(loaded(FEED))

    composeTestRule.onNodeWithText(FEED).assertIsDisplayed()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_remove_feed))
        .performClick()

    assertEquals(listOf(FEED), removed)
  }

  @Test
  fun `add and remove are hidden in read-only mode`() {
    show(loaded(FEED), isReadOnly = true)

    composeTestRule.onNodeWithText(FEED).assertIsDisplayed()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_add_feed))
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_remove_feed))
        .assertDoesNotExist()
  }

  @Test
  fun `adding a feed submits the entered url`() {
    show(loaded())

    composeTestRule.onNodeWithContentDescription(getTestString(R.string.cd_add_feed)).performClick()
    composeTestRule.onNode(hasSetTextAction()).performTextInput("blog.example")
    composeTestRule.onNodeWithText(getTestString(R.string.button_add_feed)).performClick()

    assertEquals(listOf("blog.example"), added)
  }

  @Test
  fun `add and remove are disabled while a change is in progress`() {
    show(loaded(FEED).copy(isWorking = true))

    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_add_feed))
        .assertIsNotEnabled()
    composeTestRule
        .onNodeWithContentDescription(getTestString(R.string.cd_remove_feed))
        .assertIsNotEnabled()
  }

  @Test
  fun `picking one of several feeds adds it`() {
    val candidates =
        listOf(FeedCandidate(FEED, "Posts"), FeedCandidate("https://blog.example/comments", null))
    show(loaded().copy(dialog = FeedSubscriptionsDialog.ChooseCandidate(candidates)))

    composeTestRule
        .onNodeWithText(getTestString(R.string.dialog_title_choose_feed))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("https://blog.example/comments").assertIsDisplayed()
    composeTestRule.onNodeWithText("Posts").performClick()

    assertEquals(listOf(FEED), added)
  }

  @Test
  fun `the first list asks to confirm that it is public`() {
    show(loaded().copy(dialog = FeedSubscriptionsDialog.ConfirmPublicList(event)))

    composeTestRule
        .onNodeWithText(getTestString(R.string.dialog_title_feed_list_public))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.button_publish_feed_list)).performClick()

    assertTrue(confirmed, "confirming continues publishing")
  }

  @Test
  fun `cancelling the public notice dismisses it`() {
    show(loaded().copy(dialog = FeedSubscriptionsDialog.ConfirmPublicList(event)))

    composeTestRule.onNodeWithText(getTestString(R.string.button_cancel)).performClick()

    assertTrue(dismissed, "cancelling abandons the change")
    assertTrue(!confirmed, "cancelling must not continue publishing")
  }

  @Test
  fun `a message is shown in a snackbar`() {
    show(loaded(FEED).copy(message = FeedSubscriptionsMessage.AlreadySubscribed))

    composeTestRule
        .onNodeWithText(getTestString(R.string.message_feed_already_subscribed))
        .assertIsDisplayed()
  }

  @Test
  fun `a list that cannot be loaded explains why and retries`() {
    show(
        FeedSubscriptionsUiState(
            content = FeedSubscriptionsUiState.Content.Error(FeedLoadError.AccessDenied)))

    composeTestRule
        .onNodeWithText(getTestString(R.string.error_feed_access_denied))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.button_retry)).performClick()

    assertTrue(retried, "retry loads the list again")
  }

  private companion object {
    const val FEED = "https://blog.example/feed"
  }
}
