package io.github.omochice.pinosu.presentation.ui.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for DrawerMenuItem
 *
 * Tests cover:
 * - Menu item rendering
 * - Click handling when enabled
 * - Click handling when disabled
 */
class DrawerMenuItemTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `DrawerMenuItem should display icon and text`() {
    composeTestRule.setContent {
      DrawerMenuItem(icon = Icons.Default.Info, text = "Test Item", onClick = {})
    }

    composeTestRule.onNodeWithText("Test Item").assertIsDisplayed()
  }

  @Test
  fun `DrawerMenuItem should trigger onClick when enabled and clicked`() {
    var clicked = false

    composeTestRule.setContent {
      DrawerMenuItem(icon = Icons.Default.Info, text = "Test Item", onClick = { clicked = true })
    }

    composeTestRule.onNodeWithText("Test Item").performClick()
    assertTrue("onClick should be triggered when enabled", clicked)
  }

  @Test
  fun `DrawerMenuItem should not trigger onClick when disabled and clicked`() {
    var clicked = false

    composeTestRule.setContent {
      DrawerMenuItem(
          icon = Icons.Default.Info,
          text = "Test Item",
          onClick = { clicked = true },
          enabled = false)
    }

    composeTestRule.onNodeWithText("Test Item").performClick()
    assertFalse("onClick should not be triggered when disabled", clicked)
  }

  @Test
  fun `DrawerMenuItem should be enabled by default`() {
    var clicked = false

    composeTestRule.setContent {
      DrawerMenuItem(icon = Icons.Default.Info, text = "Test Item", onClick = { clicked = true })
    }

    composeTestRule.onNodeWithText("Test Item").performClick()
    assertTrue("DrawerMenuItem should be enabled by default", clicked)
  }
}
