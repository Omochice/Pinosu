package io.github.omochice.pinosu.core.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Rule

class DrawerNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navController: NavHostController

  @BeforeTest
  fun setUp() {
    composeTestRule.setContent {
      navController = rememberNavController()
      NavHost(navController = navController, startDestination = Bookmark) {
        composable<Bookmark> { Text("bookmarks") }
        composable<Feed> { Text("feeds") }
        composable<License> { Text("licenses") }
      }
    }
  }

  private fun navigateFromDrawer(route: Route) {
    composeTestRule.runOnIdle { navController.navigateFromDrawer(route) }
    composeTestRule.waitForIdle()
  }

  private inline fun <reified T : Any> NavBackStackEntry?.isAt(): Boolean =
      this?.destination?.hasRoute<T>() == true

  @Test
  fun `Feeds opens on top of Bookmarks so back returns to Bookmarks`() {
    navigateFromDrawer(Feed)

    assertTrue(navController.currentBackStackEntry.isAt<Feed>())
    assertTrue(navController.previousBackStackEntry.isAt<Bookmark>())
  }

  @Test
  fun `choosing Feeds again does not stack a second Feeds screen`() {
    navigateFromDrawer(Feed)
    navigateFromDrawer(Feed)

    assertTrue(navController.currentBackStackEntry.isAt<Feed>())
    assertTrue(navController.previousBackStackEntry.isAt<Bookmark>())
  }

  @Test
  fun `Bookmarks from Feeds returns to the existing Bookmarks screen`() {
    navigateFromDrawer(Feed)
    navigateFromDrawer(Bookmark)

    assertTrue(navController.currentBackStackEntry.isAt<Bookmark>())
    assertNull(navController.previousBackStackEntry, "Bookmarks is the only screen left")
  }

  @Test
  fun `Feeds from a screen opened over Feeds clears that screen`() {
    navigateFromDrawer(Feed)
    navigateFromDrawer(License)
    navigateFromDrawer(Feed)

    assertTrue(navController.currentBackStackEntry.isAt<Feed>())
    assertTrue(navController.previousBackStackEntry.isAt<Bookmark>())
  }

  @Test
  fun `other drawer entries open on top of the current screen`() {
    navigateFromDrawer(Feed)
    navigateFromDrawer(License)

    assertTrue(navController.currentBackStackEntry.isAt<License>())
    assertTrue(navController.previousBackStackEntry.isAt<Feed>())
  }
}
