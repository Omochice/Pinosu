package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

/** Compose UI tests for [ProfileAvatar] */
class ProfileAvatarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysAvatarWithContentDescription() {
    composeTestRule.setContent {
      ProfileAvatar(imageUrl = "https://example.com/avatar.png", contentDescription = "avatar")
    }

    composeTestRule.onNodeWithContentDescription("avatar").assertIsDisplayed()
  }

  @Test
  fun displaysFallbackIconWhenUrlIsNull() {
    composeTestRule.setContent {
      ProfileAvatar(imageUrl = null, contentDescription = "fallback avatar")
    }

    composeTestRule.onNodeWithContentDescription("fallback avatar").assertIsDisplayed()
  }
}
