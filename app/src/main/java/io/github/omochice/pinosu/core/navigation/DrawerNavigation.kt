package io.github.omochice.pinosu.core.navigation

import androidx.navigation.NavController

/**
 * Navigate to a destination chosen in the navigation drawer
 *
 * Bookmarks and Feeds are top-level screens: choosing one leaves Bookmarks as the only screen
 * beneath it, so switching between them never grows the back stack. Other destinations open on top
 * of the current screen.
 */
fun NavController.navigateFromDrawer(route: Route) {
  if (route is Bookmark || route is Feed) {
    navigate(route) {
      popUpTo<Bookmark>()
      launchSingleTop = true
    }
  } else {
    navigate(route)
  }
}
