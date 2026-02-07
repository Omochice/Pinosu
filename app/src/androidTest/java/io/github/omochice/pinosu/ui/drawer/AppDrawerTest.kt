package io.github.omochice.pinosu.ui.drawer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import io.github.omochice.pinosu.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for AppDrawer
 *
 * Tests cover:
 * - Drawer rendering with all menu items
 * - Menu item click handling
 */
class AppDrawerTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext

  @Test
  fun `AppDrawer should display all menu items`() {
    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = {},
          onNavigateToAppInfo = {},
          onNavigateToSettings = {},
          onLogout = {},
          onCloseDrawer = {})
    }

    composeTestRule.onNodeWithText(context.getString(R.string.menu_licenses)).assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.menu_app_info)).assertIsDisplayed()
    composeTestRule.onNodeWithText(context.getString(R.string.menu_logout)).assertIsDisplayed()
  }

  @Test
  fun `AppDrawer should handle license click`() {
    var licenseClicked = false
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = { licenseClicked = true },
          onNavigateToAppInfo = {},
          onNavigateToSettings = {},
          onLogout = {},
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(context.getString(R.string.menu_licenses)).performClick()
    assertTrue("License navigation should be triggered", licenseClicked)
    assertTrue("Drawer should be closed after click", drawerClosed)
  }

  @Test
  fun `AppDrawer should handle logout click`() {
    var logoutClicked = false
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = {},
          onNavigateToAppInfo = {},
          onNavigateToSettings = {},
          onLogout = { logoutClicked = true },
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(context.getString(R.string.menu_logout)).performClick()
    assertTrue("Logout should be triggered", logoutClicked)
    assertTrue("Drawer should be closed after logout click", drawerClosed)
  }
}
