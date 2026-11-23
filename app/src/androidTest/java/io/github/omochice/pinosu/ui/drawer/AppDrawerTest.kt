package io.github.omochice.pinosu.ui.drawer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.navigation.Bookmark
import io.github.omochice.pinosu.core.navigation.Feed
import io.github.omochice.pinosu.core.navigation.License
import io.github.omochice.pinosu.core.navigation.Route
import io.github.omochice.pinosu.getTestString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

/**
 * UI tests for AppDrawer
 *
 * Tests cover:
 * - Drawer rendering with all menu items
 * - Menu item click handling
 */
class AppDrawerTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `AppDrawer should display all menu items`() {
    composeTestRule.setContent { AppDrawer(onNavigate = {}, onLogout = {}, onCloseDrawer = {}) }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_bookmarks)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.menu_feeds)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.menu_licenses)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.menu_app_info)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.menu_settings)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getTestString(R.string.menu_logout)).assertIsDisplayed()
  }

  @Test
  fun `AppDrawer should list Bookmarks and Feeds above the other items`() {
    composeTestRule.setContent { AppDrawer(onNavigate = {}, onLogout = {}, onCloseDrawer = {}) }

    val tops =
        listOf(R.string.menu_bookmarks, R.string.menu_feeds, R.string.menu_licenses).map { id ->
          composeTestRule.onNodeWithText(getTestString(id)).getBoundsInRoot().top
        }
    assertEquals(tops.sorted(), tops, "Bookmarks, Feeds, then Licenses from top to bottom")
  }

  @Test
  fun `AppDrawer should handle bookmarks click`() {
    var navigatedTo: Route? = null
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigate = { route -> navigatedTo = route },
          onLogout = {},
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_bookmarks)).performClick()
    assertEquals(Bookmark, navigatedTo, "Bookmark navigation should be triggered")
    assertTrue(drawerClosed, "Drawer should be closed after click")
  }

  @Test
  fun `AppDrawer should handle feeds click`() {
    var navigatedTo: Route? = null
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigate = { route -> navigatedTo = route },
          onLogout = {},
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_feeds)).performClick()
    assertEquals(Feed, navigatedTo, "Feed navigation should be triggered")
    assertTrue(drawerClosed, "Drawer should be closed after click")
  }

  @Test
  fun `AppDrawer should handle license click`() {
    var navigatedTo: Route? = null
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigate = { route -> navigatedTo = route },
          onLogout = {},
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_licenses)).performClick()
    assertEquals(License, navigatedTo, "License navigation should be triggered")
    assertTrue(drawerClosed, "Drawer should be closed after click")
  }

  @Test
  fun `AppDrawer should handle logout click`() {
    var logoutClicked = false
    var drawerClosed = false

    composeTestRule.setContent {
      AppDrawer(
          onNavigate = {},
          onLogout = { logoutClicked = true },
          onCloseDrawer = { drawerClosed = true })
    }

    composeTestRule.onNodeWithText(getTestString(R.string.menu_logout)).performClick()
    assertTrue(logoutClicked, "Logout should be triggered")
    assertTrue(drawerClosed, "Drawer should be closed after logout click")
  }
}
