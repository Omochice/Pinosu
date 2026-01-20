package io.github.omochice.pinosu.presentation.ui.drawer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for AppDrawer
 *
 * Tests cover:
 * - Drawer rendering with all menu items
 * - Menu item click handling
 * - Authentication state affecting logout button
 */
class AppDrawerTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `AppDrawer should display all menu items`() {
    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = {}, onNavigateToAppInfo = {}, onLogout = {}, onCloseDrawer = {})
    }

    composeTestRule.onNodeWithText("ライセンス").assertIsDisplayed()
    composeTestRule.onNodeWithText("アプリ情報").assertIsDisplayed()
    composeTestRule.onNodeWithText("ログアウト").assertIsDisplayed()
  }

  @Test
  fun `AppDrawer should handle license click`() {
    var licenseClicked = false
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = { licenseClicked = true },
          onNavigateToAppInfo = {},
          onLogout = {},
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText("ライセンス").performClick()
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
          onLogout = { logoutClicked = true },
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText("ログアウト").performClick()
    assertTrue("Logout should be triggered", logoutClicked)
    assertTrue("Drawer should be closed after logout click", drawerClosed)
  }

  @Test
  fun `AppDrawer should enable logout when authenticated`() {
    var logoutClicked = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = {},
          onNavigateToAppInfo = {},
          onLogout = { logoutClicked = true },
          onCloseDrawer = {},
          isAuthenticated = true)
    }

    composeTestRule.onNodeWithText("ログアウト").performClick()
    assertTrue("Logout should be clickable when authenticated", logoutClicked)
  }

  @Test
  fun `AppDrawer should disable logout when not authenticated`() {
    var logoutClicked = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = {},
          onNavigateToAppInfo = {},
          onLogout = { logoutClicked = true },
          onCloseDrawer = {},
          isAuthenticated = false)
    }

    composeTestRule.onNodeWithText("ログアウト").performClick()
    assertFalse("Logout should not be clickable when not authenticated", logoutClicked)
  }

  @Test
  fun `AppDrawer should always enable license and app info menus`() {
    var licenseClicked = false
    var appInfoClicked = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigateToLicense = { licenseClicked = true },
          onNavigateToAppInfo = { appInfoClicked = true },
          onLogout = {},
          onCloseDrawer = {},
          isAuthenticated = false)
    }

    composeTestRule.onNodeWithText("ライセンス").performClick()
    assertTrue("License should be clickable even when not authenticated", licenseClicked)

    composeTestRule.onNodeWithText("アプリ情報").performClick()
    assertTrue("App info should be clickable even when not authenticated", appInfoClicked)
  }
}
