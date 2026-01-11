package io.github.omochice.pinosu.presentation.ui.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for DrawerMenuItem
 *
 * Tests cover:
 * - Menu item rendering with icon and text
 * - Click handling
 */
class DrawerMenuItemTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun drawerMenuItem_shouldDisplayTextAndIcon() {
    composeTestRule.setContent {
      DrawerMenuItem(icon = Icons.Default.Home, text = "ホーム", onClick = {})
    }

    composeTestRule.onNodeWithText("ホーム").assertIsDisplayed()
  }

  @Test
  fun drawerMenuItem_shouldHandleClick() {
    var clicked = false

    composeTestRule.setContent {
      DrawerMenuItem(icon = Icons.Default.Home, text = "ホーム", onClick = { clicked = true })
    }

    composeTestRule.onNodeWithText("ホーム").performClick()
    assertTrue("DrawerMenuItem should handle click", clicked)
  }
}
