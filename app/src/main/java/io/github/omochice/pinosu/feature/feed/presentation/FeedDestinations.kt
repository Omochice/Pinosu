package io.github.omochice.pinosu.feature.feed.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.omochice.pinosu.core.navigation.Feed
import io.github.omochice.pinosu.core.navigation.Login
import io.github.omochice.pinosu.core.navigation.PostBookmark
import io.github.omochice.pinosu.core.navigation.defaultEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultExitTransition
import io.github.omochice.pinosu.core.navigation.defaultPopEnterTransition
import io.github.omochice.pinosu.core.navigation.defaultPopExitTransition
import io.github.omochice.pinosu.feature.feed.presentation.ui.FeedScreen
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedViewModel

/**
 * Add the feed timeline destination
 *
 * The login state is passed as functions because the navigation graph outlives a single
 * composition, so a plain value would go stale.
 *
 * @param navController Controller used to leave the feed screen
 * @param isLoggedIn Whether a user is logged in; the screen returns to login when this turns false
 * @param isReadOnly Whether the user logged in read-only, which hides the bookmark action
 * @param onOpenDrawer Callback when the hamburger menu is clicked to open the drawer
 */
fun NavGraphBuilder.feedDestinations(
    navController: NavController,
    isLoggedIn: () -> Boolean,
    isReadOnly: () -> Boolean,
    onOpenDrawer: () -> Unit,
) {
  composable<Feed>(
      enterTransition = { defaultEnterTransition },
      exitTransition = { defaultExitTransition },
      popEnterTransition = { defaultPopEnterTransition },
      popExitTransition = { defaultPopExitTransition }) {
        FeedTimelineDestination(
            navController = navController,
            isLoggedIn = isLoggedIn(),
            isReadOnly = isReadOnly(),
            onOpenDrawer = onOpenDrawer)
      }
}

@Composable
private fun FeedTimelineDestination(
    navController: NavController,
    isLoggedIn: Boolean,
    isReadOnly: Boolean,
    onOpenDrawer: () -> Unit,
) {
  val feedViewModel: FeedViewModel = hiltViewModel()
  val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(isLoggedIn) {
    if (!isLoggedIn) {
      navController.navigate(Login) { popUpTo<Feed> { inclusive = true } }
    }
  }

  FeedScreen(
      uiState = feedUiState,
      onRefresh = feedViewModel::refresh,
      onOpenDrawer = onOpenDrawer,
      onBookmarkItem = { link, title ->
        navController.navigate(PostBookmark(sharedUrl = link, sharedTitle = title))
      },
      isReadOnly = isReadOnly)
}
